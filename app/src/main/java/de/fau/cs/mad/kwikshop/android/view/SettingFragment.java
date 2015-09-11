package de.fau.cs.mad.kwikshop.android.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import javax.inject.Inject;

import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.BuildConfig;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.common.AutoCompletionBrandData;
import de.fau.cs.mad.kwikshop.android.common.AutoCompletionData;
import de.fau.cs.mad.kwikshop.android.common.Setting;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.DatabaseHelper;
import de.fau.cs.mad.kwikshop.android.model.SimpleStorageBase;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.BaseViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.NullCommand;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;

import static de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper.*;


public class SettingFragment extends Fragment {

    public static CharSequence[] localeSelectionNames = {"Default", "English", "German", "Portuguese", "Russian"};
    public static CharSequence[] localeIds = {"default", "en", "de", "pt", "ru"};

    private AlertDialog alert;
    private Context context;
    private ArrayList<Setting> settingsList;
    private SettingAdapter objAdapter;


    private Setting apiEndpointSetting;
    private Setting locationPermissionSetting;
    private Setting localeSetting;
    private Setting autoCompletionDeletionSetting;
    private Setting manageUnitsSetting;
    private Setting itemDeletionSetting;
    private Setting slDeletionSetting;
    private Setting recipeDeletionSetting;
    private Setting recipeAddDefaultSetting;
    private Setting parserSeparatorWordSetting;
    private Setting loginSetting;
    private Setting enableSyncSetting;
    private Setting syncNowSetting;
    private Setting syncIntervalSetting;
    private Setting placeTypeSetting;
    private Setting askForLocalizationPermissionSetting;
    private Setting supermarketFinderRadiusSetting;

    @Inject
    ViewLauncher viewLauncher;

    @Inject
    ResourceProvider resourceProvider;


    public static SettingFragment newInstance() {
        return new SettingFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(getActivity()));
        objectGraph.inject(this);

