package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.util.Locale;

import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.util.StackTraceReporter;
import de.fau.cs.mad.kwikshop.android.util.TopExceptionHandler;

/**
 * BaseActivity: all activities have to inherit
 */
public class BaseActivity extends ActionBarActivity {


    public static final Object errorReportingLock = new Object();
    public static boolean isErrorReportingInitialized = false;

    public static FrameLayout frameLayout;
    public static boolean refreshed = false;
    public static Menu overflow_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);


        initializeErrorReporting();

        if(SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.SHOPPING_MODE, false, this)){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setContentView(R.layout.activity_main);
        setSavedLocale();
        frameLayout = (FrameLayout)findViewById(R.id.content_frame);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        // color of back arrow to white
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(getResources().getColor(R.color.background_material_light), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        // disable go back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // home icon in actionbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_home);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow_action_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                return true;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.action_location:
                startActivity(new Intent(this, LocationActivity.class));
                return true;
            case R.id.action_listofshoppinglists:
                startActivity(new Intent(this, ListOfShoppingListsActivity.class));
                return true;
            case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_listofrecipes:
                startActivity(new Intent(this, ListOfRecipesActivity.class));
                return true;
            case R.id.action_openloginactivity:
                Intent intent = new Intent(this, LoginActivity.class);
                Bundle b = new Bundle();
                b.putBoolean("FORCE", true); //To make sure the Activity does not close immediately
                intent.putExtras(b);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {

        if (SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.SHOPPING_MODE, false, this)) {
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.SHOPPING_MODE, false, getApplicationContext());
            Intent intent = getIntent();
            finish();
            startActivity(intent);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.SHOPPING_MODE, false, getApplicationContext());

    }


    public void setSavedLocale() {

        if (refreshed) {
            return;
        }

        refreshed = true;

        Locale setLocale;

        // get current locale index
        int currentLocaleIdIndex = getSharedPreferences(SettingFragment.SETTINGS, Context.MODE_PRIVATE).getInt(SettingFragment.OPTION_1, 0);
        setLocale = new Locale(SettingFragment.localeIds[currentLocaleIdIndex].toString());

        if(currentLocaleIdIndex == 0) // default
            setLocale = Locale.getDefault();

        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = setLocale;
        res.updateConfiguration(conf, dm);


        // Activity must be restarted to set saved locale
        Intent refresh = new Intent(this, ListOfShoppingListsActivity.class);
        finish();
        startActivity(refresh);
    }


    public void initializeErrorReporting() {

        if (!refreshed) {
            return;
        }

        synchronized (errorReportingLock) {

            if(isErrorReportingInitialized) {
                return;
            }

            // register the TopExceptionHandler
            Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
            StackTraceReporter stackTraceReporter = ObjectGraph.create(new KwikShopModule(this)).get(StackTraceReporter.class);
            stackTraceReporter.reportStackTraceIfAvailable();

            isErrorReportingInitialized = true;
        }

    }

}
