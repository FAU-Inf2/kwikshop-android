package de.fau.cs.mad.kwikshop.android.model.synchronization;

import android.content.Context;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.SessionHandler;
import de.fau.cs.mad.kwikshop.android.model.messages.SynchronizationEvent;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
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
    private final Context context;

    @Inject
    public CompositeSynchronizer(ListSynchronizer<ShoppingList, ShoppingListServer> shoppingListSynchronizer,
                                 ListSynchronizer<Recipe, RecipeServer> recipeSynchronizer,
                                 ResourceProvider resourceProvider,
                                 Context context) {

        if(shoppingListSynchronizer == null) {
            throw new ArgumentNullException("shoppingListSynchronizer");
        }

        if(recipeSynchronizer == null) {
            throw new ArgumentNullException("recipeSynchronizer");
        }

        if(resourceProvider == null) {
            throw new ArgumentNullException("resourceProvider");
        }

        if(context == null) {
            throw new ArgumentNullException("context");
        }

        this.shoppingListSynchronizer = shoppingListSynchronizer;
        this.recipeSynchronizer = recipeSynchronizer;
        this.resourceProvider = resourceProvider;
        this.context = context;
    }


    public void synchronize() {

        //check if the user is logged in. otherwise we cannot sync
        Context applicationContext = context.getApplicationContext();
        if(!SessionHandler.isAuthenticated(applicationContext)) {
            return;
        }

        //check if synchronization is even enabled
        if(!SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.ENABLE_SYNCHRONIZATION, true, context)) {
            return;
        }


        post(SynchronizationEvent.CreateStartedMessage());

        post(SynchronizationEvent.CreateProgressMessage(resourceProvider.getString(R.string.synchronizing_shoppingLists)));

        try {
            shoppingListSynchronizer.synchronize();
        } catch (SynchronizationException ex) {

            String message = String.format("%s\n\n%s", resourceProvider.getString(R.string.error_synchronizing_shoppingLists), ex.toString());
            post(SynchronizationEvent.CreateFailedMessage(message));
            return;
        }

        post(SynchronizationEvent.CreateProgressMessage(resourceProvider.getString(R.string.synchronizing_recipes)));
        try {
            recipeSynchronizer.synchronize();
        }catch (SynchronizationException ex) {

            String message = String.format("%s\n\n%s", resourceProvider.getString(R.string.error_synchronizing_recipes), ex.toString());
            post(SynchronizationEvent.CreateFailedMessage(message));
            return;
        }

        post(SynchronizationEvent.CreateCompletedMessage());

    }



    private void post(SynchronizationEvent event) {
        EventBus.getDefault().post(event);
    }

}
