package de.fau.cs.mad.kwikshop.android.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;


import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import java.util.Locale;
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


    /* Used by sharing. If this is true, ListOfShoppingLists will be opened after sync. */
    private boolean returnToListOfShoppingLists = false;

    ProgressDialog syncProgressDialog;

    BaseViewModel viewModel;

    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;

    public static FrameLayout frameLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // header image for navigation drawer
        View mNavigationViewHeader = getLayoutInflater().inflate(R.layout.navigation_drawer_header, null);

        frameLayout = (FrameLayout) findViewById(R.id.content_frame);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        viewModel = ObjectGraph.create(new KwikShopModule(this)).get(BaseViewModel.class);

        /*
        // Shopping Mode
        if(SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.SHOPPING_MODE, false, this)){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        */

        // restart to set locale
        viewModel.setSavedLocale();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        // add header to navigation drawer
        mNavigationView.addHeaderView(mNavigationViewHeader);

        // handle click events in navigation drawer
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                switch (menuItem.getItemId()) {

                    case R.id.nav_login:
                        mDrawerLayout.closeDrawers();
                        finish();
                        viewModel.startLoginActivity();
                        return true;

                    case R.id.nav_shopping_lists:
                        mDrawerLayout.closeDrawers();
                        finish();
                        startActivity(new Intent(getApplicationContext(), ListOfShoppingListsActivity.class));

                        return true;
                    case R.id.nav_recipe:
                        mDrawerLayout.closeDrawers();
                        finish();
                        startActivity(new Intent(getApplicationContext(), ListOfRecipesActivity.class));

                        return true;
                    case R.id.nav_supermarket_finder:
                        mDrawerLayout.closeDrawers();
                        finish();
                        startActivity(new Intent(getApplicationContext(), LocationActivity.class).putExtra("LocationStarted", true));

                        return true;
                    case R.id.nav_settings:
                        mDrawerLayout.closeDrawers();
                        finish();
                        startActivity(new Intent(getApplicationContext(), SettingActivity.class));

                        return true;
                    case R.id.nav_about:
                        mDrawerLayout.closeDrawers();
                        finish();
                        startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                        return true;
                    case R.id.nav_server:
                        mDrawerLayout.closeDrawers();
                        finish();
                        startActivity(ServerIntegrationDebugActivity.getIntent(getApplicationContext()));
                        return true;

                }
                return true;
            }
        });


        EventBus.getDefault().register(this);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if(mDrawerLayout.isDrawerOpen(GravityCompat.START))
                    mDrawerLayout.closeDrawers();
                else
                    mDrawerLayout.openDrawer(GravityCompat.START);
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