        context = getActivity().getApplicationContext();

        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);
        ListView listView = (ListView) rootView.findViewById(android.R.id.list);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // local change
                if (settingsList.get(position).equals(localeSetting))
                    changeLocalOption();

                // delete history of autocompletion
                if (settingsList.get(position).equals(autoCompletionDeletionSetting))
                    deleteAutoCompletionHistoryOption();

                // manage units
                if (settingsList.get(position).equals(manageUnitsSetting))
                    manageUnits();

                // set API endpoint
                if (settingsList.get(position).equals(apiEndpointSetting))
                    setApiEndpoint();

                // location permission
                if (settingsList.get(position).equals(locationPermissionSetting)) {
                    setLocationPermission(position);
                }

                // Item deletion
                if (settingsList.get(position).equals(itemDeletionSetting)) {
                    setItemDeletionSetting(position);
                }

                // Shopping List deletion
                if (settingsList.get(position).equals(slDeletionSetting)) {
                    setShoppingListDeletionSetting(position);
                }

                // Recipe deletion
                if (settingsList.get(position).equals(recipeDeletionSetting)) {
                    setRecipeDeletionSetting(position);
                }

                // Default recipes
                if( settingsList.get(position).equals(recipeAddDefaultSetting)){
                    setRecipeAddDefaultSetting(position);
                }

                // Parser separator word
                if (settingsList.get(position).equals(parserSeparatorWordSetting)) {
                    setParserSeparatorWord();
                }

                if (settingsList.get(position).equals(loginSetting)) {
                    startLoginActivity();
                }

                if (settingsList.get(position).equals(syncNowSetting)) {
                    SyncingActivity.requestSync();
                }

                if (settingsList.get(position).equals(enableSyncSetting)) {
                    setEnableSynchronization(position);
                }

                if (settingsList.get(position).equals(syncIntervalSetting)) {
                    selectSyncInterval();
                }

                if(settingsList.get(position).equals(placeTypeSetting)){
                    selectPlaceType();
                }

                if(settingsList.get(position).equals(askForLocalizationPermissionSetting)){
                    setAskForLocationPermission(position);
                }

                if(settingsList.get(position).equals(supermarketFinderRadiusSetting)){
                    selectRadius();
                }
            }
        });


        // set title for actionbar
        getActivity().setTitle(R.string.title_activity_settings);


        // Locale Setting
        localeSetting = new Setting(context);
        localeSetting.setName(R.string.settings_option_2_setlocale);
        localeSetting.setCaption(R.string.settings_option_2_desc);

        // Autocompletion deletion
        autoCompletionDeletionSetting = new Setting(context);
        autoCompletionDeletionSetting.setName(R.string.settings_option_3_deleteHistory);
        autoCompletionDeletionSetting.setCaption(R.string.settings_option_3_desc);


        // manage units
        manageUnitsSetting = new Setting(context);
        manageUnitsSetting.setName(R.string.settings_option_3_manageUnits);
        manageUnitsSetting.setCaption(R.string.settings_option_3_desc2);


        // API endpoint settings
        apiEndpointSetting = new Setting(context);
        apiEndpointSetting.setName(R.string.settings_option_4_APIEndPoint_Title);
        apiEndpointSetting.setCaption(R.string.settings_option_4_APIEndPoint_Desc);

        // permission for location tracking
        locationPermissionSetting = new Setting(context);
        locationPermissionSetting.setName(R.string.settings_option_5_location_permission_title);
        locationPermissionSetting.setCaption(R.string.settings_option_5_location_permission_desc);
        locationPermissionSetting.setChecked(SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.LOCATION_PERMISSION, false, getActivity()));
        locationPermissionSetting.setViewVisibility(View.VISIBLE);

        //Show Dialog when deleting an item
        itemDeletionSetting = new Setting(context);
        itemDeletionSetting.setName(R.string.settings_option_6_item_deletion_name);
        itemDeletionSetting.setCaption(R.string.settings_option_6_item_deletion_descr);
        itemDeletionSetting.setChecked(SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.ITEM_DELETION_SHOW_AGAIN_MSG, false, getActivity()));
        itemDeletionSetting.setViewVisibility(View.VISIBLE);

        //Show Dialog when deleting a Shopping List
        slDeletionSetting = new Setting(context);
        slDeletionSetting.setName(R.string.settings_options_delete_shoppinglist_name);
        slDeletionSetting.setCaption(R.string.settings_options_delete_shoppinglist_descr);
        slDeletionSetting.setChecked(SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.SL_DELETION_SHOW_AGAIN_MSG, false, getActivity()));
        slDeletionSetting.setViewVisibility(View.VISIBLE);

        //Show Dialog when deleting a recipe
        recipeDeletionSetting = new Setting(context);
        recipeDeletionSetting.setName(R.string.settings_options_delete_recipe_name);
        recipeDeletionSetting.setCaption(R.string.settings_options_delete_recipe_descr);
        recipeDeletionSetting.setChecked(SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.RECIPE_DELETION_SHOW_AGAIN_MSG, false, getActivity()));
        recipeDeletionSetting.setViewVisibility(View.VISIBLE);

        //Ask to add default recipes when the user has no recipes
        recipeAddDefaultSetting = new Setting(context);
        recipeAddDefaultSetting.setName(R.string.settings_options_add_default_recipes_name);
        recipeAddDefaultSetting.setCaption(R.string.settings_options_add_default_recipes_descr);
        recipeAddDefaultSetting.setChecked(SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.ASK_TO_ADD_DEFAULT_RECIPES, false, getActivity()));
        recipeAddDefaultSetting.setViewVisibility(View.VISIBLE);

        //Choose separator word for the parser
        parserSeparatorWordSetting = new Setting(context);
        parserSeparatorWordSetting.setName(R.string.settings_option_7_parser_separate_word_name);
        parserSeparatorWordSetting.setCaption(R.string.settings_option_7_parser_separate_word_descr);

        //LoginActivity
        loginSetting = new Setting(context);
        loginSetting.setName(R.string.settings_option_8_login_name);
        loginSetting.setCaption(R.string.settings_option_8_login_descr);

        //Ask for Localization Permission
        askForLocalizationPermissionSetting = new Setting(context);
        askForLocalizationPermissionSetting.setName(R.string.setting_permission_for_localization_title);
        askForLocalizationPermissionSetting.setCaption(R.string.setting_permission_for_localization_desc);
        askForLocalizationPermissionSetting.setChecked(SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.LOCATION_PERMISSION_SHOW_AGAIN_MSG, false, getActivity()));
        askForLocalizationPermissionSetting.setViewVisibility(View.VISIBLE);


        enableSyncSetting = new Setting(context);
        enableSyncSetting.setName(R.string.settings_option_enableSync_name);
        enableSyncSetting.setCaption(R.string.settings_option_enableSync_descr);
        enableSyncSetting.setChecked(SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.ENABLE_SYNCHRONIZATION, true, context));
        enableSyncSetting.setViewVisibility(View.VISIBLE);

        syncNowSetting = new Setting(context);
        syncNowSetting.setName(R.string.settings_option_sync_name);
        syncNowSetting.setCaption(R.string.settings_option_sync_descr);

        syncIntervalSetting = new Setting(context);
        syncIntervalSetting.setName(R.string.settings_options_syncInterval_name);
        syncIntervalSetting.setCaption(R.string.settings_options_syncInterval_descr);

        //Change Place Request Type
        placeTypeSetting = new Setting(context);
        placeTypeSetting.setName(R.string.localization_store_types_dialog_title);
        placeTypeSetting.setCaption(R.string.localization_store_types_dialog_caption);

        //Change radius of place request
        supermarketFinderRadiusSetting = new Setting(context);
        supermarketFinderRadiusSetting.setName(R.string.radius);
        supermarketFinderRadiusSetting.setCaption(R.string.setting_change_radius_description);


        //headers
        Setting locationHeaderSetting;
        Setting generalHeaderSetting;
        Setting accountHeaderSetting;
        Setting synchronizationHeaderSetting;
        Setting otherHeaderSetting;


        //localization header
        locationHeaderSetting = new Setting(context);
        locationHeaderSetting.setIsHeader(true);
        locationHeaderSetting.setName(R.string.localization);

        //general header
        generalHeaderSetting = new Setting(context);
        generalHeaderSetting.setIsHeader(true);
        generalHeaderSetting.setName(R.string.general);

        //account header
        accountHeaderSetting = new Setting(context);
        accountHeaderSetting.setIsHeader(true);
        accountHeaderSetting.setName(R.string.account);

        //synchronization header
        synchronizationHeaderSetting = new Setting(context);
        synchronizationHeaderSetting.setIsHeader(true);
        synchronizationHeaderSetting.setName(R.string.synchronization);

        //other heady
        otherHeaderSetting = new Setting(context);
        otherHeaderSetting.setIsHeader(true);
        otherHeaderSetting.setName(R.string.other);



        // list of settings
        settingsList = new ArrayList<>(Arrays.asList(new Setting[]
                {

                        accountHeaderSetting,
                        loginSetting,

                        generalHeaderSetting,
                        localeSetting,
                        autoCompletionDeletionSetting,
                        manageUnitsSetting,
                        recipeAddDefaultSetting,
                        parserSeparatorWordSetting,

                        locationHeaderSetting,
                        locationPermissionSetting,
                        askForLocalizationPermissionSetting,
                        supermarketFinderRadiusSetting,
                        placeTypeSetting,



                        synchronizationHeaderSetting,
                        syncNowSetting,
                        syncIntervalSetting,
                        enableSyncSetting,

                        otherHeaderSetting,
                        itemDeletionSetting,
                        slDeletionSetting,
                        recipeDeletionSetting
                }));

        if(BuildConfig.DEBUG_MODE) {
            settingsList.add(apiEndpointSetting);
        }


        // Adapter for settings view
        objAdapter = new SettingAdapter(getActivity(), R.layout.fragment_setting_row, settingsList);
        listView.setAdapter(objAdapter);
        listView.setSelector(R.drawable.list_selector);
        listView.setDividerHeight(0);

        return rootView;
    }



    @Override
    public void onPause() {
        super.onPause();
        if (alert != null)
            alert.dismiss();
    }


    private void selectPlaceType(){

        boolean supermarketIsEnabled = loadBoolean(SharedPreferencesHelper.STORE_TYPE_SUPERMARKET, true, getActivity());
        boolean bakeryIsEnabled = loadBoolean(SharedPreferencesHelper.STORE_TYPE_BAKERY, false, getActivity());
        boolean gasStationIsEnabled = loadBoolean(SharedPreferencesHelper.STORE_TYPE_GAS_STATION, false, getActivity());
        boolean liquorStoreIsEnabled = loadBoolean(SharedPreferencesHelper.STORE_TYPE_LIQUOR_STORE, false, getActivity());
        boolean pharmacyIsEnabled = loadBoolean(SharedPreferencesHelper.STORE_TYPE_PHARMACY, false, getActivity());
        boolean shoppingMallIsEnabled = loadBoolean(SharedPreferencesHelper.STORE_TYPE_SHOPPING_MALL, false, getActivity());
        boolean floristIsEnabled = loadBoolean(SharedPreferencesHelper.STORE_TYPE_STORE, false, getActivity());


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
                              saveBoolean(SharedPreferencesHelper.STORE_TYPE_SUPERMARKET, true, getActivity());
                                break;
                            case 1:
                               saveBoolean(SharedPreferencesHelper.STORE_TYPE_BAKERY, true, getActivity());
                                break;
                            case 2:
                                saveBoolean(SharedPreferencesHelper.STORE_TYPE_GAS_STATION, true, getActivity());
                                break;
                            case 3:
                               saveBoolean(SharedPreferencesHelper.STORE_TYPE_LIQUOR_STORE, true, getActivity());
                                break;
                            case 4:
                                saveBoolean(SharedPreferencesHelper.STORE_TYPE_PHARMACY, true, getActivity());
                                break;
                            case 5:
                                saveBoolean(SharedPreferencesHelper.STORE_TYPE_SHOPPING_MALL, true, getActivity());
                                break;
                            case 6:
                                saveBoolean(SharedPreferencesHelper.STORE_TYPE_STORE, true, getActivity());
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
                                saveBoolean(SharedPreferencesHelper.STORE_TYPE_SUPERMARKET, false, getActivity());
                                break;
                            case 1:
                               saveBoolean(SharedPreferencesHelper.STORE_TYPE_BAKERY, false, getActivity());
                                break;
                            case 2:
                                saveBoolean(SharedPreferencesHelper.STORE_TYPE_GAS_STATION, false, getActivity());
                                break;
                            case 3:
                               saveBoolean(SharedPreferencesHelper.STORE_TYPE_LIQUOR_STORE, false, getActivity());
                                break;
                            case 4:
                                saveBoolean(SharedPreferencesHelper.STORE_TYPE_PHARMACY, false, getActivity());
                                break;
                            case 5:
                                saveBoolean(SharedPreferencesHelper.STORE_TYPE_SHOPPING_MALL, false, getActivity());
                                break;
                            case 6:
                                saveBoolean(SharedPreferencesHelper.STORE_TYPE_STORE, false, getActivity());
                                break;
                        }

                    }
                },
                //positive command
                resourceProvider.getString(R.string.dialog_OK),
                new Command<Void>() {
                    @Override
                    public void execute(Void parameter) {

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

    public void setLocale(String lang) {

        Locale setLocale = new Locale(lang);
        if (lang.equals("default"))
            setLocale = Locale.getDefault();
        Resources res = getActivity().getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = setLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(getActivity(), SettingActivity.class).putExtra(BaseViewModel.RESTARTEDACTIVITY, false);
        refresh.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startActivity(refresh);
        getActivity().finish();
    }

    public void deleteAutoCompletionHistoryOption() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.settings_option_3_check_title);
        builder.setMessage(R.string.settings_option_3_check_text);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                // delete history of autocompletion
                deleteAutoCompletionHistory();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                // do nothing
            }
        });

        alert = builder.create();
        alert.show();

    }



    private void changeLocalOption() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        int currentLocaleIdIndex = SharedPreferencesHelper.loadInt(SharedPreferencesHelper.LOCALE, 0, getActivity());
        builder.setTitle(R.string.settings_option_2_setlocale);
        builder.setSingleChoiceItems(localeSelectionNames, currentLocaleIdIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setLocale(localeIds[which].toString());
                SharedPreferencesHelper.saveInt(SharedPreferencesHelper.LOCALE, which, getActivity());
            }
        });

        alert = builder.create();
        alert.show();
    }

    private void deleteAutoCompletionHistory() {
        Context context = getActivity().getBaseContext();
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SimpleStorage<AutoCompletionData> autoCompletionNameStorage;
        SimpleStorage<AutoCompletionBrandData> autoCompletionBrandStorage;
        try {
            //create local autocompletion storage
            autoCompletionNameStorage = new SimpleStorageBase<>(databaseHelper.getAutoCompletionDao());
            autoCompletionBrandStorage = new SimpleStorageBase<>(databaseHelper.getAutoCompletionBrandDao());
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), getResources().getString(R.string.settings_option_3_error), Toast.LENGTH_LONG).show();
            return;
        }
        // delete all AutoCompletionData
        autoCompletionNameStorage.deleteAll();
        autoCompletionBrandStorage.deleteAll();
        AutoCompletionHelper.getAutoCompletionHelper(getActivity().getBaseContext()).reloadFromDatabase();

        Toast.makeText(getActivity(), getResources().getString(R.string.settings_option_3_success), Toast.LENGTH_LONG).show();
    }

    private void manageUnits() {

                      /*  AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
+            builder.setTitle(R.string.settings_option_3_createUnits);
+            builder.setView(fragment_unit_settings);
+            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
+                public void onClick(DialogInterface dialog, int position) {
+                            deleteAutoCompletionHistory();
+                    }
+            });
+               builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
+                public void onClick(DialogInterface dialog, int position) {
+                    // don't delete history of autocompletion
+                    return;
+                }
+            });
+            alert = builder.create();
+            alert.show();*/
        //Intent intent = new Intent(getActivity(), ManageUnitsGroupsActivity.class);
        ((SettingActivity) getActivity()).replaceFragments();

    }

    private void setApiEndpoint() {

        viewLauncher.showTextInputDialog(
                resourceProvider.getString(R.string.settings_option_4_APIEndPoint_Title),
                loadString(API_ENDPOINT, resourceProvider.getString(R.string.API_HOST), getActivity()),
                resourceProvider.getString(android.R.string.ok),
                new Command<String>() {
                    @Override
                    public void execute(String value) {
                        saveString(API_ENDPOINT, value, getActivity());
                    }
                },
                resourceProvider.getString(R.string.reset),
                new Command<String>() {
                    @Override
                    public void execute(String value) {
                        value = resourceProvider.getString(R.string.API_HOST);
                        saveString(API_ENDPOINT, value, getActivity());
                    }
                },
                resourceProvider.getString(android.R.string.cancel),
                NullCommand.StringInstance
        );

    }

    private void setLocationPermission(int position){

        if(objAdapter.getItem(position).isChecked()){
            objAdapter.getItem(position).setChecked(false);
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.LOCATION_PERMISSION,false,getActivity());
        } else {
            objAdapter.getItem(position).setChecked(true);
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.LOCATION_PERMISSION,true,getActivity());
        }
        objAdapter.notifyDataSetChanged();
    }

    private void setEnableSynchronization(int position) {

        boolean isChecked =objAdapter.getItem(position).isChecked();

        objAdapter.getItem(position).setChecked(!isChecked);
        SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.ENABLE_SYNCHRONIZATION, !isChecked, context);

        objAdapter.notifyDataSetChanged();
    }

    private void setItemDeletionSetting(int position){
        if(objAdapter.getItem(position).isChecked()){
            objAdapter.getItem(position).setChecked(false);
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.ITEM_DELETION_SHOW_AGAIN_MSG,false,getActivity());
        } else {
            objAdapter.getItem(position).setChecked(true);
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.ITEM_DELETION_SHOW_AGAIN_MSG,true,getActivity());
        }
        objAdapter.notifyDataSetChanged();
    }

    private void setShoppingListDeletionSetting(int position){
        if(objAdapter.getItem(position).isChecked()){
            objAdapter.getItem(position).setChecked(false);
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.SL_DELETION_SHOW_AGAIN_MSG,false,getActivity());
        } else {
            objAdapter.getItem(position).setChecked(true);
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.SL_DELETION_SHOW_AGAIN_MSG,true,getActivity());
        }
        objAdapter.notifyDataSetChanged();
    }

    private void setRecipeDeletionSetting(int position){
        if(objAdapter.getItem(position).isChecked()){
            objAdapter.getItem(position).setChecked(false);
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.RECIPE_DELETION_SHOW_AGAIN_MSG,false,getActivity());
        } else {
            objAdapter.getItem(position).setChecked(true);
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.RECIPE_DELETION_SHOW_AGAIN_MSG,true,getActivity());
        }
        objAdapter.notifyDataSetChanged();
    }

    private void setRecipeAddDefaultSetting(int position){
        if(objAdapter.getItem(position).isChecked()){
            objAdapter.getItem(position).setChecked(false);
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.ASK_TO_ADD_DEFAULT_RECIPES, false, getActivity());
        } else {
            objAdapter.getItem(position).setChecked(true);
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.ASK_TO_ADD_DEFAULT_RECIPES, true, getActivity());
        }
        objAdapter.notifyDataSetChanged();
    }

    private void setAskForLocationPermission(int position){
        if(objAdapter.getItem(position).isChecked()){
            objAdapter.getItem(position).setChecked(false);
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.LOCATION_PERMISSION_SHOW_AGAIN_MSG, false, getActivity());
        } else {
            objAdapter.getItem(position).setChecked(true);
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.LOCATION_PERMISSION_SHOW_AGAIN_MSG, true, getActivity());
        }
        objAdapter.notifyDataSetChanged();
    }


    private void setParserSeparatorWord(){

        viewLauncher.showTextInputDialog(getString(R.string.settings_option_7_parser_separate_word_name),
                SharedPreferencesHelper.loadString(SharedPreferencesHelper.ITEM_SEPARATOR_WORD, getString(R.string.item_divider), context),
                new Command<String>() {
                    @Override
                    public void execute(String word) {
                        for(int i = 0; i < word.length(); i++){
                            if(word.charAt(i) == ' '){
                                Toast.makeText(context,getString(R.string.settings_option_parser_separator_no_space_allowed), Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        saveString(ITEM_SEPARATOR_WORD, word, context);
                    }
                },
                new Command<String>() {
                    @Override
                    public void execute(String parameter) {

                    }
                });

    }

    private void startLoginActivity() {
        Intent intent = new Intent(context, LoginActivity.class);
        Bundle b = new Bundle();
        b.putBoolean("FORCE", true); //To make sure the Activity does not close immediately
        intent.putExtras(b);
        startActivity(intent);
    }

    private void selectSyncInterval() {

        final int defaultValue = resourceProvider.getInteger(R.integer.synchronizationInterval_default);
        final int currentValue = loadInt(SYNCHRONIZATION_INTERVAL, defaultValue, context);

        viewLauncher.showNumberInputDialog(

                // title and message
                resourceProvider.getString(R.string.settings_options_syncInterval_name),
                resourceProvider.getString(R.string.settings_options_syncInterval_inputBox_message),

                //current value
                currentValue,

                // ok button: save updated value
                resourceProvider.getString(android.R.string.ok), new Command<String>() {
                    @Override
                    public void execute(String value) {

                        try {

                            int intValue = Integer.parseInt(value);

                            if(intValue != currentValue && intValue > 0) {
                                saveInt(SYNCHRONIZATION_INTERVAL, intValue, context);
                                SyncingActivity.onSyncIntervalSettingChanged(intValue);
                            }


                        } catch (NumberFormatException ex) {
                            // should not happen, because showNumberInputDialog() only allows digits as input
                            // => just ignore the error
                        }

                    }
                },

                // reset to default value (neutral button)
                resourceProvider.getString(R.string.str_default),
                new Command<String>() {
                    @Override
                    public void execute(String parameter) {

                        if(defaultValue != currentValue) {
                            saveInt(SYNCHRONIZATION_INTERVAL, defaultValue, context);
                            SyncingActivity.onSyncIntervalSettingChanged(defaultValue);
                        }
                    }
                },

                // cancel button: do othign
                resourceProvider.getString(android.R.string.cancel),
                NullCommand.StringInstance
        );

    }


    private void selectRadius(){

        final int defaultValue = resourceProvider.getInteger(R.integer.supermarket_finder_radius);
        final int currentValue = loadInt(SUPERMARKET_FINDER_RADIUS, defaultValue, context);

        viewLauncher.showNumberInputDialog(

                // title and message
                resourceProvider.getString(R.string.radius),
                resourceProvider.getString(R.string.setting_change_radius_description),

                //current value
                currentValue,

                // ok button: save updated value
                resourceProvider.getString(android.R.string.ok), new Command<String>() {
                    @Override
                    public void execute(String value) {

                        try {

                            int intValue = Integer.parseInt(value);

                            if(intValue != currentValue && intValue > 0) {
                                saveInt(SUPERMARKET_FINDER_RADIUS, intValue, context);
                            }


                        } catch (NumberFormatException ex) {
                            // should not happen, because showNumberInputDialog() only allows digits as input
                            // => just ignore the error
                        }

                    }
                },

                // reset to default value (neutral button)
                resourceProvider.getString(R.string.str_default),
                new Command<String>() {
                    @Override
                    public void execute(String parameter) {

                        if(defaultValue != currentValue) {
                            saveInt(SUPERMARKET_FINDER_RADIUS, defaultValue, context);
                        }
                    }
                },

                // cancel button: do othign
                resourceProvider.getString(android.R.string.cancel),
                NullCommand.StringInstance
        );

    }


}
