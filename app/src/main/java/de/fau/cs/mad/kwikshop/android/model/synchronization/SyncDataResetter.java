package de.fau.cs.mad.kwikshop.android.model.synchronization;

import android.content.Context;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.common.ConnectionInfo;
import de.fau.cs.mad.kwikshop.android.model.ConnectionInfoStorage;
import de.fau.cs.mad.kwikshop.android.model.SessionHandler;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;

public abstract class SyncDataResetter<TList extends DomainListObject> {

    private final Context context;
    private final ResourceProvider resourceProvider;
    private final ListManager<TList> listManager;
    private final ConnectionInfoStorage connectionInfoStorage;
    private final SimpleStorage<Unit> unitStorage;
    private final SimpleStorage<Group> groupStorage;
    private final SimpleStorage<LastLocation> locationStorage;



    public SyncDataResetter(Context context, ResourceProvider resourceProvider,
                            ListManager<TList> listManager, ConnectionInfoStorage connectionInfoStorage,
                            SimpleStorage<Unit> unitStorage,
                            SimpleStorage<Group> groupStorage,
                            SimpleStorage<LastLocation> locationStorage) {

        if(context == null) {
            throw new ArgumentNullException("context");
        }

        if(resourceProvider == null) {
            throw new ArgumentNullException("resourceProvider");
        }

        if(listManager == null) {
            throw new ArgumentNullException("listManager");
        }

        if(connectionInfoStorage == null) {
            throw new ArgumentNullException("connectionInfoStorage");
        }

        if(unitStorage == null) {
            throw new ArgumentNullException("unitStorage");
        }

        if(groupStorage == null) {
            throw new ArgumentNullException("groupStorage");
        }

        if(locationStorage == null) {
            throw new ArgumentNullException("locationStorage");
        }

        this.context = context;
        this.resourceProvider = resourceProvider;
        this.listManager = listManager;
        this.connectionInfoStorage = connectionInfoStorage;
        this.unitStorage = unitStorage;
        this.groupStorage = groupStorage;
        this.locationStorage = locationStorage;
    }



    public void resetSyncDataIfNecessary() {

        ConnectionInfo existingInfo = connectionInfoStorage.getConnectionInfo();
        ConnectionInfo currentInfo = getCurrrentConnectionInfo();

        if(!currentInfo.equals(existingInfo)) {
            resetSyncData();
            connectionInfoStorage.setConnectionInfo(currentInfo);
        }

    }



    protected void resetSyncData() {

        for(TList list : listManager.getLists()) {
            if(list.getServerId() != 0) {
                resetSyncData(list);
            }
        }

        resetUnitSyncData();

        resetGroupSyncData();

        resetLocationSyncData();
    }

    protected void resetSyncData(TList list) {

        list.setServerId(0);
        list.setServerVersion(0);

        for(Item item : listManager.getListItems(list.getId())) {
            item.setServerId(0);
            item.setVersion(0);
        }

        listManager.saveList(list.getId());
    }

    protected void resetUnitSyncData() {

        for(Unit u : unitStorage.getItems()) {

            if(u.getServerId() != 0) {
                u.setServerId(0);
                unitStorage.updateItem(u);
            }
        }
    }

    protected void resetGroupSyncData() {

        for(Group g : groupStorage.getItems()) {

            if(g.getServerId() != 0) {
                g.setServerId(0);
                groupStorage.updateItem(g);
            }
        }
    }

    protected void resetLocationSyncData() {

        for(LastLocation l : locationStorage.getItems()) {

            if(l.getServerId() != 0) {
                l.setServerId(0);
                locationStorage.updateItem(l);
            }
        }
    }



    private ConnectionInfo getCurrrentConnectionInfo() {

        String userId = SessionHandler.getSessionUser(context);
        if(userId == null) {
            userId = "";
        }
        String apiEndPoint = SharedPreferencesHelper.loadString(SharedPreferencesHelper.API_ENDPOINT,
                                                                resourceProvider.getString(R.string.API_HOST),
                                                                context);

        return new ConnectionInfo(userId, apiEndPoint);
    }

}
