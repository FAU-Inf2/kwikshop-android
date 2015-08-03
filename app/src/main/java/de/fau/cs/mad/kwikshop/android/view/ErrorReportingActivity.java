package de.fau.cs.mad.kwikshop.android.view;

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

    public static final Object errorReportingLock = new Object();
    public static boolean isErrorReportingInitialized = false;
    public static boolean refreshed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_error_reporting);
        getSupportActionBar().hide();

        initializeErrorReporting();

        setSavedLocale();
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




}
