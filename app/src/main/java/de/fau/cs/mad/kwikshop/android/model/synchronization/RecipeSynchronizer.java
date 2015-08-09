package de.fau.cs.mad.kwikshop.android.model.synchronization;

import de.fau.cs.mad.kwikshop.android.model.DeletedList;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.restclient.ListClient;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.RecipeServer;
import de.fau.cs.mad.kwikshop.common.conversion.ObjectConverter;

public class RecipeSynchronizer extends ListSynchronizer<Recipe, RecipeServer> {

    public RecipeSynchronizer(ObjectConverter<Recipe, RecipeServer> clientToServerObjectConverter,
                              ListClient<RecipeServer> listClient,
                              ListManager<Recipe> listManager,
                              SimpleStorage<DeletedList> deletedListStorage) {

        super(clientToServerObjectConverter, listClient, listManager, deletedListStorage);

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
