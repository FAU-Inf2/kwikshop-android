package de.fau.cs.mad.kwikshop.android.model.synchronization;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.DeletedItem;
import de.fau.cs.mad.kwikshop.android.model.DeletedList;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.restclient.ListClient;
import de.fau.cs.mad.kwikshop.common.DeletionInfo;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.conversion.ObjectConverter;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObjectServer;

public abstract class ListSynchronizer<TListClient extends DomainListObject,
                                       TListServer extends DomainListObjectServer>
        extends
            SyncStrategy<TListClient, TListServer, ListSyncData<TListClient, TListServer>> {



    private final ObjectConverter<TListClient, TListServer> clientToServerObjectConverter;
    private final ListManager<TListClient> listManager;
    private final ListClient<TListServer> listClient;
    private final SimpleStorage<DeletedList> deletedListStorage;
    private final SimpleStorage<DeletedItem> deletedItemStorage;
    private final SimpleStorage<Group> groupStorage;
    private final SimpleStorage<Unit> unitStorage;
    private final SimpleStorage<LastLocation> locationStorage;



    public ListSynchronizer(ObjectConverter<TListClient, TListServer> clientToServerObjectConverter,
                            ListClient<TListServer> listClient,
                            ListManager<TListClient> listManager,
                            SimpleStorage<DeletedList> deletedListStorage,
                            SimpleStorage<DeletedItem> deletedItemStorage,
                            SimpleStorage<Group> groupStorage,
                            SimpleStorage<Unit> unitStorage,
                            SimpleStorage<LastLocation> locationStorage) {

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

        this.clientToServerObjectConverter = clientToServerObjectConverter;
        this.listClient = listClient;
        this.listManager = listManager;
        this.deletedListStorage = deletedListStorage;
        this.deletedItemStorage = deletedItemStorage;
        this.groupStorage = groupStorage;
        this.unitStorage = unitStorage;
        this.locationStorage = locationStorage;
    }



    @Override
    protected ListSyncData<TListClient, TListServer> initializeSyncData() {


        Collection<TListClient> clientLists = listManager.getLists();
        Collection<DeletedList> clientDeletedLists = deletedListStorage.getItems();
        Collection<DeletedItem> clientDeletedItems = deletedItemStorage.getItems();

        Collection<TListServer> serverLists = listClient.getLists();
        Collection<DeletionInfo> serverDeletedLists = listClient.getDeletedLists();

        Map<Integer, Collection<DeletionInfo>> serverDeletedItems = new HashMap<>();
        for(TListServer serverList : serverLists) {
            serverDeletedItems.put(serverList.getId(), listClient.getDeletedListItems(serverList.getId()));
        }

        Collection<Group> groups = groupStorage.getItems();
        Collection<Unit> units = unitStorage.getItems();
        Collection<LastLocation> locations = locationStorage.getItems();


        return new ItemSyncData<>(clientLists, clientDeletedLists, clientDeletedItems,
                                  serverLists, serverDeletedLists, serverDeletedItems,
                                  groups, units, locations);
    }

    @Override
    protected void cleanUpSyncData(ListSyncData<TListClient, TListServer> syncData) {

        listManager.clearSyncData();
    }

    @Override
    protected int getClientId(ListSyncData<TListClient, TListServer> syncData, TListClient object) {
        return object.getId();
    }

    @Override
    protected int getServerId(ListSyncData<TListClient, TListServer> syncData, TListServer object) {
        return object.getId();
    }

    @Override
    protected Collection<TListClient> getClientObjects(ListSyncData<TListClient, TListServer> syncData) {
        return syncData.getClientLists().values();
    }

    @Override
    protected Collection<TListServer> getServerObjects(ListSyncData<TListClient, TListServer> syncData) {
        return syncData.getServerLists().values();
    }

    @Override
    protected boolean clientObjectExistsOnServer(ListSyncData<TListClient, TListServer> syncData, TListClient clientList) {
        return clientList.getServerId() > 0;
    }

    @Override
    protected boolean serverObjectExistsOnClient(ListSyncData<TListClient, TListServer> syncData, TListServer serverList) {
        int serverId = serverList.getId();
        return  syncData.getClientListsByServerId().containsKey(serverId);
    }

    @Override
    protected boolean clientObjectDeletedOnServer(ListSyncData<TListClient, TListServer> syncData, TListClient clientList) {
        return syncData.getDeletedListsServerByServerId().containsKey(clientList.getServerId());
    }

    @Override
    protected boolean serverObjectDeletedOnClient(ListSyncData<TListClient, TListServer> syncData, TListServer serverList) {
        return syncData.getDeletedListsClientByServerId().containsKey(serverList.getId());
    }

    @Override
    protected TListClient getClientObjectForServerObject(ListSyncData<TListClient, TListServer> syncData, TListServer serverList) {
        return syncData.getClientListsByServerId().get(serverList.getId());
    }

    @Override
    protected TListServer getServerObjectForClientObject(ListSyncData<TListClient, TListServer> syncData, TListClient clientList) {
        int serverId = clientList.getServerId();
        return syncData.getServerLists().get(serverId);
    }

    @Override
    protected boolean serverObjectModified(ListSyncData<TListClient, TListServer> syncData, TListServer serverList) {

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
    protected boolean clientObjectModified(ListSyncData<TListClient, TListServer> syncData, TListClient clientList) {
        return clientList.getModifiedSinceLastSync();
    }

    @Override
    protected void deleteServerObject(ListSyncData<TListClient, TListServer> syncData, TListServer serverList) {

        int serverId = serverList.getId();
        listClient.deleteList(serverId);
    }

    @Override
    protected void deleteClientObject(ListSyncData<TListClient, TListServer> syncData, TListClient clientList) {

        listManager.deleteList(clientList.getId());
    }

    @Override
    protected void createServerObject(ListSyncData<TListClient, TListServer> syncData, TListClient clientList) {

        TListServer serverList = clientToServerObjectConverter.convert(clientList);
        serverList = listClient.createList(serverList);

        clientList.setServerVersion(serverList.getVersion());
        clientList.setServerId(serverList.getId());
        listManager.saveList(clientList.getId());
    }

    @Override
    protected void createClientObject(ListSyncData<TListClient, TListServer> syncData, TListServer serverList) {

        int clientListId = listManager.createList();
        TListClient clientList = listManager.getList(clientListId);

        applyPropertiesToClientData(serverList, clientList);
        clientList.setServerVersion(serverList.getVersion());
        clientList.setServerId(serverList.getId());

        listManager.saveList(clientList.getId());
    }

    @Override
    protected void updateServerObject(ListSyncData<TListClient, TListServer> syncData, TListClient clientObject, TListServer serverList) {

        applyPropertiesToServerData(clientObject, serverList);

        serverList = listClient.updateList(serverList.getId(), serverList);

        clientObject.setServerVersion(serverList.getVersion());
        listManager.saveList(clientObject.getId());
    }

    @Override
    protected void updateClientObject(ListSyncData<TListClient, TListServer> syncData, TListClient clientList, TListServer serverList) {

        applyPropertiesToClientData(serverList, clientList);
        clientList.setServerVersion(serverList.getVersion());
        clientList.setServerId(serverList.getId());

        listManager.saveList(clientList.getId());

    }



    protected abstract void applyPropertiesToClientData(TListServer source, TListClient target);

    protected abstract void applyPropertiesToServerData(TListClient source,  TListServer target);

}
