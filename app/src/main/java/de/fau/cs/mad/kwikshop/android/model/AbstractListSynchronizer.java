package de.fau.cs.mad.kwikshop.android.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.restclient.ListClient;
import de.fau.cs.mad.kwikshop.android.util.CollectionUtilities;
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

        Collection<TListClient> listsClient = listManager.getLists();
        Map<Integer, TListServer> listsServer = CollectionUtilities.toMap(listClient.getLists());

        List<DeletedList> deletedListsClient = deletedListStorage.getItems();
        List<DeletedItem> deletedItemsClient = deletedItemStorage.getItems();

        Map<Integer, DeletedList> deletedListsClientByClientId =
                CollectionUtilities.toDeletedListMapByClientId(deletedListsClient);

        Map<Integer, Map<Integer, DeletedItem>> deletedItemsClientByServerId =
                CollectionUtilities.toDeletedItemMapByServerId(deletedItemsClient);


        for(TListClient clientList : listsClient) {

            //no server id assigned => must be a new list
            if(clientList.getServerId() == 0) {

                //create list on the server
                TListServer serverList = clientToServerConverter.convert(clientList);
                serverList = listClient.createList(serverList);

                clientList.setServerVersion(serverList.getVersion());
                clientList.setServerId(serverList.getId());


                //upload items
                for(Item item : clientList.getItems()) {

                    Item serverItem = listClient.createItem(clientList.getId(), item);
                    item.setVersion(serverItem.getVersion());
                    item.setServerId(serverItem.getServerId());
                }

                listManager.saveList(clientList.getId());
            } else {

                if(clientList.getModifiedSinceLastSync()) {

                    //modified on client:

                    //case 1: not modified on server => update version on server

                    //case 2: modified on server => merge

                    //case 3: deleted from server => recreate list on server, upload all new items


                    // TODO: add id to processedServerIds

                } else {

                    //not modified on client

                    //case 1: not modified on server => hooray, nothing to do

                    //case 2: modified on server => apply properties from server to local list

                    //case 3: deleted from server => delete from client

                    // TODO: add id to processedServerIds
                }
            }
        }


        //sync lists which have been deleted client-side
        syncLocallyDeletedLists(processedServerIds, listsServer, deletedListsClientByClientId,
                                deletedItemsClientByServerId);


        //find lists present on server which do not exist locally
        syncNewServerLists(processedServerIds);


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

            applyProperties(serverList, clientList);

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
            applyProperties(serverList, clientList);

            List<Item> itemsOnServer = listClient.getListItems(serverList.getId());
            for(Item itemOnServer : itemsOnServer) {

                listManager.addListItem(id, itemOnServer);
            }
        }
    }




    protected abstract void applyProperties(TListServer properties, TListClient applyTo);



}
