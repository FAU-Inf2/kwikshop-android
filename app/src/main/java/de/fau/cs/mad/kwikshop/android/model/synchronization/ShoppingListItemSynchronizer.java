package de.fau.cs.mad.kwikshop.android.model.synchronization;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.restclient.ListClient;
import de.fau.cs.mad.kwikshop.android.restclient.RestClientFactory;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.Unit;

public class ShoppingListItemSynchronizer extends ItemSynchronizer<ShoppingList, ShoppingListServer> {


    private final RestClientFactory clientFactory;
    private ListClient<ShoppingListServer> client;

    @Inject
    public ShoppingListItemSynchronizer(RestClientFactory clientFactory, ListManager<ShoppingList> listManager,
                                        SimpleStorage<Group> groupStorage, SimpleStorage<Unit> unitStorage,
                                        SimpleStorage<LastLocation> locationStorage,
                                        ServerDataMappingHelper<ShoppingList, ShoppingListServer> mappingHelper) {

        super(listManager, groupStorage, unitStorage, locationStorage, mappingHelper);

        if(clientFactory == null) {
            throw new ArgumentNullException("clientFactory");
        }

        this.clientFactory = clientFactory;
    }

    @Override
    protected ListClient<ShoppingListServer> getApiClient() {
        if(client == null) {
            client = clientFactory.getShoppingListClient();
        }

        return client;
    }
}
