package de.fau.cs.mad.kwikshop.android.model.synchronization;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.NotSupportedException;

import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.DeletedItem;
import de.fau.cs.mad.kwikshop.android.model.DeletedList;
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

    // Local Storage
    private final ListManager<TListClient> listManager;
    private final SimpleStorage<DeletedList> deletedListStorage;
    private final SimpleStorage<DeletedItem> deletedItemStorage;

    // Server interaction
    //      listClient for interaction with the server
    private final ListClient<TListServer> listClient;

    // Utilities
    //      converter to convert client-side lists to server lists
    private final ObjectConverter<TListClient, TListServer> clientToServerConverter;



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

        //set to save which lists from the server are already done (we can skip them later on)
        Set<Integer> processedServerIds = new HashSet<>();

        //get all the client-side lists and the lists and items deleted on the client
        Collection<TListClient> clientLists = listManager.getLists();
        List<DeletedList> deletedListsClient = deletedListStorage.getItems();
        List<DeletedItem> deletedItemsClient = deletedItemStorage.getItems();

        //create maps from client-side lists for easy lookups
        Map<Integer, DeletedList> deletedListsClientByClientId =
                CollectionUtilities.toDeletedListMapByClientId(deletedListsClient);

        Map<Integer, Map<Integer, DeletedItem>> deletedItemsClientByServerId =
                CollectionUtilities.toDeletedItemMapByServerId(deletedItemsClient);

        Map<Integer, Map<Integer, DeletedItem>> deletedItemsClientByClientId =
                CollectionUtilities.toDeletedItemMapByClientId(deletedItemsClient);

        //get all the lists and deleted lists from the server
        Map<Integer, TListServer> serverLists = CollectionUtilities.toMap(listClient.getLists());
        Map<Integer, DeletionInfo> deletedListsServer = CollectionUtilities.toMap(listClient.getDeletedLists());


        //iterate over all client lists and sync them
        for(TListClient clientList : clientLists) {

            //no server id assigned => must be a new list created since the last sync
            if(clientList.getServerId() == 0) {

                syncNewLocalList(processedServerIds, clientList);

            } else {

                //list was not modified locally since last sync
                if(clientList.getModifiedSinceLastSync()) {
                    syncLocallyModifiedList(processedServerIds, clientList, serverLists,
                                            deletedListsServer, deletedItemsClientByClientId,
                                            deletedItemsClientByServerId);

                //list was modified on client since last sync
                } else {
                    syncLocallyUnmodifiedList(processedServerIds, clientList, serverLists,
                                              deletedListsServer);
                }
            }
        }

        //sync lists which have been deleted client-side
        syncLocallyDeletedLists(processedServerIds, serverLists, deletedListsServer,
                                deletedListsClientByClientId, deletedItemsClientByServerId);

        //find lists present on server which do not exist locally
        syncNewServerLists(processedServerIds);

        //delete client side data we only kept for syncing
        listManager.clearSyncData();

    }


    /**
     * Uploads a local list assumed to be only present on the client to the server
     */
    private void syncNewLocalList(Set<Integer> processedServerIds, TListClient clientList) {

        //create list on the server
        TListServer serverList = clientToServerConverter.convert(clientList);
        serverList = listClient.createList(serverList);

        // store serverId in local list for future synchronizations
        clientList.setServerId(serverList.getId());

        //upload items to server
        for(Item clientItem : clientList.getItems()) {

            Item serverItem = listClient.createItem(serverList.getId(), clientItem);
            clientItem.setVersion(serverItem.getVersion());
            clientItem.setServerId(serverItem.getServerId());
        }

        //get serverList again to get the version
        serverList = listClient.getLists(serverList.getId());
        clientList.setServerVersion(serverList.getVersion());

        //save local list
        listManager.saveList(clientList.getId());
        processedServerIds.add(serverList.getId());
    }

    /**
     * Sync a local list which has been modified since the last sync of which a version
     * already exists on the server
     */
    private void syncLocallyModifiedList(Set<Integer> processedServerIds,
                                         TListClient clientList,
                                         Map<Integer, TListServer> serverLists,
                                         Map<Integer, DeletionInfo> deletedListsServer,
                                         Map<Integer,Map<Integer, DeletedItem>> allDeletedItemsClientByClientId,
                                         Map<Integer,Map<Integer, DeletedItem>> allDeletedItemsClientByServerId) {

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
                    allDeletedItemsClientByClientId.get(clientList.getId());
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


        //case 3: modified on server => merge

        // when in doubt, use server data
        applyPropertiesToClientData(serverList, clientList);
        clientList.setServerId(serverList.getId());


        //merge item list
        Map<Integer, Item> serverItems
                = CollectionUtilities.toItemMapByServerId(listClient.getListItems(serverListId));

        Map<Integer, Item> clientItems =
                CollectionUtilities.toItemMapByClientId(clientList.getItems());

        Map<Integer, Item> clientItemsByServerId =
                CollectionUtilities.toItemMapByServerId(clientList.getItems());

        Map<Integer, DeletionInfo> deletedItemsServer =
                CollectionUtilities.toMap(listClient.getDeletedListItems(serverListId));

        Set<Integer> processedSererItemIds = new HashSet<>();

        //iterate over all local items
        for(Item clientItem : clientItems.values()) {

            if(clientItem.getServerId() == 0) {

                Item serverItem = listClient.createItem(serverListId, clientItem);

                clientItem.setServerId(serverItem.getServerId());
                clientItem.setVersion(serverItem.getVersion());

                processedSererItemIds.add(serverItem.getId());
                listManager.saveListItem(clientList.getId(), clientItem);

            } else {

                if(clientItem.getModifiedSinceLastSync()) {

                    //item modified on client, but deleted from server => create new item on server
                    if(deletedItemsServer.containsKey(clientItem.getServerId())) {

                        Item serverItem = listClient.createItem(serverListId, clientItem);

                        clientItem.setVersion(serverItem.getVersion());
                        clientItem.setServerId(serverItem.getServerId());
                        listManager.saveListItem(clientList.getId(), clientItem);

                        processedSererItemIds.add(serverItem.getId());
                        continue;
                    }


                    Item serverItem = listClient.getListItem(serverListId, clientItem.getServerId());

                    //if item was not modified on server side, update server data
                    if(serverItem.getVersion() == clientItem.getVersion()) {

                        applyPropertiesToServerData(clientItem, serverItem);

                        serverItem = listClient.updateItem(serverListId, serverItem.getId(), serverItem);

                        clientItem.setVersion(serverItem.getVersion());
                        listManager.saveListItem(clientList.getId(), clientItem);

                    //else, overwrite client data
                    } else {

                        applyPropertiesToClientData(serverItem, clientItem);

                        clientItem.setVersion(serverItem.getVersion());
                        listManager.saveListItem(clientList.getId(), clientItem);
                    }

                    processedSererItemIds.add(serverItem.getId());


                // item not modified on client
                } else {

                    //item deleted on server => delete on client
                    if(deletedItemsServer.containsKey(clientItem.getServerId())) {
                        listManager.deleteItem(clientList.getId(), clientItem.getId());
                        processedSererItemIds.add(clientItem.getServerId());
                        continue;
                    }

                    Item serverItem = serverItems.get(clientItem.getServerId());

                    //item modified on server, not on client => overwrite local data
                    if(serverItem.getVersion() != clientItem.getVersion()) {

                        applyPropertiesToClientData(serverItem, clientItem);
                        clientItem.setVersion(serverItem.getVersion());

                        listManager.saveListItem(clientList.getId(), clientItem);
                    }

                    processedSererItemIds.add(serverItem.getId());
                }
            }

        }


        Map<Integer, DeletedItem> deletedItemsClientByServerId = allDeletedItemsClientByServerId.get(serverListId);
        if(deletedItemsClientByServerId == null) {
            deletedItemsClientByServerId = new HashMap<>();
        }


        for(Item serverItem : serverItems.values()) {

            if(processedSererItemIds.contains(serverItem.getServerId())) {
                continue;
            }

            // item deleted on client
            if(deletedItemsClientByServerId.containsKey(serverItem.getServerId())) {

                DeletedItem deletedItem = deletedItemsClientByServerId.get(serverItem.getServerId());

                if (deletedItem.getServerVersion() == serverItem.getVersion()) {
                    listClient.deleteListItem(serverListId, serverItem.getServerId());
                } else {
                    listManager.addListItem(clientList.getId(), serverItem);
                }
                continue;
            }


            if(clientItemsByServerId.containsKey(serverItem.getServerId())) {

                //should not happen, we already iterated over all client items
                throw new NotSupportedException();

            } else  {
                listManager.addListItem(clientList.getId(), serverItem);
            }

        }


        serverList = listClient.getLists(serverListId);
        clientList.setServerVersion(serverList.getVersion());

        processedServerIds.add(serverListId);
        listManager.saveList(clientList.getId());

    }

    /**
     * Sync a list which has not been modified since the last sync of which
     * a version already exists on the server
     */
    private void syncLocallyUnmodifiedList(Set<Integer> processedServerIds,
                                           TListClient clientList,
                                           Map<Integer, TListServer> serverLists,
                                           Map<Integer, DeletionInfo> deletedListsServer) {

        int serverListId = clientList.getServerId();


        //case 1: deleted from server => delete from client
        if(deletedListsServer.containsKey(serverListId)) {

            listManager.deleteList(clientList.getId());
            processedServerIds.add(serverListId);
            return; //done
        }

        //get server list
        TListServer serverList = serverLists.get(serverListId);

        //case 2: not modified on server => nothing to do
        if(serverList.getVersion() == clientList.getServerVersion()) {
            processedServerIds.add(serverListId);
            return; //done
        }

        //case 3: modified on server => apply properties from server to local list
        applyPropertiesToClientData(serverList, clientList);

        // Sync Items
        //  get items which were deleted on server
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

            //Item was deleted on server => delete on client
            if(deletedItemsServer.containsKey(clientItem.getServerId())) {
                listManager.deleteItem(clientList.getId(), clientItem.getId());
                continue;
            }

            // Item still present on server => update local version to match server version
            Item serverItem = serverItems.get(clientItem.getServerId());
            if(serverItem.getVersion() != clientItem.getVersion()) {

                applyPropertiesToClientData(serverItem, clientItem);
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
     * @param processedServerIds The set of server listIds that have already been processed
     */
    private void syncLocallyDeletedLists(Set<Integer> processedServerIds,
                                         Map<Integer, TListServer> serverLists,
                                         Map<Integer, DeletionInfo> deletedListsServerByServerId,
                                         Map<Integer, DeletedList> deletedListsByClientId,
                                         Map<Integer, Map<Integer, DeletedItem>> allDeletedItemsByServerId) {


        // iterate over all lists deleted on the client
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
            if(deletedListsServerByServerId.containsKey(serverListId)) {
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

            clientList.setServerVersion(serverList.getVersion());
            clientList.setServerId(serverList.getId());
            applyPropertiesToClientData(serverList, clientList);

            listManager.saveList(clientListId);

            // get items in the list from the server
            List<Item> serverItems = listClient.getListItems(serverListId);

            // get map of items deleted from the client
            Map<Integer, DeletedItem> deletedItemsByServerId = allDeletedItemsByServerId.get(serverListId);
            if(deletedItemsByServerId == null) {
                deletedItemsByServerId = new HashMap<>();
            }


            for(Item item : serverItems) {

                //server item was deleted locally
                if(deletedItemsByServerId.containsKey(item.getServerId())) {

                    //item was not modified on server since being deleted locally => delete on server, too
                    if(deletedItemsByServerId.get(item.getServerId()).getServerVersion() == item.getVersion()) {

                        listClient.deleteListItem(serverListId, item.getServerId());
                        continue;
                    }
                }

                // item on server seems to be never than the item we deleted locally
                //      => add it to the new local list
                listManager.addListItem(clientListId, item);
            }

            processedServerIds.add(serverListId);
        }

    }

    /**
     * Downloads all lists from the server that were created on the server since the last sync
     * @param processedServerIds The et of server listIds that have already been processed
     */
    private void syncNewServerLists(Set<Integer> processedServerIds) {

        List<TListServer> listsOnServer = listClient.getLists();
        for(TListServer serverList : listsOnServer) {

            //skip lists we already processed in one of the previous steps
            if(processedServerIds.contains(serverList.getId())) {
                continue;
            }

            //list present on server but not on client => download list and create local copy
            int clientListId = listManager.createList();
            TListClient clientList = listManager.getList(clientListId);

            clientList.setServerId(serverList.getVersion());
            clientList.setServerVersion(serverList.getVersion());
            applyPropertiesToClientData(serverList, clientList);

            listManager.saveList(clientListId);


            List<Item> itemsOnServer = listClient.getListItems(serverList.getId());
            for(Item itemOnServer : itemsOnServer) {

                listManager.addListItem(clientListId, itemOnServer);
            }
        }
    }



    protected abstract void applyPropertiesToClientData(TListServer source, TListClient target);

    protected abstract void applyPropertiesToServerData(TListClient source,  TListServer target);

    protected void applyPropertiesToClientData(Item source, Item target) {

        target.setServerId(source.getServerId());
        target.setVersion(source.getVersion());

        applyPropertiesCommon(source, target);

        //TODO: group
        //TODO: unit
        //TODO: location
    }

    protected void applyPropertiesToServerData(Item source, Item target){

       applyPropertiesCommon(source, target);

        //TODO: group
        //TODO: unit
        //TODO: location
    }

    protected void applyPropertiesCommon(Item source, Item target) {

        target.setOrder(source.getOrder());
        target.setBought(source.isBought());
        target.setName(source.getName());
        target.setAmount(source.getAmount());
        target.setHighlight(source.isHighlight());
        target.setBrand(source.getBrand());
        target.setComment(source.getComment());
        target.setLastBought(source.getLastBought());
        target.setRepeatType(source.getRepeatType());
        target.setPeriodType(source.getPeriodType());
        target.setSelectedRepeatTime(source.getSelectedRepeatTime());
        target.setRemindFromNextPurchaseOn(source.isRemindFromNextPurchaseOn());
        target.setRemindAtDate(source.getRemindAtDate());

    }


}
