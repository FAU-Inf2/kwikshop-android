package de.fau.cs.mad.kwikshop.android.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.restclient.ListClient;
import de.fau.cs.mad.kwikshop.android.util.CollectionUtilities;
import de.fau.cs.mad.kwikshop.common.DeletionInfo;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.conversion.ObjectConverter;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObjectServer;

public abstract class AbstractListSynchronizer<TListClient extends DomainListObject, TListServer extends DomainListObjectServer> {


    private final ListManager<TListClient> listManager;
    private final ListClient<TListServer> listClient;
    private final ObjectConverter<TListClient, TListServer> clientToServerConverter;
    private final SimpleStorage<DeletedList> deletedListStorage;
    private final SimpleStorage<DeletedItem> deletedItemStorage;



    public AbstractListSynchronizer(ListManager<TListClient> listManager,
                                    ListClient<TListServer> listClient,
                                    ObjectConverter<TListClient, TListServer> clientToServerConverter,
                                    SimpleStorage<DeletedList> deletedListStorage,
                                    SimpleStorage<DeletedItem> deletedItemStorage) {

        if(listManager == null) {
            throw new ArgumentNullException("listManager");
        }

        if(listClient == null) {
            throw new ArgumentNullException("listClient");
        }

        if(clientToServerConverter == null) {
            throw new ArgumentNullException("clientToServerConverter");
        }

        if(deletedListStorage == null) {
            throw new ArgumentNullException("deletedListStorage");
        }

        if(deletedItemStorage == null) {
            throw new ArgumentNullException("deletedItemStorage");
        }

        this.listManager = listManager;
        this.listClient = listClient;
        this.clientToServerConverter = clientToServerConverter;
        this.deletedListStorage = deletedListStorage;
        this.deletedItemStorage = deletedItemStorage;
    }



    public void synchronize() {

        Set<Integer> processedServerIds = new HashSet<>();

        Collection<TListClient> clientLists = listManager.getLists();
        Map<Integer, TListServer> serverLists = CollectionUtilities.toMap(listClient.getLists());

        List<DeletedList> deletedListsClient = deletedListStorage.getItems();
        List<DeletedItem> deletedItemsClient = deletedItemStorage.getItems();

        Map<Integer, DeletionInfo> deletedListsServer = CollectionUtilities.toMap(listClient.getDeletedLists());

        Map<Integer, DeletedList> deletedListsClientByClientId =
                CollectionUtilities.toDeletedListMapByClientId(deletedListsClient);

        Map<Integer, Map<Integer, DeletedItem>> deletedItemsClientByServerId =
                CollectionUtilities.toDeletedItemMapByServerId(deletedItemsClient);

        Map<Integer, Map<Integer, DeletedItem>> deletedItemsClientByClientId =
                CollectionUtilities.toDeletedItemMapByClientId(deletedItemsClient);

        for(TListClient clientList : clientLists) {

            //no server id assigned => must be a new list
            if(clientList.getServerId() == 0) {

                syncNewLocalList(processedServerIds, clientList);

            } else {

                if(clientList.getModifiedSinceLastSync()) {
                    syncLocallyModifiedList(processedServerIds, serverLists, deletedListsServer,
                                            clientList, deletedItemsClientByClientId);
                } else {
                    syncLocallyUnmodifiedList(processedServerIds, serverLists, deletedListsServer, clientList);
                }
            }
        }


        //sync lists which have been deleted client-side
        syncLocallyDeletedLists(processedServerIds, serverLists, deletedListsClientByClientId,
                                deletedItemsClientByServerId);


        //find lists present on server which do not exist locally
        syncNewServerLists(processedServerIds);


        //delete client side data we only kept for syncing
        listManager.clearSyncData();

    }



    private void syncNewLocalList(Set<Integer> processedServerIds, TListClient clientList) {
        //create list on the server
        TListServer serverList = clientToServerConverter.convert(clientList);
        serverList = listClient.createList(serverList);


        clientList.setServerId(serverList.getId());


        //upload items
        for(Item clientItem : clientList.getItems()) {

            Item serverItem = listClient.createItem(serverList.getId(), clientItem);
            clientItem.setVersion(serverItem.getVersion());
            clientItem.setServerId(serverItem.getServerId());
        }

        //get serverList again (the version was probably changed)
        serverList = listClient.getLists(serverList.getId());
        clientList.setServerVersion(serverList.getVersion());

        listManager.saveList(clientList.getId());

        processedServerIds.add(serverList.getId());
    }

