package de.fau.cs.mad.kwikshop.android.model.synchronization;


import java.util.Collection;
import java.util.List;

import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.restclient.ListClient;
import de.fau.cs.mad.kwikshop.common.conversion.ObjectConverter;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObjectServer;

public abstract class ListSynchronizer<TListClient extends DomainListObject,
                              TListServer extends DomainListObjectServer, 
                              TSyncData extends ListSyncData<TListClient, TListServer>>
        extends SyncStrategy<TListClient, TListServer, TSyncData> {


    private final ObjectConverter<TListClient, TListServer> clientToServerObjectConverter;
    private final ListManager<TListClient> listManager;
    private final ListClient<TListServer> listClient;



    public ListSynchronizer(ObjectConverter<TListClient, TListServer> clientToServerObjectConverter,
                            ListClient<TListServer> listClient,
                            ListManager<TListClient> listManager) {

        if(clientToServerObjectConverter == null) {
            throw new ArgumentNullException("clientToServerObjectConverter");
        }

        if(listClient == null) {
            throw new ArgumentNullException("listClient");
        }

        if(listManager == null) {
            throw new ArgumentNullException("listManager");
        }

        this.clientToServerObjectConverter = clientToServerObjectConverter;
        this.listClient = listClient;
        this.listManager = listManager;
    }



    @Override
    protected abstract TSyncData initializeSyncData();

    @Override
    protected void cleanUpSyncData(TSyncData syncData) {

    }

    @Override
    protected int getClientId(TSyncData syncData, TListClient object) {
        return object.getId();
    }

    @Override
    protected int getServerId(TSyncData syncData, TListServer object) {
        return object.getId();
    }

    @Override
    protected Collection<TListClient> getClientObjects(TSyncData syncData) {
        return syncData.getClientLists();
    }

    @Override
    protected Collection<TListServer> getServerObjects(TSyncData syncData) {
        return syncData.getServerLists().values();
    }

    @Override
    protected boolean clientObjectExistsOnServer(TSyncData syncData, TListClient clientList) {
        return clientList.getServerId() > 0;
    }

    @Override
    protected boolean serverObjectExistsOnClient(TSyncData syncData, TListServer serverList) {
        int serverId = serverList.getId();
        return  syncData.getClientListsByServerId().containsKey(serverId);
    }

    @Override
    protected boolean clientObjectDeletedOnServer(TSyncData syncData, TListClient clientList) {
        return syncData.getDeletedListsServerByServerId().containsKey(clientList.getServerId());
    }

    @Override
    protected boolean serverObjectDeletedOnClient(TSyncData syncData, TListServer serverList) {
        return syncData.getDeletedListsClientByServerId().containsKey(serverList.getId());
    }

    @Override
    protected TListClient getClientObjectForServerObject(TSyncData syncData, TListServer serverList) {
        return syncData.getClientListsByServerId().get(serverList.getId());
    }

    @Override
    protected TListServer getServerObjectForClientObject(TSyncData syncData, TListClient clientList) {
        int serverId = clientList.getServerId();
        return syncData.getServerLists().get(serverId);
    }

    @Override
    protected boolean serverObjectModified(TSyncData syncData, TListServer serverList) {
        TListClient clientList = getClientObjectForServerObject(syncData, serverList);
        return clientList.getServerVersion() != serverList.getVersion();
    }

    @Override
    protected boolean clientObjectModified(TSyncData syncData, TListClient clientList) {
        return clientList.getModifiedSinceLastSync();
    }

    @Override
    protected void deleteServerObject(TSyncData syncData, TListServer serverList) {

        int serverId = serverList.getId();
        listClient.deleteList(serverId);
    }

    @Override
    protected void deleteClientObject(TSyncData syncData, TListClient clientList) {

        listManager.deleteList(clientList.getId());
    }

    @Override
    protected void createServerObject(TSyncData syncData, TListClient clientList) {

        TListServer serverList = clientToServerObjectConverter.convert(clientList);
        serverList = listClient.createList(serverList);

        clientList.setServerVersion(serverList.getVersion());
        clientList.setServerId(serverList.getId());
        listManager.saveList(clientList.getId());
    }

    @Override
    protected void createClientObject(TSyncData syncData, TListServer serverList) {

        int clientListId = listManager.createList();
        TListClient clientList = listManager.getList(clientListId);

        applyPropertiesToClientData(serverList, clientList);
        clientList.setServerVersion(serverList.getVersion());
        clientList.setServerId(serverList.getId());

        listManager.saveList(clientList.getId());
    }

    @Override
    protected void updateServerObject(TSyncData syncData, TListClient clientObject, TListServer serverList) {

        applyPropertiesToServerData(clientObject, serverList);

        serverList = listClient.updateList(serverList.getId(), serverList);

        clientObject.setServerVersion(serverList.getVersion());
        listManager.saveList(clientObject.getId());
    }

    @Override
    protected void updateClientObject(TSyncData syncData, TListClient clientList, TListServer serverList) {

        applyPropertiesToClientData(serverList, clientList);
        clientList.setServerVersion(serverList.getVersion());
        clientList.setServerId(serverList.getId());

        listManager.saveList(clientList.getId());

    }



    protected abstract void applyPropertiesToClientData(TListServer source, TListClient target);

    protected abstract void applyPropertiesToServerData(TListClient source,  TListServer target);

}
