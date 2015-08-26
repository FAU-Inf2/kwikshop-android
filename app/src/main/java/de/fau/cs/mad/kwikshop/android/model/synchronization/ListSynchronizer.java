package de.fau.cs.mad.kwikshop.android.model.synchronization;


import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
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
import de.fau.cs.mad.kwikshop.common.interfaces.DomainObject;
import retrofit.RetrofitError;

public abstract class ListSynchronizer<TListClient extends DomainListObject,
                                       TListServer extends DomainListObjectServer>
        extends
            SyncStrategy<TListClient, TListServer, ItemSyncData<TListClient, TListServer>> {



    private final ObjectConverter<TListClient, TListServer> clientToServerObjectConverter;
    private final ListManager<TListClient> listManager;
    private final SimpleStorage<DeletedList> deletedListStorage;
    private final SimpleStorage<DeletedItem> deletedItemStorage;
    private final SimpleStorage<Group> groupStorage;
    private final SimpleStorage<Unit> unitStorage;
    private final SimpleStorage<LastLocation> locationStorage;
    private final ItemSynchronizer<TListClient, TListServer> itemSynchronizer;
    private final ServerDataMappingHelper<TListClient, TListServer> mappingHelper;


    public ListSynchronizer(ObjectConverter<TListClient, TListServer> clientToServerObjectConverter,
                            ListManager<TListClient> listManager,
                            SimpleStorage<DeletedList> deletedListStorage,
                            SimpleStorage<DeletedItem> deletedItemStorage,
                            SimpleStorage<Group> groupStorage,
                            SimpleStorage<Unit> unitStorage,
                            SimpleStorage<LastLocation> locationStorage,
                            ItemSynchronizer<TListClient, TListServer> itemSynchronizer,
                            ServerDataMappingHelper<TListClient, TListServer> mappingHelper) {

        if(clientToServerObjectConverter == null) {
            throw new ArgumentNullException("clientToServerObjectConverter");
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

        if(mappingHelper == null) {
            throw new ArgumentNullException("mappingHelper");
        }

        this.clientToServerObjectConverter = clientToServerObjectConverter;
        this.listManager = listManager;
        this.deletedListStorage = deletedListStorage;
        this.deletedItemStorage = deletedItemStorage;
        this.groupStorage = groupStorage;
        this.unitStorage = unitStorage;
        this.locationStorage = locationStorage;
        this.itemSynchronizer = itemSynchronizer;
        this.mappingHelper = mappingHelper;
    }



    @Override
    protected ItemSyncData<TListClient, TListServer> initializeSyncData() {

        // load local data
        Collection<TListClient> clientLists = listManager.getLists();
        Collection<DeletedList> clientDeletedLists = deletedListStorage.getItems();
        Collection<DeletedItem> clientDeletedItems = deletedItemStorage.getItems();
        Collection<LastLocation> clientLocations = locationStorage.getItems();



        //load all the data we need for syncing from the server
        Collection<TListServer> serverLists;
        Collection<DeletionInfo> serverDeletedLists;
        Map<Integer, Collection<DeletionInfo>> serverDeletedItems = new HashMap<>();
        Map<Integer, Map<Integer, Item>> allServerListItems = new HashMap<>();
        try {

            serverLists = getApiClient().getLists();
            serverDeletedLists = getApiClient().getDeletedLists();

            for(TListServer serverList : serverLists) {
                serverDeletedItems.put(serverList.getId(), getApiClient().getDeletedListItems(serverList.getId()));
                allServerListItems.put(serverList.getId(), CollectionUtilities.toItemMapByServerId(getApiClient().getListItems(serverList.getId())));
            }

        } catch (RetrofitError ex) {
            throw new SynchronizationException(ex, "Error getting data from server for syncing");
        }

        LinkedList<Unit> serverUnits = new LinkedList<>();
        LinkedList<Group> serverGroups = new LinkedList<>();

        for(Map<Integer, Item> map : allServerListItems.values()) {
            for(Item item : map.values()) {
                if(item.getUnit() != null) {
                    serverUnits.add(item.getUnit());
                }
                if(item.getGroup() != null) {
                    serverGroups.add(item.getGroup());
                }
            }
        }

        // assign the server id for predefined data so it does not get duplicated
        // for every device that syncs with the server

        linkPredefinedLists(clientLists, serverLists, serverDeletedLists, allServerListItems, serverDeletedItems);

        Collection<Unit> clientUnits = linkPredefinedUnits(serverUnits);


        Collection<Group> clientGroups = linkPredefinedGroups(serverGroups);


        return new ItemSyncData<>(clientLists, clientDeletedLists, clientDeletedItems,
                                  serverLists, allServerListItems, serverDeletedLists, serverDeletedItems,
                                  clientGroups, clientUnits, clientLocations);
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
        return clientList.getServerId() > 0 && !clientObjectDeletedOnServer(syncData, clientList);
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
        getApiClient().deleteList(serverId);


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

        try {
            serverList = getApiClient().createList(serverList);

        } catch (RetrofitError ex) {
            throw new SynchronizationException(ex, "Error creating list on server");
        }

        clientList.setServerId(serverList.getId());

        //also upload items
        for(Item clientItem : clientList.getItems()) {

            unitStorage.refresh(clientItem.getUnit());
            groupStorage.refresh(clientItem.getGroup());
            locationStorage.refresh(clientItem.getLocation());

            Item serverItem = getApiClient().createItem(serverList.getId(), clientItem);

            Unit clientUnit = clientItem.getUnit();
            if(clientUnit != null) {
                int serverId = serverItem.getUnit().getServerId();
                clientUnit.setServerId(serverId);
                unitStorage.updateItem(clientUnit);
                syncData.getUnitsByServerId().put(serverId, clientUnit);
            }

            Group clientGroup = clientItem.getGroup();
            if(clientGroup != null) {
                int serverId = serverItem.getGroup().getServerId();
                clientGroup.setServerId(serverId);
                groupStorage.updateItem(clientGroup);
                syncData.getGroupsByServerId().put(serverId, clientGroup);
            }

            LastLocation clientLocation = clientItem.getLocation();
            if(clientLocation != null) {
                int serverId = serverItem.getServerId();
                clientLocation.setServerId(serverId);
                locationStorage.updateItem(clientItem.getLocation());
                syncData.getLocationsByServerId().put(serverId, clientLocation);
            }

            clientItem.setServerId(serverItem.getServerId());
            clientItem.setVersion(serverItem.getVersion());
            listManager.saveListItem(clientList.getId(), clientItem);
        }

        //get server list again to get the new version
        try {
            serverList = getApiClient().getLists(serverList.getId());
        } catch (RetrofitError ex) {
            throw new SynchronizationException(ex, "Could not get list %s from server", serverList.getId());
        }

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

        List<Item> serverItems;
        try {

            serverItems = getApiClient().getListItems(serverList.getId());

        } catch (RetrofitError ex) {

            throw new SynchronizationException(ex, "Could not get items for list %s", serverList.getId());
        }

        for(Item item : serverItems) {

            item.setGroup(mappingHelper.getClientGroup(syncData, item.getGroup()));
            item.setUnit(mappingHelper.getClientUnit(syncData, item.getUnit()));
            item.setLocation(mappingHelper.getClientLocation(syncData, item.getLocation()));

            listManager.addListItem(clientListId, item);
        }


    }

    @Override
    protected void updateServerObject(ItemSyncData<TListClient, TListServer> syncData, TListClient clientList, TListServer serverList) {

        applyPropertiesToServerData(clientList, serverList);

        try {
            serverList = getApiClient().updateList(serverList.getId(), serverList);
        } catch (RetrofitError ex) {
            throw new SynchronizationException(ex, "Could not update list %s on server", serverList.getId());
        }

        itemSynchronizer.synchronize(clientList.getId(), serverList.getId(), syncData);

        try {
            serverList = getApiClient().getLists(serverList.getId());
        } catch (RetrofitError ex) {
            throw new SynchronizationException(ex, "Could not get list %s from server", serverList.getId());
        }
        clientList.setServerVersion(serverList.getVersion());
            listManager.saveList(clientList.getId());
    }

    @Override
    protected void updateClientObject(ItemSyncData<TListClient, TListServer> syncData, TListClient clientList, TListServer serverList) {

        applyPropertiesToClientData(serverList, clientList);
        clientList.setServerId(serverList.getId());

        itemSynchronizer.synchronize(clientList.getId(), serverList.getId(), syncData);

        serverList = getApiClient().getLists(serverList.getId());
        clientList.setServerVersion(serverList.getVersion());
        listManager.saveList(clientList.getId());

    }



    protected abstract void applyPropertiesToClientData(TListServer source, TListClient target);

    protected abstract void applyPropertiesToServerData(TListClient source,  TListServer target);

    protected abstract ListClient<TListServer> getApiClient();



    private void linkPredefinedLists(Collection<TListClient> clientLists,
                                     Collection<TListServer> serverLists,
                                     Collection<DeletionInfo> serverDeletedLists,
                                     Map<Integer, Map<Integer, Item>> allServerListItems,
                                     Map<Integer, Collection<DeletionInfo>> serverDeletedItems) {

        Map<Integer, Integer> predefinedIdServerIdMap = new HashMap<>();
        for(TListServer serverList : serverLists) {
            if(serverList.getPredefinedId() > 0) {
                predefinedIdServerIdMap.put(serverList.getPredefinedId(), serverList.getId());
            }
        }

        addDeletionInfosToPredefinedIdMap(serverDeletedLists, predefinedIdServerIdMap);

        for(TListClient clientList : clientLists) {

            int serverListId = clientList.getServerId();
            int predefinedListId = clientList.getPredefinedId();

            // if the local list does not yet have a serverId and it's a predefined list
            // assign the correct serverId before syncing
            if(serverListId == 0 && predefinedListId > 0) {

                if(predefinedIdServerIdMap.containsKey(predefinedListId)) {

                    // assign server id for list
                    serverListId = predefinedIdServerIdMap.get(predefinedListId);
                    clientList.setServerId(serverListId);
                    listManager.saveListWithoutModificationFlag(clientList.getId());

                    if(allServerListItems.containsKey(serverListId)) {
                        linkPredefinedItems(
                                clientList,
                                allServerListItems.get(serverListId).values(),
                                serverDeletedItems.get(serverListId));
                    }
                }
            }
        }
    }

    private void linkPredefinedItems(TListClient clientList,
                                     Collection<Item> serverListItems,
                                     Collection<DeletionInfo> serverDeletedItems) {

        Map<Integer, Integer> predefinedIdMap = new HashMap<>();
        for(Item item : serverListItems) {
            if(item.getPredefinedId() > 0) {
                predefinedIdMap.put(item.getPredefinedId(), item.getServerId());
            }
        }

        addDeletionInfosToPredefinedIdMap(serverDeletedItems, predefinedIdMap);

        for(Item item : clientList.getItems()) {

            int predefinedId = item.getPredefinedId();
            int serverItemId = item.getServerId();

            if(serverItemId == 0 && predefinedId > 0 ) {

                if(predefinedIdMap.containsKey(item.getPredefinedId())) {
                    item.setServerId(predefinedIdMap.get(predefinedId));
                    listManager.saveListItemWithoutModificationFlag(clientList.getId(), item);
                }
            }
        }
    }

    private Collection<Unit> linkPredefinedUnits(Collection<Unit> serverUnits) {

        Map<Integer, Unit> serverUnitsByPredefinedId = CollectionUtilities.toMapByPredefinedId(serverUnits);
        List<Unit> clientUnits = unitStorage.getItems();


        for(Unit clientUnit : clientUnits) {

            int predefinedId = clientUnit.getPredefinedId();

            if(clientUnit.getServerId() == 0 &&
                predefinedId > 0 &&
                serverUnitsByPredefinedId.containsKey(predefinedId)) {

                clientUnit.setServerId(serverUnitsByPredefinedId.get(predefinedId).getServerId());
                unitStorage.updateItem(clientUnit);
            }
        }

        return clientUnits;
    }

    private Collection<Group> linkPredefinedGroups(Collection<Group> serverGroups) {

        Map<Integer, Group> serverGroupsByPredefinedId = CollectionUtilities.toMapByPredefinedId(serverGroups);

        List<Group> clientGroups = groupStorage.getItems();
        for(Group clientGroup : clientGroups) {

            int predefinedId = clientGroup.getPredefinedId();

            if(clientGroup.getServerId() == 0 &&
                predefinedId > 0 &&
                serverGroupsByPredefinedId.containsKey(predefinedId)) {

                clientGroup.setServerId(serverGroupsByPredefinedId.get(predefinedId).getServerId());
                groupStorage.updateItem(clientGroup);
            }
        }

        return clientGroups;
    }


    private void addDeletionInfosToPredefinedIdMap(Collection<DeletionInfo> deletionInfos, Map<Integer, Integer> predefinedIdMap) {

        if(deletionInfos == null) {
            return;
        }

        for(DeletionInfo deletionInfo : deletionInfos) {
            if(deletionInfo.getPredefinedId() > 0) {
                predefinedIdMap.put(deletionInfo.getPredefinedId(), deletionInfo.getId());
            }
        }
    }


}
