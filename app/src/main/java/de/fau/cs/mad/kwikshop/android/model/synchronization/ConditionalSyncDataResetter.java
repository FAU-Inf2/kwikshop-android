package de.fau.cs.mad.kwikshop.android.model.synchronization;

import android.content.Context;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.common.ConnectionInfo;
import de.fau.cs.mad.kwikshop.android.model.ConnectionInfoStorage;
import de.fau.cs.mad.kwikshop.android.model.SessionHandler;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.ShoppingList;

public class ConditionalSyncDataResetter {

    private final Context context;
    private final ResourceProvider resourceProvider;
    private final ConnectionInfoStorage connectionInfoStorage;
    private final SyncDataResetter<ShoppingList> shoppingListSyncDataResetter;
    private final SyncDataResetter<Recipe> recipeSyncDataResetter;



    @Inject
    public ConditionalSyncDataResetter(Context context, ResourceProvider resourceProvider,
                                       ConnectionInfoStorage connectionInfoStorage,
                                       SyncDataResetter<ShoppingList> shoppingListSyncDataResetter,
                                       SyncDataResetter<Recipe> recipeSyncDataResetter) {

        if(context == null) {
            throw new ArgumentNullException("context");
        }

        if(resourceProvider == null) {
            throw new ArgumentNullException("resourceProvider");
        }

        if(connectionInfoStorage == null) {
            throw new ArgumentNullException("connectionInfoStorage");
        }

        if(shoppingListSyncDataResetter == null) {
            throw new ArgumentNullException("shoppingListSyncDataResetter");
        }

        if(recipeSyncDataResetter == null) {
            throw new ArgumentNullException("recipeSyncDataResetter");
        }

        this.context = context;
        this.resourceProvider = resourceProvider;
        this.connectionInfoStorage = connectionInfoStorage;
        this.shoppingListSyncDataResetter = shoppingListSyncDataResetter;
        this.recipeSyncDataResetter = recipeSyncDataResetter;
    }



    public void resetSyncDataIfNecessary() {

        ConnectionInfo existingInfo = connectionInfoStorage.getConnectionInfo();
        ConnectionInfo currentInfo = getCurrrentConnectionInfo();

        if(!currentInfo.equals(existingInfo)) {

            shoppingListSyncDataResetter.resetSyncData();
            recipeSyncDataResetter.resetSyncData();

            connectionInfoStorage.setConnectionInfo(currentInfo);
        }

    }



    private ConnectionInfo getCurrrentConnectionInfo() {

        String userId = SessionHandler.getSessionUser(context);
        if(userId == null) {
            userId = "";
        }
        String apiEndPoint = SharedPreferencesHelper.loadString(SharedPreferencesHelper.API_ENDPOINT,
                resourceProvider.getString(R.string.API_HOST),
                context);

        return new ConnectionInfo(userId, apiEndPoint);
    }


}
