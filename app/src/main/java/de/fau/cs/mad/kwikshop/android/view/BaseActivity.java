package de.fau.cs.mad.kwikshop.android.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.ikimuhendis.ldrawer.ActionBarDrawerToggle;
import java.util.Locale;
import butterknife.ButterKnife;
import butterknife.InjectView;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.model.messages.ShareSuccessEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.SynchronizationEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.SynchronizationEventType;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.BaseViewModel;
import de.greenrobot.event.EventBus;

/**
 * BaseActivity: all activities have to inherit
 */
public class BaseActivity extends ActionBarActivity {

    public static boolean refreshed = false;

    /* Used by sharing. If this is true, ListOfShoppingLists will be opened after sync. */
    private boolean returnToListOfShoppingLists = false;

    ProgressDialog syncProgressDialog;

    BaseViewModel viewModel;

    @InjectView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @InjectView(R.id.navigation_view)
    NavigationView mNavigationView;

    public static FrameLayout frameLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View mNavigationViewHeader = getLayoutInflater().inflate(R.layout.navigation_drawer_header, null);

        frameLayout = (FrameLayout) findViewById(R.id.content_frame);

        ButterKnife.inject(this);

        viewModel = ObjectGraph.create(new KwikShopModule(this)).get(BaseViewModel.class);

        /*
        // Shopping Mode
        if(SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.SHOPPING_MODE, false, this)){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        */

        mNavigationView.addHeaderView(mNavigationViewHeader);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                menuItem.setChecked(true);
                switch (menuItem.getItemId()) {
                    case R.id.nav_login:
                        mDrawerLayout.closeDrawers();
                        viewModel.startLoginActivity();
                        return true;
                    case R.id.nav_shopping_lists:
                        mDrawerLayout.closeDrawers();
                        startActivity(new Intent(getApplicationContext(), ListOfShoppingListsActivity.class));

                        return true;
                    case R.id.nav_recipe:
                        mDrawerLayout.closeDrawers();
                        startActivity(new Intent(getApplicationContext(), ListOfRecipesActivity.class));

                        return true;
                    case R.id.nav_supermarket_finder:
                        mDrawerLayout.closeDrawers();
                        startActivity(new Intent(getApplicationContext(), LocationActivity.class));

                        return true;
                    case R.id.nav_settings:
                        mDrawerLayout.closeDrawers();
                        startActivity(new Intent(getApplicationContext(), SettingActivity.class));

                        return true;
                    case R.id.nav_about:
                        mDrawerLayout.closeDrawers();
                        startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                        return true;
                    case R.id.nav_server:
                        mDrawerLayout.closeDrawers();
                        startActivity(ServerIntegrationDebugActivity.getIntent(getApplicationContext()));
                        return true;

                }
                return true;
            }
        });

        setSavedLocale();


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

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
                if(mDrawerLayout.isDrawerOpen(GravityCompat.START))
                    mDrawerLayout.closeDrawers();
                else
                    mDrawerLayout.openDrawer(GravityCompat.START);
                return true;

            case R.id.action_listofrecipes:
                startActivity(new Intent(this, ListOfRecipesActivity.class));
                return true;

            case R.id.action_serverintegrationdebugactiviy:
                startActivity(ServerIntegrationDebugActivity.getIntent(this));
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
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
        } else if(event.getEventType() == SynchronizationEventType.Failed) {
            dismissSyncProgressDialog();

            AlertDialog.Builder messageBox = new AlertDialog.Builder(this);
            messageBox.setPositiveButton(getResources().getString(android.R.string.ok), null);
            messageBox.setMessage(event.getMessage());
            messageBox.setCancelable(false);
            messageBox.create().show();

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

        /* Return to ListOfShoppingLists after sharing */
        if(returnToListOfShoppingLists) {
            returnToListOfShoppingLists = false;
            Intent intent = new Intent(this, ListOfShoppingListsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

    }


    @SuppressWarnings("unused")
    public void onEvent(ShareSuccessEvent event) {
        returnToListOfShoppingLists = true;
        SyncingActivity.requestSync();
    }

}
