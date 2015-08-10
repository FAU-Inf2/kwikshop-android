package de.fau.cs.mad.kwikshop.android.model.synchronization;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.restclient.ListClient;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.Unit;

public class ShoppingListItemSynchronizer extends ItemSynchronizer<ShoppingList, ShoppingListServer> {


    @Inject
    public ShoppingListItemSynchronizer(ListClient<ShoppingListServer> listClient, ListManager<ShoppingList> listManager, SimpleStorage<Group> groupStorage, SimpleStorage<Unit> unitStorage, SimpleStorage<LastLocation> locationStorage) {
        super(listClient, listManager, groupStorage, unitStorage, locationStorage);
    }
}
