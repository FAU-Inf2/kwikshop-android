package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;

import java.util.Locale;

import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.util.StackTraceReporter;
import de.fau.cs.mad.kwikshop.android.util.TopExceptionHandler;

/**
 * Entry activity that checks for crash-dumps and offers to send using email or copy it to clipboard
 * <p/>
 * By inheritng from SyncingActivity, this also is the activity that sets up syncing with the server
 * using andoird's sync framework
 */
public class ErrorReportingActivity extends SyncingActivity {

    private final static String EXTRA_FINISH_INSTANTLY = "extra_finishInstantly";

    public static final Object errorReportingLock = new Object();
    public static boolean isErrorReportingInitialized = false;
    public static boolean refreshed = false;

    private StackTraceReporter stackTraceReporter;


    public static Intent getIntent(Context context, boolean finishInstantly) {
        Intent intent = new Intent(context, ErrorReportingActivity.class);
        intent.putExtra(EXTRA_FINISH_INSTANTLY, finishInstantly);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getBooleanExtra(EXTRA_FINISH_INSTANTLY, false)) {
            finish();
        }


        setContentView(R.layout.activity_error_reporting);
        getSupportActionBar().hide();

        initializeErrorReporting();

        setSavedLocale();


    }

    @Override
    protected void onResume() {
        super.onResume();
        reportStackTraceIfAvailable();
    }

    public void initializeErrorReporting() {

        if (!refreshed) {
            return;
        }

        synchronized (errorReportingLock) {

            if (isErrorReportingInitialized) {
                exitActivity();
                return;
            }

            isErrorReportingInitialized = true;
        }
        // register the TopExceptionHandler
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
        stackTraceReporter = ObjectGraph.create(new KwikShopModule(this)).get(StackTraceReporter.class);

    }


    private void reportStackTraceIfAvailable() {

        if (stackTraceReporter != null) {

            StackTraceReporter.Callback exitAcitvityCallBack = new StackTraceReporter.Callback() {
                @Override
                public void onCallback() {
                        exitActivity();
                }
            };

            // do not exit the activity, if the user choose to send an email
            // otherwise the activity we launch next will start in foreground and the user will not
            // see his email client.
            // as we call reportStackTraceIfAvailable() every time the unit is resumed
            // we will exit the current activity when the user returns from the mail client
            stackTraceReporter.reportStackTraceIfAvailable(exitAcitvityCallBack,
                                                           null,
                                                           exitAcitvityCallBack,
                                                           exitAcitvityCallBack);
        }
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
        nextActivity.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

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
        int currentLocaleIdIndex = SharedPreferencesHelper.loadInt(SharedPreferencesHelper.LOCALE, 0, getApplicationContext());
        setLocale = new Locale(SettingFragment.localeIds[currentLocaleIdIndex].toString());

        if (currentLocaleIdIndex == 0) // default
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


}
