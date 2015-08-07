package de.fau.cs.mad.kwikshop.android.model.synchronization;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.model.DeletedList;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.restclient.ListClient;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.conversion.ObjectConverter;

public class ShoppingListSynchronizer extends ListSynchronizer<ShoppingList, ShoppingListServer> {


    @Inject
    public ShoppingListSynchronizer(ObjectConverter<ShoppingList, ShoppingListServer> clientToServerObjectConverter,
                                    ListClient<ShoppingListServer> listClient,
                                    ListManager<ShoppingList> listManager,
                                    SimpleStorage<DeletedList> deletedListStorage) {

        super(clientToServerObjectConverter, listClient, listManager, deletedListStorage);
    }

    @Override
    protected void applyPropertiesToClientData(ShoppingListServer source, ShoppingList target) {

        target.setName(target.getName());
        target.setSortTypeInt(target.getSortTypeInt());
        target.setLocation(target.getLocation());
        target.setLastModifiedDate(target.getLastModifiedDate());

        target.setServerVersion(source.getVersion());
        target.setServerId(source.getId());
    }

    @Override
    protected void applyPropertiesToServerData(ShoppingList source, ShoppingListServer target) {

        target.setName(target.getName());
        target.setSortTypeInt(target.getSortTypeInt());
        target.setLocation(target.getLocation());
        target.setLastModifiedDate(target.getLastModifiedDate());
    }
}