    private void syncLocallyModifiedList(Set<Integer> processedServerIds,
                                         Map<Integer, TListServer> serverLists,
                                         Map<Integer, DeletionInfo> deletedListsServer,
                                         TListClient clientList,
                                         Map<Integer,Map<Integer, DeletedItem>> allDeltedItemsClientByClientId) {


        //case 1: deleted from server => recreate list on server, upload all new items
        if(deletedListsServer.containsKey(clientList.getServerId())) {

            TListServer serverList = clientToServerConverter.convert(clientList);
            serverList = listClient.createList(serverList);

            for(Item clientItem : clientList.getItems()) {

                // do not upload an Item if it was on the server and was not modified since
                // the last sync (in this case it is considered to have been included in the deletion)
                if(clientItem.getServerId() != 0 && !clientItem.getModifiedSinceLastSync()) {
                    continue;
                }

                Item serverItem = listClient.createItem(serverList.getId(), clientItem);
                clientItem.setVersion(serverItem.getVersion());
                clientItem.setServerId(serverItem.getServerId());
            }

            //the (deleted) server list was processed
            processedServerIds.add(clientList.getServerId());

            //update serverId
            clientList.setServerId(serverList.getId());

            //get serverList again (the version was probably changed)
            serverList = listClient.getLists(serverList.getId());
            clientList.setServerVersion(serverList.getVersion());

            processedServerIds.add(serverList.getId());

            listManager.saveList(clientList.getId());

            return;
        }


        int serverListId = clientList.getServerId();

        //case 2: not modified on server => update version on server
        TListServer serverList = serverLists.get(serverListId);

        if(serverList.getVersion() == clientList.getServerVersion()) {

            applyPropertiesToServerData(clientList, serverList);

            Map<Integer, Item> serverItemsByServerId = CollectionUtilities.toItemMapByServerId(listClient.getListItems(serverListId));

            //no modification on server-side: update
            for(Item clientItem : clientList.getItems()) {

                // new item on client => upload to server
                if(clientItem.getServerId() == 0) {
                    Item serverItem = listClient.createItem(serverList.getId(), clientItem);

                    clientItem.setServerId(serverItem.getServerId());
                    clientItem.setVersion(serverItem.getVersion());
                    listManager.saveListItem(clientList.getId(), clientItem);

                // item modified on client => update server item
                } else {

                    Item serverItem = listClient.getListItem(serverListId, clientItem.getServerId());

                    if(serverItem.getVersion() != clientItem.getVersion()) {

                        applyPropertiesToServerData(clientItem, serverItem);
                        serverItem = listClient.updateItem(serverListId, serverItem.getId(), serverItem);

                        clientItem.setServerId(serverItem.getServerId());
                        clientItem.setVersion(serverItem.getVersion());
                        listManager.saveListItem(clientList.getId(), clientItem);
                    }
                }
            }


            Map<Integer, DeletedItem> deletedItemsClientByClientId =
                    allDeltedItemsClientByClientId.get(clientList.getId());
            if(deletedItemsClientByClientId == null) {
                deletedItemsClientByClientId = new HashMap<>();
            }

            //delete items deleted locally on server
            for(DeletedItem deletedItemClient : deletedItemsClientByClientId.values()) {

                if(deletedItemClient.getItemIdServer() == 0) {
                   continue;
                }

                if(serverItemsByServerId.containsKey(deletedItemClient.getItemId())) {
                    listClient.deleteListItem(serverListId, deletedItemClient.getItemIdServer());
                }
            }

            serverList = listClient.getLists(serverListId);
            clientList.setServerVersion(serverList.getVersion());

            processedServerIds.add(serverListId);
            listManager.saveList(clientList.getId());

            return;
        }


        //TODO
        //case 3: modified on server => merge

        // TODO: add id to processedServerIds
    }

