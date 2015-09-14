package de.fau.cs.mad.kwikshop.android.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import javax.inject.Inject;

import butterknife.ButterKnife;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.model.InternetHelper;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.model.SessionHandler;
import de.fau.cs.mad.kwikshop.android.model.messages.MoveAllItemsEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.StartSharingCodeIntentEvent;
import de.fau.cs.mad.kwikshop.android.restclient.RestClientFactory;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.ShoppingListViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.greenrobot.event.EventBus;

public class ShoppingListActivity extends BaseActivity {

    @Inject
    RestClientFactory clientFactory;

    @Inject
    ViewLauncher viewLauncher;

    public static final String SHOPPING_LIST_ID = "shopping_list_id";
    public static final String SHOPPING_MODE = "shopping_mode";

    public Menu menu;

    private int listId = -1;

    private ShoppingListViewModel viewModel;

    private ProgressDialog progressDialog;


    public static Intent getIntent(Context context, int shoppingListId) {

        Intent intent = new Intent(context, ShoppingListActivity.class);
        intent.putExtra(SHOPPING_LIST_ID, shoppingListId);
        return intent;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.overflow_action_menu, menu);
        getMenuInflater().inflate(R.menu.shoppinglist_replacement_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){

        MenuItem findLocationItem =  menu.findItem(R.id.refresh_current_supermarket);

        if(SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.LOCATION_PERMISSION, false, this))
            findLocationItem.setVisible(true);
        else
            findLocationItem.setVisible(false);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        ItemSortType  type = null;
        switch (item.getItemId()){
            case R.id.sort_by_group_option: type = ItemSortType.GROUP; break;
            case R.id.sort_by_alphabet_option: type = ItemSortType.ALPHABETICALLY; break;
            case R.id.action_move_all_to_shopping_cart:
                EventBus.getDefault().post(MoveAllItemsEvent.moveAllToBoughtEvent);
                break;
            case R.id.action_move_all_from_shopping_cart:
                EventBus.getDefault().post(MoveAllItemsEvent.moveAllFromBoughtEvent);
                break;
            case R.id.action_shopping_mode:
                /* start shopping mode */
                viewLauncher.showShoppingListInShoppingMode(listId);
                /*
                Intent shoppingModeIntent = ShoppingListActivity.getIntent(getApplicationContext(), getIntent().getExtras().getInt(SHOPPING_LIST_ID));
                shoppingModeIntent.putExtra(SHOPPING_MODE, true);
                shoppingModeIntent.putExtra(ShoppingListFragment.DO_NOT_ASK_FOR_SUPERMARKET, true);
                startActivity(shoppingModeIntent);
                */
                break;
            case R.id.refresh_current_supermarket:
                return false;
            case R.id.share_option:

                /* Check if user is logged in */
                if(!SessionHandler.isAuthenticated(getApplicationContext())) {
                    AlertDialog.Builder messageBox = new AlertDialog.Builder(this);
                    messageBox.setPositiveButton(getResources().getString(android.R.string.ok), null);
                    messageBox.setMessage(getResources().getString(R.string.share_notloggedin));
                    messageBox.create().show();
                    break;
                }

                /* Check for internet connection */
                if(!InternetHelper.checkInternetConnection(this)) {
                    AlertDialog.Builder messageBox = new AlertDialog.Builder(ShoppingListActivity.this);
                    messageBox.setPositiveButton(getResources().getString(android.R.string.ok), null);
                    messageBox.setMessage(R.string.alert_dialog_connection_message);
                    messageBox.create().show();
                    break;
                }

                /* Synchronize if serverId is 0, then start the intent */
                int serverId = ListStorageFragment.getLocalListStorage().loadList(listId).getServerId();
                if(serverId == 0) {
                    startSharingCodeIntent = true;
                    SyncingActivity.requestSync();
                } else {
                    startSharingCodeIntent(serverId);
                }
                break;
        }
        if(type != null) EventBus.getDefault().post(type);

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(StartSharingCodeIntentEvent event) {
        int serverId = ListStorageFragment.getLocalListStorage().loadList(listId).getServerId();
        startSharingCodeIntent(serverId);
    }

    protected void startSharingCodeIntent(final Integer serverId) {

        new AsyncTask<Integer, Void, String>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = new ProgressDialog(ShoppingListActivity.this);
                progressDialog.setMessage(ShoppingListActivity.this.getResources().getString(R.string.share_loading));
                progressDialog.setIndeterminate(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(true);
                progressDialog.show();

            }

            @Override
            protected String doInBackground(Integer... id) {
                try {
                    if(serverId == null)
                        return null;

                    return clientFactory.getShoppingListClient().getSharingCode(serverId.intValue()).getSharingCode();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {

                if(result == null) {
                    progressDialog.setMessage(getApplicationContext().getResources().getString(R.string.share_sharingcodeerror));
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                        }
                    }, 1000);
                } else {
                    /* Create intent and send it */
                    progressDialog.dismiss();
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT,
                            String.format(getResources().getString(R.string.share_intentstring), result));
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                }

            }

        }.execute();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.inject(this);
        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(this));
        viewModel = objectGraph.get(ShoppingListViewModel.class);
        objectGraph.inject(this);

        //Get Shopping List ID

        Intent intent = getIntent();
        listId = -1;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            listId = extras.getInt(SHOPPING_LIST_ID);
        }

        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(frameLayout.getId(), ShoppingListFragment.newInstance(listId)).commit();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public void onBackPressed() {

        // check for barcode scanner fragment
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment BarcodeScannerFragment = fragmentManager.findFragmentByTag("BARCODE_SCANNER_FRAGMENT");
        if (BarcodeScannerFragment != null && BarcodeScannerFragment.isVisible()) {
            startActivity(getIntent().putExtra(ShoppingListFragment.DO_NOT_ASK_FOR_SUPERMARKET, true));
            return;
        }

        // check for shopping mode
        if(baseViewModel.isShoppingModeEnabled()){
            viewModel.showDialogLeaveShoppingMode(listId);
            return;
        }

        super.onBackPressed();
    }




}
