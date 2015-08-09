package de.fau.cs.mad.kwikshop.android.model.synchronization;

import java.util.Collection;

import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.restclient.ListClient;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObjectServer;

public class ItemSynchronizer<TListClient extends DomainListObject,
                              TListServer extends DomainListObjectServer>
        extends
            SyncStrategy<Item, Item, ItemSyncData<TListClient, TListServer>> {


    private final ItemSyncData<TListClient, TListServer> syncData;
    private final ListClient<TListServer> listClient;
    private final ListManager<TListClient> listManager;
    private final SimpleStorage<Group> groupStorage;
    private final SimpleStorage<Unit> unitStorage;
    private final SimpleStorage<LastLocation> locationStorage;

    private int serverListId = -1;
    private int clientListId = -1;


    public ItemSynchronizer(ItemSyncData<TListClient, TListServer> syncData,
                            ListClient<TListServer> listClient,
                            ListManager<TListClient> listManager,
                            SimpleStorage<Group> groupStorage,
                            SimpleStorage<Unit> unitStorage,
                            SimpleStorage<LastLocation> locationStorage) {

        if(syncData == null) {
            throw new ArgumentNullException("syncData");
        }

        if(listClient == null) {
            throw new ArgumentNullException("listClient");
        }

        if(listManager == null) {
            throw new ArgumentNullException("listManager");
        }

        if(groupStorage == null) {
            throw new ArgumentNullException("groupStorage");
        }

        if(unitStorage == null) {
            throw new ArgumentNullException("unitStorage");
        }

        this.syncData = syncData;
        this.listClient = listClient;
        this.listManager = listManager;
        this.groupStorage = groupStorage;
        this.unitStorage = unitStorage;
        this.locationStorage = locationStorage;
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
        return object.getServerId() > 0;
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
        return itemSyncData.getDeletedItemsClientByServerId(clientListId).containsKey(object.getServerId());
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
        Item clientItem = getClientObjectForServerObject(itemSyncData, serverItem);
        return clientItem.getVersion() != serverItem.getVersion();
    }

    @Override
    protected boolean clientObjectModified(ItemSyncData<TListClient, TListServer> itemSyncData, Item clientItem) {
        return clientItem.getModifiedSinceLastSync();
    }

    @Override
    protected void deleteServerObject(ItemSyncData<TListClient, TListServer> itemSyncData, Item serverItem) {

        listClient.deleteListItem(serverListId, serverItem.getServerId());
    }

    @Override
    protected void deleteClientObject(ItemSyncData<TListClient, TListServer> itemSyncData, Item clientItem) {

        listManager.deleteItem(clientListId, clientItem.getId());
    }

    @Override
    protected void createServerObject(ItemSyncData<TListClient, TListServer> itemSyncData, Item clientItem) {

        Item serverItem = listClient.createItem(serverListId, clientItem);

        clientItem.setServerId(clientItem.getServerId());
        clientItem.setVersion(serverItem.getVersion());
        if(clientItem.getUnit() != null && serverItem.getUnit() != null) {
            clientItem.getUnit().setServerId(serverItem.getUnit().getServerId());
        }
        if(clientItem.getGroup() != null && serverItem.getGroup() != null) {
            clientItem.getGroup().setServerId(serverItem.getGroup().getServerId());
        }
        if(clientItem.getLocation() != null && serverItem.getLocation() != null) {
            clientItem.getLocation().setServerId(serverItem.getLocation().getServerId());
        }

        listManager.saveListItem(clientListId, clientItem);

    }

    @Override
    protected void createClientObject(ItemSyncData<TListClient, TListServer> itemSyncData, Item serverObject) {

        listManager.addListItem(clientListId, serverObject);
    }

    @Override
    protected void updateServerObject(ItemSyncData<TListClient, TListServer> itemSyncData, Item clientItem, Item serverItem) {

        applyPropertiesToServerData(itemSyncData, clientItem, serverItem);

        serverItem = listClient.updateItem(serverListId, serverItem.getServerId(), serverItem);

        clientItem.setVersion(serverItem.getVersion());

        if(clientItem.getUnit() != null && serverItem.getUnit() != null) {
            clientItem.getUnit().setServerId(serverItem.getUnit().getServerId());
        }
        if(clientItem.getGroup() != null && serverItem.getGroup() != null) {
            clientItem.getGroup().setServerId(serverItem.getGroup().getServerId());
        }
        if(clientItem.getLocation() != null && serverItem.getLocation() != null) {
            clientItem.getLocation().setServerId(serverItem.getLocation().getServerId());
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
        clientItem.setGroup(getClientGroup(itemSyncData, serverItem.getGroup()));
        clientItem.setUnit(getClientUnit(itemSyncData, serverItem.getUnit()));
        clientItem.setLocation(getClientLocation(itemSyncData, serverItem.getLocation()));
    }

    protected void applyPropertiesToServerData(ItemSyncData<TListClient, TListServer> itemSyncData, Item source, Item target){

        applyPropertiesCommon(source, target);

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
