package de.fau.cs.mad.kwikshop.android.model.synchronization;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.model.DeletedItem;
import de.fau.cs.mad.kwikshop.android.model.DeletedList;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.restclient.ListClient;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.RecipeServer;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.conversion.ObjectConverter;

public class RecipeSynchronizer extends ListSynchronizer<Recipe, RecipeServer> {

    @Inject
    public RecipeSynchronizer(ObjectConverter<Recipe, RecipeServer> clientToServerObjectConverter,
                              ListClient<RecipeServer> listClient,
                              ListManager<Recipe> listManager,
                              SimpleStorage<DeletedList> deletedListStorage,
                              SimpleStorage<DeletedItem> deletedItemStorage,
                              SimpleStorage<Group> groupStorage,
                              SimpleStorage<Unit> unitStorage,
                              SimpleStorage<LastLocation> locationStorage,
                              ItemSynchronizer<Recipe, RecipeServer> itemSynchronizer) {

        super(clientToServerObjectConverter, listClient, listManager, deletedListStorage,
              deletedItemStorage, groupStorage, unitStorage, locationStorage, itemSynchronizer);

    }



    @Override
    protected void applyPropertiesToClientData(RecipeServer source, Recipe target) {

        target.setName(source.getName());
        target.setScaleFactor(source.getScaleFactor());
        target.setScaleName(source.getScaleName());
        target.setLastModifiedDate(source.getLastModifiedDate());

        target.setServerVersion(source.getVersion());
        target.setServerId(source.getId());
    }

    @Override
    protected void applyPropertiesToServerData(Recipe source, RecipeServer target) {

        target.setName(source.getName());
        target.setScaleFactor(source.getScaleFactor());
        target.setScaleName(source.getScaleName());
        target.setLastModifiedDate(source.getLastModifiedDate());

    }

}
