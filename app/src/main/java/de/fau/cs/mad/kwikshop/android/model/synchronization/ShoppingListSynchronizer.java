package de.fau.cs.mad.kwikshop.android.model.synchronization;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.DeletedItem;
import de.fau.cs.mad.kwikshop.android.model.DeletedList;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.restclient.ListClient;
import de.fau.cs.mad.kwikshop.android.restclient.RestClientFactory;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.conversion.ObjectConverter;

public class ShoppingListSynchronizer extends ListSynchronizer<ShoppingList, ShoppingListServer> {

    private final RestClientFactory clientFactory;
    private ListClient<ShoppingListServer> client;

    @Inject
    public ShoppingListSynchronizer(RestClientFactory clientFactory,
                                    ObjectConverter<ShoppingList, ShoppingListServer> clientToServerObjectConverter,
                                    ListManager<ShoppingList> listManager,
                                    SimpleStorage<DeletedList> deletedListStorage,
                                    SimpleStorage<DeletedItem> deletedItemStorage,
                                    SimpleStorage<Group> groupStorage,
                                    SimpleStorage<Unit> unitStorage,
                                    SimpleStorage<LastLocation> locationStorage,
                                    ItemSynchronizer<ShoppingList, ShoppingListServer> itemSynchronizer,
                                    ServerDataMappingHelper<ShoppingList, ShoppingListServer> mappingHelper) {

        super(clientToServerObjectConverter, listManager, deletedListStorage,
              deletedItemStorage, groupStorage, unitStorage, locationStorage,
              itemSynchronizer, mappingHelper);

        if(clientFactory == null) {
            throw new ArgumentNullException("clientFactory");
        }

        this.clientFactory = clientFactory;

    }



    @Override
    protected void applyPropertiesToClientData(ShoppingListServer source, ShoppingList target) {

        target.setName(source.getName());
        target.setSortTypeInt(source.getSortTypeInt());
        target.setLastModifiedDate(source.getLastModifiedDate());
        target.setOwnerId(source.getOwnerId());

        target.setServerVersion(source.getVersion());
        target.setServerId(source.getId());
    }

    @Override
    protected void applyPropertiesToServerData(ShoppingList source, ShoppingListServer target) {

        target.setName(source.getName());
        target.setSortTypeInt(source.getSortTypeInt());
        target.setLastModifiedDate(source.getLastModifiedDate());
    }

    @Override
    protected ListClient<ShoppingListServer> getApiClient() {
        if(client == null) {
            client = clientFactory.getShoppingListClient();
        }
        return client;
    }
}
