package de.fau.cs.mad.kwikshop.android.model.synchronization;


import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.DeletedItem;
import de.fau.cs.mad.kwikshop.android.model.DeletedList;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.restclient.ListClient;
import de.fau.cs.mad.kwikshop.android.util.CollectionUtilities;
import de.fau.cs.mad.kwikshop.common.DeletionInfo;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.conversion.ObjectConverter;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObjectServer;

public abstract class ListSynchronizer<TListClient extends DomainListObject,
                                       TListServer extends DomainListObjectServer>
        extends
            SyncStrategy<TListClient, TListServer, ItemSyncData<TListClient, TListServer>> {



    private final ObjectConverter<TListClient, TListServer> clientToServerObjectConverter;
    private final ListManager<TListClient> listManager;
    private final ListClient<TListServer> listClient;
    private final SimpleStorage<DeletedList> deletedListStorage;
    private final SimpleStorage<DeletedItem> deletedItemStorage;
    private final SimpleStorage<Group> groupStorage;
    private final SimpleStorage<Unit> unitStorage;
    private final SimpleStorage<LastLocation> locationStorage;
    private final ItemSynchronizer<TListClient, TListServer> itemSynchronizer;


    public ListSynchronizer(ObjectConverter<TListClient, TListServer> clientToServerObjectConverter,
                            ListClient<TListServer> listClient,
                            ListManager<TListClient> listManager,
                            SimpleStorage<DeletedList> deletedListStorage,
                            SimpleStorage<DeletedItem> deletedItemStorage,
                            SimpleStorage<Group> groupStorage,
                            SimpleStorage<Unit> unitStorage,
                            SimpleStorage<LastLocation> locationStorage,
                            ItemSynchronizer<TListClient, TListServer> itemSynchronizer) {

        if(clientToServerObjectConverter == null) {
            throw new ArgumentNullException("clientToServerObjectConverter");
        }

        if(listClient == null) {
            throw new ArgumentNullException("listClient");
        }

        if(listManager == null) {
            throw new ArgumentNullException("listManager");
        }

        if(deletedListStorage == null) {
            throw new ArgumentNullException("deletedListStorage");
        }

        if(deletedItemStorage == null) {
            throw new ArgumentNullException("deletedItemStorage");
        }

        if(groupStorage == null) {
            throw new ArgumentNullException("groupStorage");
        }

        if(unitStorage == null) {
            throw new ArgumentNullException("unitStorage");
        }

        if(locationStorage == null) {
            throw new ArgumentNullException("locationStorage");
        }

        if(itemSynchronizer == null) {
            throw new ArgumentNullException("itemSynchronizer");
        }

        this.clientToServerObjectConverter = clientToServerObjectConverter;
        this.listClient = listClient;
        this.listManager = listManager;
        this.deletedListStorage = deletedListStorage;
        this.deletedItemStorage = deletedItemStorage;
        this.groupStorage = groupStorage;
        this.unitStorage = unitStorage;
        this.locationStorage = locationStorage;
        this.itemSynchronizer = itemSynchronizer;
    }



    @Override
    protected ItemSyncData<TListClient, TListServer> initializeSyncData() {


        Collection<TListClient> clientLists = listManager.getLists();
        Collection<DeletedList> clientDeletedLists = deletedListStorage.getItems();
        Collection<DeletedItem> clientDeletedItems = deletedItemStorage.getItems();

        Collection<TListServer> serverLists = listClient.getLists();
        Collection<DeletionInfo> serverDeletedLists = listClient.getDeletedLists();

        Map<Integer, Collection<DeletionInfo>> serverDeletedItems = new HashMap<>();
        Map<Integer, Map<Integer, Item>> allServerListItems = new HashMap<>();
        for(TListServer serverList : serverLists) {
            serverDeletedItems.put(serverList.getId(), listClient.getDeletedListItems(serverList.getId()));
            allServerListItems.put(serverList.getId(), CollectionUtilities.toItemMapByServerId(listClient.getListItems(serverList.getId())));
        }

        Collection<Group> groups = groupStorage.getItems();
        Collection<Unit> units = unitStorage.getItems();
        Collection<LastLocation> locations = locationStorage.getItems();


        return new ItemSyncData<>(clientLists, clientDeletedLists, clientDeletedItems,
                                  serverLists, allServerListItems, serverDeletedLists, serverDeletedItems,
                                  groups, units, locations);
    }

    @Override
    protected void cleanUpSyncData(ItemSyncData<TListClient, TListServer> syncData) {

        listManager.clearSyncData();
    }

    @Override
    protected int getClientId(ItemSyncData<TListClient, TListServer> syncData, TListClient object) {
        return object.getId();
    }

    @Override
    protected int getServerId(ItemSyncData<TListClient, TListServer> syncData, TListServer object) {
        return object.getId();
    }

    @Override
    protected Collection<TListClient> getClientObjects(ItemSyncData<TListClient, TListServer> syncData) {
        return syncData.getClientLists().values();
    }

    @Override
    protected Collection<TListServer> getServerObjects(ItemSyncData<TListClient, TListServer> syncData) {
        return syncData.getServerLists().values();
    }

    @Override
    protected boolean clientObjectExistsOnServer(ItemSyncData<TListClient, TListServer> syncData, TListClient clientList) {
        return clientList.getServerId() > 0;
    }

    @Override
    protected boolean serverObjectExistsOnClient(ItemSyncData<TListClient, TListServer> syncData, TListServer serverList) {
        int serverId = serverList.getId();
        return  syncData.getClientListsByServerId().containsKey(serverId);
    }

    @Override
    protected boolean clientObjectDeletedOnServer(ItemSyncData<TListClient, TListServer> syncData, TListClient clientList) {
        return syncData.getDeletedListsServerByServerId().containsKey(clientList.getServerId());
    }

    @Override
    protected boolean serverObjectDeletedOnClient(ItemSyncData<TListClient, TListServer> syncData, TListServer serverList) {
        return syncData.getDeletedListsClientByServerId().containsKey(serverList.getId());
    }

    @Override
    protected TListClient getClientObjectForServerObject(ItemSyncData<TListClient, TListServer> syncData, TListServer serverList) {
        return syncData.getClientListsByServerId().get(serverList.getId());
    }

    @Override
    protected TListServer getServerObjectForClientObject(ItemSyncData<TListClient, TListServer> syncData, TListClient clientList) {
        int serverId = clientList.getServerId();
        return syncData.getServerLists().get(serverId);
    }

    @Override
    protected boolean serverObjectModified(ItemSyncData<TListClient, TListServer> syncData, TListServer serverList) {

        int serverListId = serverList.getId();
        int lastSeenVersion;

        TListClient clientList = getClientObjectForServerObject(syncData, serverList);

        if(syncData.getDeletedListsClientByServerId().containsKey(serverListId)) {
            lastSeenVersion = syncData.getDeletedListsClientByServerId().get(serverListId).getServerVersion();
        } else if(clientList != null) {
            lastSeenVersion = clientList.getServerVersion();
        } else {
            return true;
        }

        return lastSeenVersion != serverList.getVersion();
    }

    @Override
    protected boolean clientObjectModified(ItemSyncData<TListClient, TListServer> syncData, TListClient clientList) {
        return clientList.getModifiedSinceLastSync();
    }

    @Override
    protected void deleteServerObject(ItemSyncData<TListClient, TListServer> syncData, TListServer serverList) {

        int serverId = serverList.getId();
        listClient.deleteList(serverId);


        //sync items: nothing to do, deleting the list will also delete the items
    }

    @Override
    protected void deleteClientObject(ItemSyncData<TListClient, TListServer> syncData, TListClient clientList) {

        listManager.deleteList(clientList.getId());

        //sync items: nothing to do, deleting the list will also delete the items
    }

    @Override
    protected void createServerObject(ItemSyncData<TListClient, TListServer> syncData, TListClient clientList) {

        TListServer serverList = clientToServerObjectConverter.convert(clientList);
        serverList = listClient.createList(serverList);

        clientList.setServerId(serverList.getId());


        //also upload items
        for(Item clientItem : clientList.getItems()) {
            Item serverItem = listClient.createItem(serverList.getId(), clientItem);

            clientItem.setServerId(serverItem.getServerId());
            clientItem.setVersion(serverItem.getVersion());
            listManager.saveListItem(clientList.getId(), clientItem);
        }

        //get server list again to get the new version
        serverList = listClient.getLists(serverList.getId());
        clientList.setServerVersion(serverList.getVersion());
        listManager.saveList(clientList.getId());
    }

    @Override
    protected void createClientObject(ItemSyncData<TListClient, TListServer> syncData, TListServer serverList) {

        int clientListId = listManager.createList();
        TListClient clientList = listManager.getList(clientListId);

        applyPropertiesToClientData(serverList, clientList);
        clientList.setServerVersion(serverList.getVersion());
        clientList.setServerId(serverList.getId());
        listManager.saveList(clientList.getId());

        List<Item> serverItems = listClient.getListItems(serverList.getId());

        for(Item serverItem : serverItems) {
            listManager.addListItem(clientListId, serverItem);
        }


    }

    @Override
    protected void updateServerObject(ItemSyncData<TListClient, TListServer> syncData, TListClient clientList, TListServer serverList) {

        applyPropertiesToServerData(clientList, serverList);

        serverList = listClient.updateList(serverList.getId(), serverList);

        itemSynchronizer.synchronize(clientList.getId(), serverList.getId(), syncData);

        serverList = listClient.getLists(serverList.getId());
        clientList.setServerVersion(serverList.getVersion());
        listManager.saveList(clientList.getId());
    }

    @Override
    protected void updateClientObject(ItemSyncData<TListClient, TListServer> syncData, TListClient clientList, TListServer serverList) {

        applyPropertiesToClientData(serverList, clientList);
        clientList.setServerId(serverList.getId());

        itemSynchronizer.synchronize(clientList.getId(), serverList.getId(), syncData);

        serverList = listClient.getLists(serverList.getId());
        clientList.setServerVersion(serverList.getVersion());
        listManager.saveList(clientList.getId());

    }



    protected abstract void applyPropertiesToClientData(TListServer source, TListClient target);

    protected abstract void applyPropertiesToServerData(TListClient source,  TListServer target);

}
