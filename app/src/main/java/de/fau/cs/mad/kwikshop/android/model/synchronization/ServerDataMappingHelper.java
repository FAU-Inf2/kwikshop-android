package de.fau.cs.mad.kwikshop.android.model.synchronization;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObjectServer;

public class ServerDataMappingHelper<TListClient extends DomainListObject, TListServer extends DomainListObjectServer> {

    private final SimpleStorage<Group> groupStorage;
    private final SimpleStorage<Unit> unitStorage;
    private final SimpleStorage<LastLocation> locationStorage;


    @Inject
    public ServerDataMappingHelper(SimpleStorage<Group> groupStorage, SimpleStorage<Unit> unitStorage,
                                   SimpleStorage<LastLocation> locationStorage) {

        if(groupStorage == null) {
            throw new ArgumentNullException("groupStorage");
        }

        if(unitStorage == null) {
            throw new ArgumentNullException("unitStorage");
        }

        if(locationStorage == null) {
            throw new ArgumentNullException("locationStorage");
        }

        this.groupStorage = groupStorage;
        this.unitStorage = unitStorage;
        this.locationStorage = locationStorage;
    }



    public Group getClientGroup(ItemSyncData<TListClient, TListServer> itemSyncData, Group serverGroup) {

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

    public Unit getClientUnit(ItemSyncData<TListClient, TListServer> itemSyncData, Unit serverUnit) {

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

    public LastLocation getClientLocation(ItemSyncData<TListClient, TListServer> itemSyncData, LastLocation serverLocation) {

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
