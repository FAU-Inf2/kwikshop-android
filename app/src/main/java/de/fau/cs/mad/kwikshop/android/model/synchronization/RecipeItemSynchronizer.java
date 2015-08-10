package de.fau.cs.mad.kwikshop.android.model.synchronization;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.restclient.ListClient;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.RecipeServer;
import de.fau.cs.mad.kwikshop.common.Unit;

public class RecipeItemSynchronizer extends ItemSynchronizer<Recipe, RecipeServer> {

    @Inject
    public RecipeItemSynchronizer(ListClient<RecipeServer> listClient, ListManager<Recipe> listManager, SimpleStorage<Group> groupStorage, SimpleStorage<Unit> unitStorage, SimpleStorage<LastLocation> locationStorage) {
        super(listClient, listManager, groupStorage, unitStorage, locationStorage);
    }
}
