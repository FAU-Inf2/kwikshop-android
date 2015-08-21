package de.fau.cs.mad.kwikshop.android.view;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;

import java.util.Locale;

import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.util.StackTraceReporter;
import de.fau.cs.mad.kwikshop.android.util.TopExceptionHandler;

public class ErrorReportingActivity extends ActionBarActivity {

    private final static String EXTRA_FINISH_INSTANTLY = "extra_finishInstantly";

    public static final Object errorReportingLock = new Object();
    public static boolean isErrorReportingInitialized = false;
    public static boolean refreshed = false;

    Account account;

    public static Intent getIntent(Context context, boolean finishInstantly) {
        Intent intent = new Intent(context, ErrorReportingActivity.class);
        intent.putExtra(EXTRA_FINISH_INSTANTLY, finishInstantly);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getIntent().getBooleanExtra(EXTRA_FINISH_INSTANTLY, false)) {
            finish();
        }


        setContentView(R.layout.activity_error_reporting);
        getSupportActionBar().hide();

        initializeErrorReporting();

        setSavedLocale();

        //set up dummy account for syncing
        account = CreateSyncAccount(this);

    }


    public void initializeErrorReporting() {

        if(!refreshed) {
            return;
        }

        synchronized (errorReportingLock) {

            if(isErrorReportingInitialized) {
                exitActivity();
                return;
            }

            isErrorReportingInitialized = true;
        }
        // register the TopExceptionHandler
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
        StackTraceReporter stackTraceReporter = ObjectGraph.create(new KwikShopModule(this)).get(StackTraceReporter.class);
        stackTraceReporter.reportStackTraceIfAvailable(new StackTraceReporter.Callback() {
            @Override
            public void onCompleted() {

                exitActivity();
            }
        });


    }


    private void exitActivity() {

        Intent nextActivity;

        // optimization to reduce starting of activities:
        // directly go to list of shopping lists if LoginActivity would close itself anyways
        if (LoginActivity.skipActivity(this)) {

            nextActivity = ListOfShoppingListsActivity.getIntent(this);

        } else {

            nextActivity = LoginActivity.getIntent(this);
        }


        startActivity(nextActivity);
        finish();

    }

    // duplicate from BaseActivity. Cannot inherit from BaseActivity because we need to
    // call into initializeErrorReporting() before setSavedLocale()
    // Might be a future task to optimize this
    private void setSavedLocale() {

        if (refreshed) {
            return;
        }

        refreshed = true;

        Locale setLocale;

        // get current locale index
        int currentLocaleIdIndex =  SharedPreferencesHelper.loadInt(SharedPreferencesHelper.LOCALE, 0, getApplicationContext());
        setLocale = new Locale(SettingFragment.localeIds[currentLocaleIdIndex].toString());

        if(currentLocaleIdIndex == 0) // default
            setLocale = Locale.getDefault();

        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = setLocale;
        res.updateConfiguration(conf, dm);


        // Activity must be restarted to set saved locale
        Intent refresh = new Intent(this, getClass());
        finish();
        startActivity(refresh);
    }

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account CreateSyncAccount(Context context) {

        // Create the account type and default account
        Account newAccount = new Account("dummyaccount", "kwikshop.mad.cs.fau.de");
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




}
