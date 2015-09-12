package de.fau.cs.mad.kwikshop.android.model.synchronization;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.restclient.LeaseResource;
import de.fau.cs.mad.kwikshop.android.restclient.RestClientFactory;
import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.SessionHandler;
import de.fau.cs.mad.kwikshop.android.model.messages.SynchronizationEvent;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.RecipeServer;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.SynchronizationLease;
import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.common.sorting.ItemOrderWrapper;
import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

/**
 * Central class coordinating syncing of all synced data
 */
public class CompositeSynchronizer {

    private final ConditionalSyncDataResetter syncDataResetter;
    private final ListSynchronizer<ShoppingList, ShoppingListServer> shoppingListSynchronizer;
    private final ListSynchronizer<Recipe, RecipeServer> recipeSynchronizer;
    private final RestClientFactory restClientFactory;
    private final ResourceProvider resourceProvider;
    private final Context context;

    private final SimpleStorage<BoughtItem> boughtItemStorage;

    @Inject
    public CompositeSynchronizer(ConditionalSyncDataResetter syncDataResetter,
                                 ListSynchronizer<ShoppingList, ShoppingListServer> shoppingListSynchronizer,
                                 ListSynchronizer<Recipe, RecipeServer> recipeSynchronizer,
                                 SimpleStorage<BoughtItem> boughtItemStorage,
                                 RestClientFactory restClientFactory,
                                 ResourceProvider resourceProvider,
                                 Context context) {

        if(syncDataResetter == null) {
            throw new ArgumentNullException("syncDataResetter");
        }

        if(shoppingListSynchronizer == null) {
            throw new ArgumentNullException("shoppingListSynchronizer");
        }

        if(recipeSynchronizer == null) {
            throw new ArgumentNullException("recipeSynchronizer");
        }

        if(boughtItemStorage == null) {
            throw new ArgumentNullException("boughtItemStorage");
        }

        if(restClientFactory == null) {
            throw new ArgumentNullException("restClientFactory");
        }

        if(resourceProvider == null) {
            throw new ArgumentNullException("resourceProvider");
        }

        if(context == null) {
            throw new ArgumentNullException("context");
        }

        this.syncDataResetter = syncDataResetter;
        this.shoppingListSynchronizer = shoppingListSynchronizer;
        this.recipeSynchronizer = recipeSynchronizer;
        this.boughtItemStorage = boughtItemStorage;
        this.restClientFactory = restClientFactory;
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


        // start synchronization
        post(SynchronizationEvent.CreateStartedMessage());

        // reset all local server data if the used server or user has changed
        syncDataResetter.resetSyncDataIfNecessary();


        // get a synchronization lease

        LeaseResource leaseCLient = restClientFactory.getLeaseClient();
        post(SynchronizationEvent.CreateProgressMessage(resourceProvider.getString(R.string.aquiring_lease)));

        SynchronizationLease[] lease = new SynchronizationLease[1];
        try {
            lease[0] = leaseCLient.getSynchronizationLeaseSynchronously();
        } catch (RetrofitError ex) {

            post(SynchronizationEvent.CreateFailedMessage(resourceProvider.getString(R.string.error_aquiring_lease)));
            return;
        }

        // update the lease in the background
        UpdateLeaseRunnable updateLeaseRunnable = new UpdateLeaseRunnable(leaseCLient, lease);
        Thread updateLeaseThread = new Thread(updateLeaseRunnable);
        updateLeaseThread.start();

        // synchronize shopping lists and recipes
        boolean success = synchronizeShoppingLists() && synchronizeRecipes();

        sendBoughtItems();

        // stop updating the lease
        updateLeaseRunnable.cancel();
        updateLeaseThread.interrupt();

        try {
            leaseCLient.removeSynchronizationLeaseSynchronously(lease[0].getId());
        } catch (RetrofitError ex) {
            // error while deleting lease can be ignore
        }

        if(success) {
            post(SynchronizationEvent.CreateCompletedMessage());
        }

    }


    private void sendBoughtItems() {
        try {
            List<BoughtItem> syncableBoughtItems = new ArrayList<>();
            for(BoughtItem boughtItem: boughtItemStorage.getItems()) {
                if(boughtItem.isSync()) {
                    syncableBoughtItems.add(boughtItem);
                    boughtItemStorage.deleteSingleItem(boughtItem);
                }
            }

            if(syncableBoughtItems.size() == 0)
                return;

            // Sort the List by Date
            Collections.sort(syncableBoughtItems);

            ItemOrderWrapper itemOrderWrapper = new ItemOrderWrapper(syncableBoughtItems);
            restClientFactory.getShoppingListClient().postItemOrder(itemOrderWrapper);
        } catch (Exception e) {

        }
    }


    private boolean synchronizeShoppingLists() {

        post(SynchronizationEvent.CreateProgressMessage(resourceProvider.getString(R.string.synchronizing_shoppingLists)));

        try {

            shoppingListSynchronizer.synchronize();

        } catch (Exception ex) {

            Log.e("KwikShop-Sync", "Exception in ShoppingList synchronization", ex);

            String message = String.format("%s\n\n%s", resourceProvider.getString(R.string.error_synchronizing_shoppingLists), ex.toString());
            post(SynchronizationEvent.CreateFailedMessage(message));
            return false;
        }

        return true;
    }

    private boolean synchronizeRecipes() {

        post(SynchronizationEvent.CreateProgressMessage(resourceProvider.getString(R.string.synchronizing_recipes)));

        try {

            recipeSynchronizer.synchronize();

        } catch (Exception ex) {

            Log.e("KwikShop-Sync", "Exception in Recipe synchronization", ex);

            String message = String.format("%s\n\n%s", resourceProvider.getString(R.string.error_synchronizing_recipes), ex.toString());
            post(SynchronizationEvent.CreateFailedMessage(message));
            return false;
        }

        return true;
    }

    private void post(SynchronizationEvent event) {
        EventBus.getDefault().post(event);
    }


    private class UpdateLeaseRunnable implements Runnable {

        private LeaseResource leaseClient;
        private final SynchronizationLease[] lease;
        private boolean cancelled = false;


        public UpdateLeaseRunnable(LeaseResource leaseClient, SynchronizationLease[] lease) {

            if(leaseClient == null) {
                throw new ArgumentNullException("leaseClient");
            }
            if(lease == null) {
                throw new ArgumentNullException("lease");
            }
            if(lease.length != 1) {
                throw new IllegalArgumentException("lease must be an array of size 1");
            }
            if(lease[0] == null) {
                throw new ArgumentNullException("lease[0]");
            }

            this.leaseClient = leaseClient;
            this.lease = lease;
        }

        @Override
        public void run() {

            while(!cancelled) {

                try {
                    Thread.sleep(getSleepTime());
                } catch (InterruptedException e) {
                    return;
                }

                if(cancelled) {
                    return;
                }

                try {
                    SynchronizationLease updatedLease = leaseClient.extendSynchronizationLeaseSynchronously(lease[0].getId());

                    synchronized (lease) {
                        lease[0] = updatedLease;
                    }

                } catch (RetrofitError ex) {

                }
            }


        }


        public void cancel() {
            this.cancelled = true;
        }

        private long getSleepTime() {
            return Math.max(0, (lease[0].getExpirationTime().getTime() - new Date().getTime()) / 2);
        }

    }

}
