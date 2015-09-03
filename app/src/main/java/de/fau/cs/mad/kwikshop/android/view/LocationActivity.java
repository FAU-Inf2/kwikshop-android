package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.ButterKnife;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.greenrobot.event.EventBus;

public class LocationActivity extends BaseActivity {


    @Inject
    ViewLauncher viewLauncher;

    @Inject
    ResourceProvider resourceProvider;

    public static Intent getIntent(Context context) {
        return new Intent(context, LocationActivity.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.location_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_store_type:
                selectPlaceType();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(this));
        objectGraph.inject(this);

        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(frameLayout.getId(), LocationFragment.newInstance()).commit();
        }
    }


    private void selectPlaceType(){

        boolean supermarketIsEnabled = SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.STORE_TYPE_SUPERMARKET, true, this);
        boolean bakeryIsEnabled = SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.STORE_TYPE_BAKERY, false, this);
        boolean gasStationIsEnabled = SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.STORE_TYPE_GAS_STATION, false, this);
        boolean liquorStoreIsEnabled = SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.STORE_TYPE_LIQUOR_STORE, false, this);
        boolean pharmacyIsEnabled = SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.STORE_TYPE_PHARMACY, false, this);
        boolean shoppingMallIsEnabled = SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.STORE_TYPE_SHOPPING_MALL, false, this);
        boolean floristIsEnabled = SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.STORE_TYPE_STORE, false, this);


        boolean[] storeTypeStatus = new boolean[]{
                supermarketIsEnabled,
                bakeryIsEnabled,
                gasStationIsEnabled,
                liquorStoreIsEnabled,
                pharmacyIsEnabled,
                shoppingMallIsEnabled,
                floristIsEnabled
        };

        viewLauncher.showMultiplyChoiceDialog(
                resourceProvider.getString(R.string.localization_store_types_dialog_title),
                resourceProvider.getStringArray(R.array.store_types_array),
                storeTypeStatus,
                //select command
                new Command<Integer>() {
                    @Override
                    public void execute(Integer selection) {
                        switch(selection){
                            case 0:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_SUPERMARKET, true, getApplicationContext());
                                break;
                            case 1:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_BAKERY, true, getApplicationContext());
                                break;
                            case 2:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_GAS_STATION, true, getApplicationContext());
                                break;
                            case 3:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_LIQUOR_STORE, true, getApplicationContext());
                                break;
                            case 4:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_PHARMACY, true, getApplicationContext());
                                break;
                            case 5:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_SHOPPING_MALL, true,  getApplicationContext());
                                break;
                            case 6:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_STORE, true, getApplicationContext());
                                break;

                        }

                    }
                },
                //deselect command
                new Command<Integer>() {
                    @Override
                    public void execute(Integer deSelection) {
                        switch(deSelection){
                            case 0:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_SUPERMARKET, false, getApplicationContext());
                                break;
                            case 1:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_BAKERY, false, getApplicationContext());
                                break;
                            case 2:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_GAS_STATION, false, getApplicationContext());
                                break;
                            case 3:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_LIQUOR_STORE, false, getApplicationContext());
                                break;
                            case 4:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_PHARMACY, false, getApplicationContext());
                                break;
                            case 5:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_SHOPPING_MALL, false,  getApplicationContext());
                                break;
                            case 6:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_STORE, false, getApplicationContext());
                                break;
                        }

                    }
                },
                //positive command
                resourceProvider.getString(R.string.dialog_OK),
                new Command<Void>() {
                    @Override
                    public void execute(Void parameter) {
                        viewLauncher.restartActivity();
                    }
                },
                //negative command
                resourceProvider.getString(R.string.cancel),
                new Command<Void>() {
                    @Override
                    public void execute(Void parameter) {

                    }
                }
        );
    }


   

}
