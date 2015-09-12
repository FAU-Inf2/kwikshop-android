package de.fau.cs.mad.kwikshop.android.model.synchronization;

import android.content.Context;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.common.ConnectionInfo;
import de.fau.cs.mad.kwikshop.android.model.ConnectionInfoStorage;
import de.fau.cs.mad.kwikshop.android.model.SessionHandler;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.model.synchronization.background.SyncDataResetter;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;

public abstract class AbstractSyncDataResetter<TList extends DomainListObject> implements SyncDataResetter<TList> {

    private final ListManager<TList> listManager;
    private final SimpleStorage<Unit> unitStorage;
    private final SimpleStorage<Group> groupStorage;
    private final SimpleStorage<LastLocation> locationStorage;



    public AbstractSyncDataResetter(ListManager<TList> listManager,
                                    SimpleStorage<Unit> unitStorage,
                                    SimpleStorage<Group> groupStorage,
                                    SimpleStorage<LastLocation> locationStorage) {

        if(listManager == null) {
            throw new ArgumentNullException("listManager");
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


        this.listManager = listManager;
        this.unitStorage = unitStorage;
        this.groupStorage = groupStorage;
        this.locationStorage = locationStorage;
    }






    public void resetSyncData() {

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



}
