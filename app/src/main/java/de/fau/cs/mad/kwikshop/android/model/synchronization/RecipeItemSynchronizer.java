package de.fau.cs.mad.kwikshop.android.model.synchronization;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.restclient.ListClient;
import de.fau.cs.mad.kwikshop.android.restclient.RestClientFactory;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.RecipeServer;
import de.fau.cs.mad.kwikshop.common.Unit;

public class RecipeItemSynchronizer extends ItemSynchronizer<Recipe, RecipeServer> {

    private final RestClientFactory clientFactory;
    private ListClient<RecipeServer> client;

    @Inject
    public RecipeItemSynchronizer(RestClientFactory clientFactory, ListManager<Recipe> listManager, SimpleStorage<Group> groupStorage, SimpleStorage<Unit> unitStorage, SimpleStorage<LastLocation> locationStorage) {
        super(listManager, groupStorage, unitStorage, locationStorage);

        if(clientFactory == null) {
            throw new ArgumentNullException("clientFactory");
        }

        this.clientFactory = clientFactory;
    }

    @Override
    protected synchronized ListClient<RecipeServer> getApiClient() {

        if(client == null) {
            client = clientFactory.getRecipeClient();
        }

        return client;
    }
}
