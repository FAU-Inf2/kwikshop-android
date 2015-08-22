package de.fau.cs.mad.kwikshop.android.view;

import android.app.AlertDialog;
import android.content.ContentResolver;
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
    private Setting parserSeparatorWordSetting;
    private Setting loginSetting;
    private Setting enableSyncSetting;
    private Setting syncNowSetting;

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

        //Choose separator word for the parser
        parserSeparatorWordSetting = new Setting(context);
        parserSeparatorWordSetting.setName(R.string.settings_option_7_parser_separate_word_name);
        parserSeparatorWordSetting.setCaption(R.string.settings_option_7_parser_separate_word_descr);

        //LoginActivity
        loginSetting = new Setting(context);
        loginSetting.setName(R.string.settings_option_8_login_name);
        loginSetting.setCaption(R.string.settings_option_8_login_descr);

        enableSyncSetting = new Setting(context);
        enableSyncSetting.setName(R.string.settings_option_enableSync_name);
        enableSyncSetting.setCaption(R.string.settings_option_enableSync_descr);
        enableSyncSetting.setChecked(SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.ENABLE_SYNCHRONIZATION, true, context));
        enableSyncSetting.setViewVisibility(View.VISIBLE);

        syncNowSetting = new Setting(context);
        syncNowSetting.setName(R.string.settings_option_sync_name);
        syncNowSetting.setCaption(R.string.settings_option_sync_descr);

        // add all settings to the list of settins

        // list of settings
        settingsList = new ArrayList<>(Arrays.asList(new Setting[]
                {
                    localeSetting,
                    autoCompletionDeletionSetting,
                    itemDeletionSetting,
                    parserSeparatorWordSetting,
                    manageUnitsSetting,
                    locationPermissionSetting,
                    enableSyncSetting,
                    syncNowSetting,
                    loginSetting,
                    apiEndpointSetting
                }));


        // Adapter for settings view
        objAdapter = new SettingAdapter(getActivity(), R.layout.fragment_setting_row, settingsList);
        listView.setAdapter(objAdapter);

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (alert != null)
            alert.dismiss();
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
        Intent refresh = new Intent(getActivity(), SettingActivity.class);
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

    private void setParserSeparatorWord(){

        viewLauncher.showTextInputDialog(getString(R.string.settings_option_7_parser_separate_word_name),
                SharedPreferencesHelper.loadString(SharedPreferencesHelper.ITEM_SEPARATOR_WORD, getString(R.string.item_divider), context),
                new Command<String>() {
                    @Override
                    public void execute(String word) {
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

}
