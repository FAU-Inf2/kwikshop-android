package de.fau.cs.mad.kwikshop.android.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
import de.fau.cs.mad.kwikshop.android.model.messages.SynchronizationEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.SynchronizationEventType;
import de.fau.cs.mad.kwikshop.android.model.synchronization.CompositeSynchronizer;
import de.fau.cs.mad.kwikshop.android.model.synchronization.ShoppingListSynchronizer;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.greenrobot.event.EventBus;

/**
 * BaseActivity: all activities have to inherit
 */
public class BaseActivity extends ActionBarActivity {

    public static FrameLayout frameLayout;
    public static boolean refreshed = false;

    ProgressDialog syncProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);


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

        EventBus.getDefault().register(this);

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

            case R.id.action_serverintegrationdebugactiviy:
                startActivity(ServerIntegrationDebugActivity.getIntent(this));
                return true;

            case R.id.action_startSynchronization:
                startSynchronization();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {

        if (SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.SHOPPING_MODE, false, this)) {
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.SHOPPING_MODE, false, getApplicationContext());
            restartActivity();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void restartActivity(){
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void setSavedLocale() {

        if (refreshed) {
            return;
        }
        refreshed = true;

        // get current locale index
       // int currentLocaleIdIndex = getSharedPreferences(SettingFragment.SETTINGS, Context.MODE_PRIVATE).getInt(SharedPreferencesHelper.LOCALE, 0);
        int currentLocaleIdIndex =  SharedPreferencesHelper.loadInt(SharedPreferencesHelper.LOCALE,0,getApplicationContext());
        Locale setLocale= new Locale(SettingFragment.localeIds[currentLocaleIdIndex].toString());

        if(currentLocaleIdIndex == 0) // default
            setLocale = Locale.getDefault();

        // change locale configuration
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = setLocale;
        res.updateConfiguration(conf, dm);

        // Activity must be restarted to set saved locale
        restartActivity();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(SynchronizationEvent event) {

        ProgressDialog dialog = getSyncProgressDialog();
        dialog.setMessage(event.getMessage());

        if(event.getEventType() == SynchronizationEventType.Completed) {
            dismissSyncProgressDialog();
        }

        EventBus.getDefault().cancelEventDelivery(event);
    }

    //Only call from main thread (not thread-safe)
    private ProgressDialog getSyncProgressDialog() {

        if(syncProgressDialog == null) {

            syncProgressDialog =  ProgressDialog.show(
                    this,
                    getResources().getString(R.string.synchronizing),
                    "",
                    true);
        }
        return this.syncProgressDialog;
    }

    //Only call from main thread (not thread-safe)
    private void dismissSyncProgressDialog() {

        if(syncProgressDialog != null) {
            syncProgressDialog.dismiss();
            syncProgressDialog = null;
        }

    }

    protected void startSynchronization() {


        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(this));
        final CompositeSynchronizer synchronizer = objectGraph.get(CompositeSynchronizer.class);

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {

                synchronizer.synchronize();

                return null;
            }
        }.execute();

    }



}
