package de.fau.cs.mad.kwikshop.android.model.synchronization;

import java.util.Collection;


import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.restclient.ListClient;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.ItemViewModel;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObjectServer;
import retrofit.RetrofitError;

public abstract class ItemSynchronizer<TListClient extends DomainListObject,
                              TListServer extends DomainListObjectServer>
        extends
            SyncStrategy<ItemViewModel, ItemViewModel, ItemSyncData<TListClient, TListServer>> {


    private final ListManager<TListClient> listManager;
    private final SimpleStorage<Group> groupStorage;
    private final SimpleStorage<Unit> unitStorage;
    private final SimpleStorage<LastLocation> locationStorage;

    private ItemSyncData<TListClient, TListServer> syncData;
    private int serverListId = -1;
    private int clientListId = -1;


    public ItemSynchronizer(ListManager<TListClient> listManager,
                            SimpleStorage<Group> groupStorage,
                            SimpleStorage<Unit> unitStorage,
                            SimpleStorage<LastLocation> locationStorage) {


        if(listManager == null) {
            throw new ArgumentNullException("listManager");
        }

        if(groupStorage == null) {
            throw new ArgumentNullException("groupStorage");
        }

        if(unitStorage == null) {
            throw new ArgumentNullException("unitStorage");
        }

        this.listManager = listManager;
        this.groupStorage = groupStorage;
        this.unitStorage = unitStorage;
        this.locationStorage = locationStorage;
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
    protected int getClientId(ItemSyncData<TListClient, TListServer> itemSyncData, ItemViewModel item) {
        return item.getId();
    }

    @Override
    protected int getServerId(ItemSyncData<TListClient, TListServer> itemSyncData, ItemViewModel item) {
        return item.getServerId();
    }

    @Override
    protected Collection<ItemViewModel> getClientObjects(ItemSyncData<TListClient, TListServer> itemSyncData) {
        return itemSyncData.getClientItems(clientListId);
    }

    @Override
    protected Collection<ItemViewModel> getServerObjects(ItemSyncData<TListClient, TListServer> itemSyncData) {
        return itemSyncData.getServerItems(serverListId).values();
    }

    @Override
    protected boolean clientObjectExistsOnServer(ItemSyncData<TListClient, TListServer> itemSyncData, ItemViewModel object) {
        return object.getServerId() > 0 && !clientObjectDeletedOnServer(itemSyncData, object);
    }

    @Override
    protected boolean serverObjectExistsOnClient(ItemSyncData<TListClient, TListServer> itemSyncData, ItemViewModel object) {
        return itemSyncData.getClientItemsByServerId(clientListId).containsKey(object.getServerId());
    }

    @Override
    protected boolean clientObjectDeletedOnServer(ItemSyncData<TListClient, TListServer> itemSyncData, ItemViewModel object) {

        //item deleted on server if either the containing list or the Item itself has been deleted
        return itemSyncData.getDeletedListsServerByServerId().containsKey(serverListId) ||
               itemSyncData.getDeletedItemsServer(serverListId).containsKey(object.getServerId());
    }

    @Override
    protected boolean serverObjectDeletedOnClient(ItemSyncData<TListClient, TListServer> itemSyncData, ItemViewModel object) {
        return itemSyncData.getDeletedItemsClientByServerId(serverListId) != null &&
               itemSyncData.getDeletedItemsClientByServerId(serverListId).containsKey(object.getServerId());
    }

    @Override
    protected ItemViewModel getClientObjectForServerObject(ItemSyncData<TListClient, TListServer> itemSyncData, ItemViewModel serverObject) {
        return itemSyncData.getClientItemsByServerId(clientListId).get(serverObject.getServerId());
    }

    @Override
    protected ItemViewModel getServerObjectForClientObject(ItemSyncData<TListClient, TListServer> itemSyncData, ItemViewModel clientObject) {
        return itemSyncData.getServerItems(serverListId).get(clientObject.getServerId());
    }

    @Override
    protected boolean serverObjectModified(ItemSyncData<TListClient, TListServer> itemSyncData, ItemViewModel serverItem) {

        int serverItemId = serverItem.getServerId();
        int lastSeenVersion;

        ItemViewModel clientItem = getClientObjectForServerObject(itemSyncData, serverItem);

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
    protected boolean clientObjectModified(ItemSyncData<TListClient, TListServer> itemSyncData, ItemViewModel clientItem) {
        return clientItem.getModifiedSinceLastSync();
    }

    @Override
    protected void deleteServerObject(ItemSyncData<TListClient, TListServer> itemSyncData, ItemViewModel serverItem) {

        try {
            getApiClient().deleteListItem(serverListId, serverItem.getServerId());
        } catch (RetrofitError ex) {
            throw new SynchronizationException(ex, "Error deleting item %s in list %s", serverListId, serverItem.getServerId());
        }
    }

    @Override
    protected void deleteClientObject(ItemSyncData<TListClient, TListServer> itemSyncData, ItemViewModel clientItem) {

        listManager.deleteItem(clientListId, clientItem.getId());
    }

    @Override
    protected void createServerObject(ItemSyncData<TListClient, TListServer> itemSyncData, ItemViewModel clientItem) {

        unitStorage.refresh(clientItem.getUnit());
        groupStorage.refresh(clientItem.getGroup());
        locationStorage.refresh(clientItem.getLocation());

        ItemViewModel serverItem;

        try {
            serverItem = getApiClient().createItem(serverListId, clientItem);
        } catch (RetrofitError ex) {
            throw new SynchronizationException(ex, "Error creating item on server in list %s", serverListId);
        }

        clientItem.setServerId(serverItem.getServerId());
        clientItem.setVersion(serverItem.getVersion());

        if(clientItem.getUnit() != null && serverItem.getUnit() != null) {
            clientItem.getUnit().setServerId(serverItem.getUnit().getServerId());
            unitStorage.updateItem(clientItem.getUnit());
        }
        if(clientItem.getGroup() != null && serverItem.getGroup() != null) {
            clientItem.getGroup().setServerId(serverItem.getGroup().getServerId());
            groupStorage.updateItem(clientItem.getGroup());
        }
        if(clientItem.getLocation() != null && serverItem.getLocation() != null) {
            clientItem.getLocation().setServerId(serverItem.getLocation().getServerId());
            locationStorage.updateItem(clientItem.getLocation());
        }

        listManager.saveListItem(clientListId, clientItem);

    }

    @Override
    protected void createClientObject(ItemSyncData<TListClient, TListServer> itemSyncData, ItemViewModel serverItem) {

        serverItem.setGroup(getClientGroup(itemSyncData, serverItem.getGroup()));
        serverItem.setUnit(getClientUnit(itemSyncData, serverItem.getUnit()));
        serverItem.setLocation(getClientLocation(itemSyncData, serverItem.getLocation()));

        listManager.addListItem(clientListId, serverItem);
    }

    @Override
    protected void updateServerObject(ItemSyncData<TListClient, TListServer> itemSyncData, ItemViewModel clientItem, ItemViewModel serverItem) {

        applyPropertiesToServerData(itemSyncData, clientItem, serverItem);

        try {
            serverItem = getApiClient().updateItem(serverListId, serverItem.getServerId(), serverItem);
        } catch (RetrofitError ex) {
            throw new SynchronizationException(ex, "Error updating item %s in list %s on server", serverItem.getServerId(), serverListId);
        }

        clientItem.setVersion(serverItem.getVersion());

        if(clientItem.getUnit() != null && serverItem.getUnit() != null) {
            clientItem.getUnit().setServerId(serverItem.getUnit().getServerId());
            unitStorage.updateItem(clientItem.getUnit());
        }
        if(clientItem.getGroup() != null && serverItem.getGroup() != null) {
            clientItem.getGroup().setServerId(serverItem.getGroup().getServerId());
            groupStorage.updateItem(clientItem.getGroup());
        }
        if(clientItem.getLocation() != null && serverItem.getLocation() != null) {
            clientItem.getLocation().setServerId(serverItem.getLocation().getServerId());
            locationStorage.updateItem(clientItem.getLocation());
        }

        listManager.saveListItem(clientListId, clientItem);
    }

    @Override
    protected void updateClientObject(ItemSyncData<TListClient, TListServer> itemSyncData, ItemViewModel clientItem, ItemViewModel serverItem) {

        applyPropertiesToClientData(itemSyncData, serverItem, clientItem);
        listManager.saveListItem(clientListId, clientItem);
    }



    protected void applyPropertiesToClientData(ItemSyncData<TListClient, TListServer> itemSyncData, ItemViewModel serverItem, ItemViewModel clientItem) {

        applyPropertiesCommon(serverItem, clientItem);

        clientItem.setServerId(serverItem.getServerId());
        clientItem.setVersion(serverItem.getVersion());

        //Group, Unit, Location
        clientItem.setGroup(getClientGroup(itemSyncData, serverItem.getGroup()));
        clientItem.setUnit(getClientUnit(itemSyncData, serverItem.getUnit()));
        clientItem.setLocation(getClientLocation(itemSyncData, serverItem.getLocation()));
    }

    protected void applyPropertiesToServerData(ItemSyncData<TListClient, TListServer> itemSyncData, ItemViewModel source, ItemViewModel target){

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

    private void applyPropertiesCommon(ItemViewModel source, ItemViewModel target) {

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

    private Group getClientGroup(ItemSyncData<TListClient, TListServer> itemSyncData, Group serverGroup) {

        if(serverGroup == null) {
            return null;
        }

        int serverGroupId = serverGroup.getServerId();

        //check if a group on the client exists that corresponds to the specified server group
        // (lookup by server id)
        if(itemSyncData.getGroupsByServerId().containsKey(serverGroupId)) {
            return itemSyncData.getGroupsByServerId().get(serverGroupId);
        } else {
            //group not found => create new client-side group
            groupStorage.addItem(serverGroup);
            itemSyncData.getGroupsByServerId().put(serverGroupId, serverGroup);
            return serverGroup;
        }
    }

    private Unit getClientUnit(ItemSyncData<TListClient, TListServer> itemSyncData, Unit serverUnit) {

        if(serverUnit == null) {
            return null;
        }

        int serverUnitId = serverUnit.getServerId();

        if(itemSyncData.getUnitsByServerId().containsKey(serverUnitId)) {
            return itemSyncData.getUnitsByServerId().get(serverUnitId);
        } else {
            unitStorage.addItem(serverUnit);
            itemSyncData.getUnitsByServerId().put(serverUnitId, serverUnit);
            return serverUnit;
        }
    }

    private LastLocation getClientLocation(ItemSyncData<TListClient, TListServer> itemSyncData, LastLocation serverLocation) {

        if(serverLocation == null) {
            return null;
        }

        int serverLocationId = serverLocation.getServerId();

        if(itemSyncData.getLocationsByServerId().containsKey(serverLocationId)) {
            return itemSyncData.getLocationsByServerId().get(serverLocationId);
        } else {
            locationStorage.addItem(serverLocation);
            itemSyncData.getLocationsByServerId().put(serverLocationId, serverLocation);
            return serverLocation;
        }
    }
}