    private void syncLocallyUnmodifiedList(Set<Integer> processedServerIds,
                                           Map<Integer, TListServer> serverLists,
                                           Map<Integer, DeletionInfo> deletedListsServer, TListClient clientList) {
        //not modified on client

        int serverListId = clientList.getServerId();

        //case 1: deleted from server => delete from client
        if(deletedListsServer.containsKey(serverListId)) {

            listManager.deleteList(clientList.getId());
            processedServerIds.add(serverListId);
            return;
        }

        TListServer serverList = serverLists.get(serverListId);

        //case 2: not modified on server => hooray, nothing to do
        if(serverList.getVersion() == clientList.getServerVersion()) {
            processedServerIds.add(serverListId);
            return;
        }

        //case 3: modified on server => apply properties from server to local list
        applyPropertiesToClientData(serverList, clientList);

        // Sync Items
        Map<Integer, DeletionInfo> deletedItemsServer =
                CollectionUtilities.toMap(listClient.getDeletedListItems(serverList.getId()));

        Map<Integer, Item> serverItems = CollectionUtilities.toItemMapByServerId(listClient.getListItems(serverListId));
        Map<Integer, Item> clientItemsByServerId = CollectionUtilities.toItemMapByServerId(clientList.getItems());

        for(Item clientItem : clientItemsByServerId.values()) {

            // as list was not modified locally, there can be no Items on client side
            // that are not present on server
            if(clientItem.getServerId() == 0) {
                throw new UnsupportedOperationException("This should not happen");
            }

            //Item was deleted on server
            if(deletedItemsServer.containsKey(clientItem.getServerId())) {
                listManager.deleteItem(clientList.getId(), clientItem.getId());
                continue;
            }

            Item serverItem = serverItems.get(clientItem.getServerId());
            if(serverItem.getVersion() != clientItem.getVersion()) {

                applyProperties(serverItem, clientItem);
                listManager.saveListItem(clientList.getId(), clientItem);
            }

        }

        //add new items from server to local list
        for(Item serverItem : serverItems.values()) {

            if(!clientItemsByServerId.containsKey(serverItem.getServerId())) {

                listManager.addListItem(clientList.getId(), serverItem);
            }
        }

        listManager.saveList(clientList.getId());
        processedServerIds.add(serverListId);
    }

    /**
     * Processes lists that were deleted client-side
     * @param processedServerIds The et of server listIds that have already been processed
     */
    private void syncLocallyDeletedLists(Set<Integer> processedServerIds,
                                         Map<Integer, TListServer> serverLists,
                                         Map<Integer, DeletedList> deletedListsByClientId,
                                         Map<Integer, Map<Integer, DeletedItem>> allDeletedItemsByServerId) {



        for(DeletedList deletedList : deletedListsByClientId.values()) {

            // type of list not the type being currently synced => skip entry
            if(deletedList.getListType() != listManager.getListType()) {
                continue;
            }

            int serverListId = deletedList.getListIdServer();
            //list has no server id => list was never uploaded to the server => we're done with this entry
            if(serverListId == 0) {
                continue;
            }

            // list was already synced during previous sync phase => we're done with this entry
            if(processedServerIds.contains(serverListId)) {
                continue;
            }

            //case 1: list not found on server (deleted) => hooray, nothing to do
            if(!serverLists.containsKey(serverListId)) {
                continue;
            }


            TListServer serverList = serverLists.get(serverListId);

            //case 2: list not modified on server => delete on server
            if(serverList.getVersion() == deletedList.getServerVersion()) {

                listClient.deleteList(serverListId);
                processedServerIds.add(serverListId);
                continue;
            }

            //case 3: list modified on server => download list from server, recreate local list, sync items

            int clientListId = listManager.createList();
            TListClient clientList = listManager.getList(clientListId);

            applyPropertiesToClientData(serverList, clientList);

            List<Item> serverItems = listClient.getListItems(serverListId);

            Map<Integer, DeletedItem> deletedItemsByServerId = allDeletedItemsByServerId.get(serverListId);
            if(deletedItemsByServerId == null) {
                deletedItemsByServerId = new HashMap<>();
            }

            for(Item item : serverItems) {

                //server item was deleted locally
                if(deletedItemsByServerId.containsKey(item.getServerId())) {

                    //item was not modified on server since being deleted locally => delete on server
                    if(deletedItemsByServerId.get(item.getServerId()).getServerVersion() == item.getVersion()) {

                        listClient.deleteListItem(serverListId, item.getServerId());
                        continue;
                    }
                }

                listManager.addListItem(clientListId, item);
            }

            processedServerIds.add(serverListId);
        }

    }

    /**
     * Downloads all lists from the server that we're created on the server since the last sync
     * @param processedServerIds The et of server listIds that have already been processed
     */
    private void syncNewServerLists(Set<Integer> processedServerIds) {

        List<TListServer> listsOnServer = listClient.getLists();
        for(TListServer serverList : listsOnServer) {

            //skip lists we already processed in previous step
            if(processedServerIds.contains(serverList.getId())) {
                continue;
            }


            //list present on server but not on client => download list and create local copy
            int id = listManager.createList();
            TListClient clientList = listManager.getList(id);
            applyPropertiesToClientData(serverList, clientList);

            List<Item> itemsOnServer = listClient.getListItems(serverList.getId());
            for(Item itemOnServer : itemsOnServer) {

                listManager.addListItem(id, itemOnServer);
            }
        }
    }



    protected abstract void applyPropertiesToClientData(TListServer source, TListClient target);

    protected abstract void applyPropertiesToServerData(TListClient source,  TListServer target);

    protected abstract void applyProperties(Item source, Item target);

    protected abstract void applyPropertiesToServerData(Item source, Item target);


}
