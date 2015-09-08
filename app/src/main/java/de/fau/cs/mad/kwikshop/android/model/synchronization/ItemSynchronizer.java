package de.fau.cs.mad.kwikshop.android.model.synchronization;

import java.util.Collection;


import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.restclient.ListClient;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObjectServer;
import retrofit.RetrofitError;

public abstract class ItemSynchronizer<TListClient extends DomainListObject,
                              TListServer extends DomainListObjectServer>
        extends
            SyncStrategy<Item, Item, ItemSyncData<TListClient, TListServer>> {


    private final ListManager<TListClient> listManager;
    private final SimpleStorage<Group> groupStorage;
    private final SimpleStorage<Unit> unitStorage;
    private final SimpleStorage<LastLocation> locationStorage;
    private final ServerDataMappingHelper<TListClient, TListServer> mappingHelper;

    private ItemSyncData<TListClient, TListServer> syncData;
    private int serverListId = -1;
    private int clientListId = -1;


    public ItemSynchronizer(ListManager<TListClient> listManager,
                            SimpleStorage<Group> groupStorage,
                            SimpleStorage<Unit> unitStorage,
                            SimpleStorage<LastLocation> locationStorage,
                            ServerDataMappingHelper<TListClient, TListServer> mappingHelper) {


        if(listManager == null) {
            throw new ArgumentNullException("listManager");
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

        if(mappingHelper == null) {
            throw new ArgumentNullException("mappingHelper");
        }

        this.listManager = listManager;
        this.groupStorage = groupStorage;
        this.unitStorage = unitStorage;
        this.locationStorage = locationStorage;
        this.mappingHelper = mappingHelper;
    }



    protected abstract ListClient<TListServer> getApiClient();



    public void synchronize(int clientListId, int serverListId, ItemSyncData<TListClient, TListServer> syncData) {

        this.clientListId = clientListId;
        this.serverListId = serverListId;
        this.syncData = syncData;

        synchronize();

    }

    @Override
    protected ItemSyncData<TListClient, TListServer> initializeSyncData() {

        if(serverListId == -1 || clientListId == -1) {
            throw new UnsupportedOperationException("List Ids not set");
        }

        return syncData;
    }

    @Override
    protected void cleanUpSyncData(ItemSyncData<TListClient, TListServer> itemSyncData) {

        this.serverListId = -1;
        this.clientListId = -1;
    }

    @Override
    protected int getClientId(ItemSyncData<TListClient, TListServer> itemSyncData, Item item) {
        return item.getId();
    }

    @Override
    protected int getServerId(ItemSyncData<TListClient, TListServer> itemSyncData, Item item) {
        return item.getServerId();
    }

    @Override
    protected Collection<Item> getClientObjects(ItemSyncData<TListClient, TListServer> itemSyncData) {
        return itemSyncData.getClientItems(clientListId);
    }

    @Override
    protected Collection<Item> getServerObjects(ItemSyncData<TListClient, TListServer> itemSyncData) {
        return itemSyncData.getServerItems(serverListId).values();
    }

    @Override
    protected boolean clientObjectExistsOnServer(ItemSyncData<TListClient, TListServer> itemSyncData, Item object) {
        return object.getServerId() > 0 && !clientObjectDeletedOnServer(itemSyncData, object);
    }

    @Override
    protected boolean serverObjectExistsOnClient(ItemSyncData<TListClient, TListServer> itemSyncData, Item object) {
        return itemSyncData.getClientItemsByServerId(clientListId).containsKey(object.getServerId());
    }

    @Override
    protected boolean clientObjectDeletedOnServer(ItemSyncData<TListClient, TListServer> itemSyncData, Item object) {

        //item deleted on server if either the containing list or the Item itself has been deleted
        return itemSyncData.getDeletedListsServerByServerId().containsKey(serverListId) ||
               itemSyncData.getDeletedItemsServer(serverListId).containsKey(object.getServerId());
    }

    @Override
    protected boolean serverObjectDeletedOnClient(ItemSyncData<TListClient, TListServer> itemSyncData, Item object) {
        return itemSyncData.getDeletedItemsClientByServerId(serverListId) != null &&
               itemSyncData.getDeletedItemsClientByServerId(serverListId).containsKey(object.getServerId());
    }

    @Override
    protected Item getClientObjectForServerObject(ItemSyncData<TListClient, TListServer> itemSyncData, Item serverObject) {
        return itemSyncData.getClientItemsByServerId(clientListId).get(serverObject.getServerId());
    }

    @Override
    protected Item getServerObjectForClientObject(ItemSyncData<TListClient, TListServer> itemSyncData, Item clientObject) {
        return itemSyncData.getServerItems(serverListId).get(clientObject.getServerId());
    }

    @Override
    protected boolean serverObjectModified(ItemSyncData<TListClient, TListServer> itemSyncData, Item serverItem) {

        int serverItemId = serverItem.getServerId();
        int lastSeenVersion;

        Item clientItem = getClientObjectForServerObject(itemSyncData, serverItem);

        if(syncData.getDeletedItemsClientByServerId(serverListId) != null &&
           syncData.getDeletedItemsClientByServerId(serverListId).containsKey(serverItemId)) {

            lastSeenVersion = syncData.getDeletedItemsClientByServerId(serverListId).get(serverItemId).getServerVersion();

        } else if(clientItem != null) {
            lastSeenVersion = clientItem.getVersion();
        } else {
            return true;
        }

        return lastSeenVersion != serverItem.getVersion();
    }

    @Override
    protected boolean clientObjectModified(ItemSyncData<TListClient, TListServer> itemSyncData, Item clientItem) {
        return clientItem.getModifiedSinceLastSync();
    }

    @Override
    protected void deleteServerObject(ItemSyncData<TListClient, TListServer> itemSyncData, Item serverItem) {

        try {
            getApiClient().deleteListItem(serverListId, serverItem.getServerId());
        } catch (RetrofitError ex) {
            throw new SynchronizationException(ex, "Error deleting item %s in list %s", serverListId, serverItem.getServerId());
        }
    }

    @Override
    protected void deleteClientObject(ItemSyncData<TListClient, TListServer> itemSyncData, Item clientItem) {

        listManager.deleteItem(clientListId, clientItem.getId());
    }

    @Override
    protected void createServerObject(ItemSyncData<TListClient, TListServer> itemSyncData, Item clientItem) {

        unitStorage.refresh(clientItem.getUnit());
        groupStorage.refresh(clientItem.getGroup());
        locationStorage.refresh(clientItem.getLocation());

        Item serverItem;

        try {
            serverItem = getApiClient().createItem(serverListId, clientItem);
        } catch (RetrofitError ex) {
            throw new SynchronizationException(ex, "Error creating item on server in list %s", serverListId);
        }

        clientItem.setServerId(serverItem.getServerId());
        clientItem.setVersion(serverItem.getVersion());

        Unit clientUnit = clientItem.getUnit();
        if(clientUnit != null) {
            int serverId = serverItem.getUnit().getServerId();
            clientUnit.setServerId(serverId);
            unitStorage.updateItem(clientItem.getUnit());
            syncData.getUnitsByServerId().put(serverId, clientUnit);
        }

        Group clientGroup = clientItem.getGroup();
        if(clientGroup != null) {
            int serverId = serverItem.getGroup().getServerId();
            clientGroup.setServerId(serverId);
            groupStorage.updateItem(clientItem.getGroup());
            syncData.getGroupsByServerId().put(serverId, clientGroup);
        }

        LastLocation clientLocation = clientItem.getLocation();
        if(clientLocation != null) {
            int serverId = serverItem.getLocation().getServerId();
            clientLocation.setServerId(serverId);
            locationStorage.updateItem(clientLocation);
            syncData.getLocationsByServerId().put(serverId, clientLocation);
        }

        listManager.saveListItem(clientListId, clientItem);

    }

    @Override
    protected void createClientObject(ItemSyncData<TListClient, TListServer> itemSyncData, Item serverItem) {

        serverItem.setGroup(mappingHelper.getClientGroup(itemSyncData, serverItem.getGroup()));
        serverItem.setUnit(mappingHelper.getClientUnit(itemSyncData, serverItem.getUnit()));
        serverItem.setLocation(mappingHelper.getClientLocation(itemSyncData, serverItem.getLocation()));

        listManager.addListItem(clientListId, serverItem);
    }

    @Override
    protected void updateServerObject(ItemSyncData<TListClient, TListServer> itemSyncData, Item clientItem, Item serverItem) {

        applyPropertiesToServerData(itemSyncData, clientItem, serverItem);

        try {
            serverItem = getApiClient().updateItem(serverListId, serverItem.getServerId(), serverItem);
        } catch (RetrofitError ex) {
            throw new SynchronizationException(ex, "Error updating item %s in list %s on server", serverItem.getServerId(), serverListId);
        }

        clientItem.setVersion(serverItem.getVersion());

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
            int serverId = serverItem.getLocation().getServerId();
            clientLocation.setServerId(serverId);
            locationStorage.updateItem(clientLocation);
            syncData.getLocationsByServerId().put(serverId, clientLocation);
        }

        listManager.saveListItem(clientListId, clientItem);
    }

    @Override
    protected void updateClientObject(ItemSyncData<TListClient, TListServer> itemSyncData, Item clientItem, Item serverItem) {

        applyPropertiesToClientData(itemSyncData, serverItem, clientItem);
        listManager.saveListItem(clientListId, clientItem);
    }



    protected void applyPropertiesToClientData(ItemSyncData<TListClient, TListServer> itemSyncData, Item serverItem, Item clientItem) {

        applyPropertiesCommon(serverItem, clientItem);

        clientItem.setServerId(serverItem.getServerId());
        clientItem.setVersion(serverItem.getVersion());

        //Group, Unit, Location
        clientItem.setGroup(mappingHelper.getClientGroup(itemSyncData, serverItem.getGroup()));
        clientItem.setUnit(mappingHelper.getClientUnit(itemSyncData, serverItem.getUnit()));
        clientItem.setLocation(mappingHelper.getClientLocation(itemSyncData, serverItem.getLocation()));
    }

    protected void applyPropertiesToServerData(ItemSyncData<TListClient, TListServer> itemSyncData, Item source, Item target){

        applyPropertiesCommon(source, target);

        unitStorage.refresh(source.getUnit());
        groupStorage.refresh(source.getGroup());
        locationStorage.refresh(source.getLocation());

        //no special treatment necessary for Group, Unit and location. The appropriate
        // server instances will be created implicitly if they do not yet exist
        target.setGroup(source.getGroup());
        target.setUnit(source.getUnit());
        target.setLocation(source.getLocation());
    }

    private void applyPropertiesCommon(Item source, Item target) {

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
        target.setPredefinedId(source.getPredefinedId());

    }





}

