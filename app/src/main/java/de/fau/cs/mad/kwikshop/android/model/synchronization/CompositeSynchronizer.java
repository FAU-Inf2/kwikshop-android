package de.fau.cs.mad.kwikshop.android.model.synchronization;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.messages.SynchronizationEvent;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.RecipeServer;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.greenrobot.event.EventBus;

/**
 * Synchronizer that synchronized both shopping lists and recipes
 * and posts progress updates to EventBus
 */
public class CompositeSynchronizer {

    private final ListSynchronizer<ShoppingList, ShoppingListServer> shoppingListSynchronizer;
    private final ListSynchronizer<Recipe, RecipeServer> recipeSynchronizer;
    private final ResourceProvider resourceProvider;

    @Inject
    public CompositeSynchronizer(ListSynchronizer<ShoppingList, ShoppingListServer> shoppingListSynchronizer,
                                 ListSynchronizer<Recipe, RecipeServer> recipeSynchronizer,
                                 ResourceProvider resourceProvider) {

        if(shoppingListSynchronizer == null) {
            throw new ArgumentNullException("shoppingListSynchronizer");
        }

        if(recipeSynchronizer == null) {
            throw new ArgumentNullException("recipeSynchronizer");
        }

        if(resourceProvider == null) {
            throw new ArgumentNullException("resourceProvider");
        }

        this.shoppingListSynchronizer = shoppingListSynchronizer;
        this.recipeSynchronizer = recipeSynchronizer;
        this.resourceProvider = resourceProvider;
    }


    public void synchronize() {

        post(SynchronizationEvent.CreateStartedMessage());

        post(SynchronizationEvent.CreateProgressMessage(resourceProvider.getString(R.string.synchronizing_shoppingLists)));
        shoppingListSynchronizer.synchronize();

        post(SynchronizationEvent.CreateProgressMessage(resourceProvider.getString(R.string.synchronizing_recipes)));
        recipeSynchronizer.synchronize();

        post(SynchronizationEvent.CreateCompletedMessage());

    }



    private void post(SynchronizationEvent event) {
        EventBus.getDefault().post(event);
    }

}
