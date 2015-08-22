package de.fau.cs.mad.kwikshop.android.view;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import de.fau.cs.mad.kwikshop.android.R;

import static de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper.*;

/**
 * Activity that initializes syncing using Android's sync framework
 * The app's entry activity should inherit from this
 */
public abstract class SyncingActivity extends ActionBarActivity {

    private static final String AUTHORITY = "de.fau.cs.mad.kwikshop.android.provider";
    private static final String ACCOUNT_TYPE = "de.fau.cs.mad.kwikshop.android";
    private static final String ACCOUNT_NAME = "KwikShop Default Account";


    private static final Object initializationLock = new Object();
    private static boolean isSyncingInitialized = false;
    private static Account account;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //set up syncing
        synchronized (initializationLock) {
            if(!isSyncingInitialized) {

                //creates a dummy account (we don't need the account for syncing,
                // but the sync framework expects us to create one anyways)
                account = CreateSyncAccount(this);

                //make sync framework sync automatically

                ContentResolver.setSyncAutomatically(account, AUTHORITY, true);

                int intervalInMinutes = loadInt(SYNCHRONIZATION_INTERVAL,
                                                getResources().getInteger(R.integer.synchronizationInterval_default),
                                                this);
                ContentResolver.addPeriodicSync(account, AUTHORITY, Bundle.EMPTY, intervalInMinutes * 60);

                isSyncingInitialized = true;
            }
        }
    }



    private static Account CreateSyncAccount(Context context) {

        // Create the account type and default account
        Account newAccount = new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
        } else {

            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
        }

        return newAccount;
    }

    public static void requestSync() {

        synchronized (initializationLock) {
            if(!isSyncingInitialized) {
                throw new UnsupportedOperationException("The sync framework has not been initialized yet");
            }
        }

        // Pass the settings flags by inserting them in a bundle
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        /*
         * Request the sync for the default account, authority, and
         * manual sync settings
         */
        ContentResolver.requestSync(account, AUTHORITY, settingsBundle);

    }


    public static void onSyncIntervalSettingChanged(int newValue) {

        synchronized (initializationLock) {
            if(isSyncingInitialized) {
                ContentResolver.addPeriodicSync(account, AUTHORITY, Bundle.EMPTY, newValue * 60);
            }

        }
    }


}
