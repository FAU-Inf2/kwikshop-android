package de.fau.cs.mad.kwikshop.android.model.synchronization;

import java.util.Collection;

import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.restclient.ListClient;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObjectServer;

public class ItemSynchronizer<TListClient extends DomainListObject,
                              TListServer extends DomainListObjectServer>
        extends
            SyncStrategy<Item, Item, ItemSyncData<TListClient, TListServer>> {


    private final ItemSyncData<TListClient, TListServer> syncData;
    private final ListClient<TListServer> listClient;
    private final ListManager<TListClient> listManager;

    private int serverListId = -1;
    private int clientListId = -1;


    public ItemSynchronizer(ItemSyncData<TListClient, TListServer> syncData, ListClient<TListServer> listClient, ListManager<TListClient> listManager) {

        if(syncData == null) {
            throw new ArgumentNullException("syncData");
        }

        if(listClient == null) {
            throw new ArgumentNullException("listClient");
        }

        if(listManager == null) {
            throw new ArgumentNullException("listManager");
        }

        this.syncData = syncData;
        this.listClient = listClient;
        this.listManager = listManager;
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
        listManager.saveListItem(clientListId, clientItem);

    }

    @Override
    protected void createClientObject(ItemSyncData<TListClient, TListServer> itemSyncData, Item serverObject) {

        listManager.addListItem(clientListId, serverObject);
    }

    @Override
    protected void updateServerObject(ItemSyncData<TListClient, TListServer> itemSyncData, Item clientItem, Item serverItem) {

        applyPropertiesToServerData(clientItem, serverItem);

        serverItem = listClient.updateItem(serverListId, serverItem.getServerId(), serverItem);

        clientItem.setVersion(serverItem.getVersion());
        listManager.saveListItem(clientListId, clientItem);
    }

    @Override
    protected void updateClientObject(ItemSyncData<TListClient, TListServer> itemSyncData, Item clientItem, Item serverItem) {

        applyPropertiesToClientData(serverItem, clientItem);
        listManager.saveListItem(clientListId, clientItem);
    }



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

}
