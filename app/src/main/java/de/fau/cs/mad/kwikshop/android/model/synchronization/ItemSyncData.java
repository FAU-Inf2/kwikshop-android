package de.fau.cs.mad.kwikshop.android.model.synchronization;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.DeletedItem;
import de.fau.cs.mad.kwikshop.android.model.DeletedList;
import de.fau.cs.mad.kwikshop.android.util.CollectionUtilities;
import de.fau.cs.mad.kwikshop.common.DeletionInfo;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObjectServer;

public class ItemSyncData<TListClient extends DomainListObject,
                          TListServer extends DomainListObjectServer>
        extends
            ListSyncData<TListClient, TListServer> {

    private final Map<Integer, Map<Integer, Item>> serverListItems;
    private final Map<Integer, Collection<DeletionInfo>> allDeletedItemsServer;
    private final Map<Integer, Map<Integer, DeletedItem>> allDeletedItemsClientByServerId;

    private final Map<Integer, Group> groupsByServerId;
    private final Map<Integer, Unit> unitsByServerId;
    private final Map<Integer, LastLocation> locationsByServerId;

    //caches
    Map<Integer, Map<Integer, Item>> clientItemsByServerIdCache = new HashMap<>();
    Map<Integer, Map<Integer, DeletionInfo>> deletedItemsServerCache = new HashMap<>();


    public ItemSyncData(Collection<TListClient> clientLists,
                        Collection<DeletedList> deletedListsClient,
                        Collection<DeletedItem> allDeletedItemsClient,
                        Collection<TListServer> serverLists,
                        Map<Integer, Map<Integer, Item>> serverListItems,
                        Collection<DeletionInfo> deletedListsServer,
                        Map<Integer, Collection<DeletionInfo>> deletedItemsServer,
                        Collection<Group> groups,
                        Collection<Unit> units,
                        Collection<LastLocation> locations) {

        super(clientLists, deletedListsClient, serverLists, deletedListsServer);

        if(allDeletedItemsClient == null) {
            throw new ArgumentNullException("allDeletedItemsClient");
        }

        if(serverListItems == null) {
            throw new ArgumentNullException("serverListItems");
        }

        if(deletedItemsServer == null) {
            throw new ArgumentNullException("allDeletedItemsServer");
        }

        this.allDeletedItemsServer = deletedItemsServer;
        this.serverListItems = serverListItems;
        this.allDeletedItemsClientByServerId = CollectionUtilities.toDeletedItemMapByServerId(allDeletedItemsClient);


        this.groupsByServerId = CollectionUtilities.toGroupMapByServerId(groups);
        this.unitsByServerId = CollectionUtilities.toUnitMapByServerId(units);
        this.locationsByServerId = CollectionUtilities.toLocationMapByServerId(locations);
    }



    public Collection<Item> getClientItems(int clientListId) {

        return getClientLists().get(clientListId).getItems();
    }

    public Map<Integer, Item> getServerItems(int serverListId) {

        return serverListItems.get(serverListId);
    }


    public Map<Integer, Item> getClientItemsByServerId(int clientListId){

//        if(!clientItemsByServerIdCache.containsKey(clientListId)) {
//
//            clientItemsByServerIdCache.put(
//                    clientListId,
//                    CollectionUtilities.toItemMapByServerId(getClientItems(clientListId)));
//        }
//
//        return clientItemsByServerIdCache.get(clientListId);
        return CollectionUtilities.toItemMapByServerId(getClientItems(clientListId));
    }

    public Map<Integer, DeletionInfo> getDeletedItemsServer(int serverListId) {

//        if(deletedItemsServerCache.containsKey(serverListId)) {
//
//            deletedItemsServerCache.put(
//                    serverListId,
//                    CollectionUtilities.toMap(allDeletedItemsServer.get(serverListId)));
//        }
//
//        return deletedItemsServerCache.get(serverListId);
        return CollectionUtilities.toMap(allDeletedItemsServer.get(serverListId));


    }

    public Map<Integer, DeletedItem> getDeletedItemsClientByServerId(int clientListId) {
        return allDeletedItemsClientByServerId.get(clientListId);
    }

    public Map<Integer, Unit> getUnitsByServerId() {
        return unitsByServerId;
    }

    public Map<Integer, Group> getGroupsByServerId() {
        return groupsByServerId;
    }

    public Map<Integer, LastLocation> getLocationsByServerId() {
        return locationsByServerId;
    }

}
