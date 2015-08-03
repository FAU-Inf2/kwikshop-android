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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

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

    private static final String ARG_SECTION_NUMBER = "section_number";
    public static String SETTINGS = "settings";
    public static CharSequence[] localeSelectionNames = {"Default", "English", "German", "Portuguese", "Russian"};
    public static CharSequence[] localeIds = {"default", "en", "de", "pt", "ru"};

    private View rootView;
    private AlertDialog alert;
    private ListView listView;
    private Context context;
    private ArrayList<Setting> settingsList;
    private SettingAdapter objAdapter;


    private Setting apiEndpointSetting;
    private Setting locationPermission;
    private Setting setLocale;
    private Setting setAutoCompletionDeletion;
    private Setting setManageUnits;


    @Inject
    ViewLauncher viewLauncher;

    @Inject
    ResourceProvider resourceProvider;

    public static SettingFragment newInstance(int sectionNumber) {
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(getActivity()));
        objectGraph.inject(this);

        context = getActivity().getApplicationContext();

        rootView = inflater.inflate(R.layout.fragment_setting, container, false);
        listView = (ListView) rootView.findViewById(android.R.id.list);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // local change
                if (settingsList.get(position).equals(setLocale))
                    changeLocalOption();


                // delete history of autocompletion
                if (settingsList.get(position).equals(setAutoCompletionDeletion))
                    deleteAutoCompletionHistoryOption();

                // manage units
                if (settingsList.get(position).equals(setManageUnits))
                    manageUnits(position);

                // set API endpoint
                if(settingsList.get(position).equals(apiEndpointSetting))
                    setApiEndpoint();

                // location permission
                if(settingsList.get(position).equals(locationPermission)){
                    setLocationPermission(position);
                }



            }
        });





        // set title for actionbar
        getActivity().setTitle(R.string.title_activity_settings);

        // list of settings
        settingsList = new ArrayList<>();

        // Locale Setting
        setLocale = new Setting(context);
        setLocale.setName(R.string.settings_option_2_setlocale);
        setLocale.setCaption(R.string.settings_option_2_desc);
        settingsList.add(setLocale);

        // Autocompletion deletion
        setAutoCompletionDeletion = new Setting(context);
        setAutoCompletionDeletion.setName(R.string.settings_option_3_deleteHistory);
        setAutoCompletionDeletion.setCaption(R.string.settings_option_3_desc);
        settingsList.add(setAutoCompletionDeletion);

        // manage units
        setManageUnits = new Setting(context);
        setManageUnits.setName(R.string.settings_option_3_manageUnits);
        setManageUnits.setCaption(R.string.settings_option_3_desc2);
        settingsList.add(setManageUnits);

        // API endpoint settings
        apiEndpointSetting = new Setting(context);
        apiEndpointSetting.setName(R.string.settings_option_4_APIEndPoint_Title);
        apiEndpointSetting.setCaption(R.string.settings_option_4_APIEndPoint_Desc);
        settingsList.add(apiEndpointSetting);

        // permission for location tracking
        locationPermission = new Setting(context);
        locationPermission.setName(R.string.settings_option_5_location_permission_title);
        locationPermission.setCaption(R.string.settings_option_5_location_permission_desc);
        locationPermission.setChecked(SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.LOCATION_PERMISSION,false,getActivity()));
        locationPermission.setViewVisibility(View.VISIBLE);
        settingsList.add(locationPermission);

        // Adapter for settings view
        objAdapter = new SettingAdapter(getActivity(), R.layout.fragment_setting_row, settingsList);
        listView.setAdapter(objAdapter);

        return rootView;
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
                // don't delete history of autocompletion
                return;
            }
        });

        alert = builder.create();
        alert.show();

    }

    private void deleteAutoCompletionHistory() {
        Context context = getActivity().getBaseContext();
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SimpleStorage<AutoCompletionData> autoCompletionNameStorage = null;
        SimpleStorage<AutoCompletionBrandData> autoCompletionBrandStorage = null;
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

    private void manageUnits(int position) {

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

    @SuppressWarnings("unchecked")
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


}
