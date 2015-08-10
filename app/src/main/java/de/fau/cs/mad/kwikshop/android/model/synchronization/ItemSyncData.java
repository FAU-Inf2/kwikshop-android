package de.fau.cs.mad.kwikshop.android.model.synchronization;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
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

    private int lastClientListId = -1;
    private int lastServerListId = -1;
    private Map<Integer, Item> serverItems;
    private Map<Integer, Item> clientItemsByServerId;
    private Map<Integer, DeletionInfo> deletedItemsServer;

    private final Map<Integer, Map<Integer, Item>> serverListItems;
    private final Map<Integer, Collection<DeletionInfo>> allDeletedItemsServer;
    private final Map<Integer, Map<Integer, DeletedItem>> allDeletedItemsClientByServerId;

    private final Map<Integer, Group> groupsByServerId;
    private final Map<Integer, Unit> unitsByServerId;
    private final Map<Integer, LastLocation> locationsByServerId;


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

        if(lastServerListId == serverListId) {
            return serverItems;
        }

        lastServerListId = serverListId;
        serverItems = serverListItems.get(serverListId);

        return serverItems;
    }


    public Map<Integer, Item> getClientItemsByServerId(int clientListId){

        if(clientListId == lastClientListId) {
            return  clientItemsByServerId;
        } else {

            lastClientListId = clientListId;

            clientItemsByServerId = CollectionUtilities.toItemMapByServerId(getClientItems(clientListId));
            return clientItemsByServerId;
        }
    }

    public Map<Integer, DeletionInfo> getDeletedItemsServer(int serverListId) {

        if(lastServerListId == serverListId) {
            return deletedItemsServer;
        }

        lastServerListId = serverListId;

        deletedItemsServer = CollectionUtilities.toMap(allDeletedItemsServer.get(serverListId));
        return deletedItemsServer;
    }

    public Map<Integer, DeletedItem> getDeletedItemsClientByServerId(int clientListId) {

        lastClientListId = clientListId;
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
