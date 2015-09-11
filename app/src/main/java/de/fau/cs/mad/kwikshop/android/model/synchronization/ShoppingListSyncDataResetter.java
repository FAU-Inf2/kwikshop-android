package de.fau.cs.mad.kwikshop.android.model.synchronization;

import android.content.Context;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.model.ConnectionInfoStorage;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.Unit;

public class ShoppingListSyncDataResetter extends SyncDataResetter<ShoppingList> {


    @Inject
    public ShoppingListSyncDataResetter(Context context, ResourceProvider resourceProvider,
                                        ListManager<ShoppingList> listManager,
                                        ConnectionInfoStorage connectionInfoStorage,
                                        SimpleStorage<Unit> unitStorage,
                                        SimpleStorage<Group> groupStorage,
                                        SimpleStorage<LastLocation> locationStorage) {

        super(context, resourceProvider, listManager, connectionInfoStorage, unitStorage, groupStorage, locationStorage);
    }


    @Override
    protected void resetSyncData(ShoppingList list) {

        list.setOwnerId(null);
        super.resetSyncData(list);

    }
}
