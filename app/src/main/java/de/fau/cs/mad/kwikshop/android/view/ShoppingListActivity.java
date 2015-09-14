package de.fau.cs.mad.kwikshop.android.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import javax.inject.Inject;

import butterknife.ButterKnife;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.model.InternetHelper;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.model.SessionHandler;
import de.fau.cs.mad.kwikshop.android.model.messages.MoveAllItemsEvent;
import de.fau.cs.mad.kwikshop.android.model.synchronization.CompositeSynchronizer;
import de.fau.cs.mad.kwikshop.android.restclient.RestClientFactory;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.view.interfaces.EditModeActivity;
import de.fau.cs.mad.kwikshop.android.view.interfaces.SaveDeleteActivity;
import de.fau.cs.mad.kwikshop.android.viewmodel.ShoppingListViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

public class ShoppingListActivity extends BaseActivity implements EditModeActivity {

    @Inject
    RestClientFactory clientFactory;

    @Inject
    ViewLauncher viewLauncher;

    public static final String SHOPPING_LIST_ID = "shopping_list_id";
    public static final String SHOPPING_MODE = "shopping_mode";
    public static final String EDIT_MODE = "edit_mode";

    public Menu menu;

    private int listId = -1;

    private ShoppingListViewModel viewModel;


    public static Intent getIntent(Context context, int shoppingListId) {

        Intent intent = new Intent(context, ShoppingListActivity.class);
        intent.putExtra(SHOPPING_LIST_ID, shoppingListId);
        return intent;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        if(!getIntent().getExtras().getBoolean(EDIT_MODE)){
            getMenuInflater().inflate(R.menu.overflow_action_menu, menu);
            getMenuInflater().inflate(R.menu.shoppinglist_replacement_menu, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){

        MenuItem findLocationItem =  menu.findItem(R.id.refresh_current_supermarket);

        if(findLocationItem != null){
            if(SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.LOCATION_PERMISSION, false, this))
                findLocationItem.setVisible(true);
            else
                findLocationItem.setVisible(false);

        }

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

                /* Synchronize first to make sure this ShoppingList exists on the server */
                SyncingActivity.requestSync();
                startSharingCodeIntent(ListStorageFragment.getLocalListStorage().loadList(listId).getServerId());
                break;

            case R.id.action_edit_items:
                /* start edit mode */
                viewLauncher.showShoppingListInEditMode(listId);
                break;
        }
        if(type != null) EventBus.getDefault().post(type);

        return super.onOptionsItemSelected(item);
    }

    protected void startSharingCodeIntent(final Integer serverId) {

        new AsyncTask<Integer, Void, String>() {

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
                    // Disabled because there is already an error messagebox when syncing fails
                    /*AlertDialog.Builder messageBox = new AlertDialog.Builder(ShoppingListActivity.this);
                    messageBox.setPositiveButton(getResources().getString(android.R.string.ok), null);
                    messageBox.setMessage(R.string.share_sharingcodeerror);
                    messageBox.create().show();*/
                } else {
                    /* Create intent and send it */
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
            if(extras.getBoolean(EDIT_MODE)){
                Log.e("SLA", "CustomActionBar is on");
                showCustomActionBar();
            }
        }

        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(frameLayout.getId(), ShoppingListFragment.newInstance(listId)).commit();
        }
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



    protected void showCustomActionBar() {

        //hide default action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        //show custom action bar
        View view = getLayoutInflater().inflate(R.layout.actionbar_save_cancel, null);
        actionBar.setCustomView(view);
    }


    @Override
    public View getSaveButton() {
        ActionBar actionBar = getSupportActionBar();
        return actionBar.getCustomView().findViewById(R.id.button_save);

    }

    @Override
    public View getDeleteButton() {
        ActionBar actionBar = getSupportActionBar();
        return actionBar.getCustomView().findViewById(R.id.button_remove);
    }

    @Override
    public View getAddToShoppingCartButton() {
        ActionBar actionBar = getSupportActionBar();
        return actionBar.getCustomView().findViewById(R.id.button_add_to_cart);
    }

    @Override
    public View getRemoveFromShoppingCartButton() {
        ActionBar actionBar = getSupportActionBar();
        return actionBar.getCustomView().findViewById(R.id.button_remove_from_cart);
    }
}
