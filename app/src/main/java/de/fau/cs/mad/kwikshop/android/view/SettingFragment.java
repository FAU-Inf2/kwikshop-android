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
import java.util.Locale;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.common.AutoCompletionBrandData;
import de.fau.cs.mad.kwikshop.android.common.AutoCompletionData;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.DatabaseHelper;
import de.fau.cs.mad.kwikshop.android.model.SimpleStorageBase;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;


public class SettingFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    public static String SETTINGS = "settings";
    public static String OPTION_1 = "locale";
    public static String OPTION_2 = "autocomplete";
    public static String OPTION_3 = "manage units and groups";
    public static CharSequence[] localeSelectionNames = {"Default", "English", "German", "Portuguese"};
    public static CharSequence[] localeIds = {"default", "en", "de", "pt"};

    private View rootView;
    private AlertDialog alert;
    private ArrayList setList;
    private ListView listView;
    @InjectView(R.layout.fragment_unit_settings)
    View fragment_unit_settings;
    @InjectView(R.id.unit_spinner)
    Spinner unit_spinner;



    public static SettingFragment newInstance(int sectionNumber) {

        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rootView = inflater.inflate(R.layout.fragment_setting, container, false);
        listView = (ListView) rootView.findViewById(android.R.id.list);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // local change
                changeLocalOption(position);

                // delete history of autocompletion
                deleteAutoCompletionHistoryOption(position);

                //manage units
                manageUnits(position);

            }
        });


        // set title for actionbar
        getActivity().setTitle(R.string.title_activity_settings);

        setList = new ArrayList<String>();
        setList.add(OPTION_1);
        setList.add(OPTION_2);
        setList.add(OPTION_3);
        setList.add(OPTION_4);

        SettingAdapter objAdapter = new SettingAdapter(getActivity(), R.layout.fragment_setting_row, setList);
        listView.setAdapter(objAdapter);

        return rootView;


    }

    private void changeLocalOption(int position){

        if (setList.get(position).equals(OPTION_1)) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            //int currentLocaleIdIndex = getActivity().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE).getInt(OPTION_1, 0);
            int currentLocaleIdIndex = SharedPreferencesHelper.loadInt(OPTION_1, 0, getActivity());
            builder.setTitle(R.string.settings_option_2_setlocale);
            builder.setSingleChoiceItems(localeSelectionNames, currentLocaleIdIndex, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    setLocale(localeIds[which].toString());
                    SharedPreferencesHelper.saveInt(OPTION_1, which, getActivity());
                    //getActivity().getSharedPreferences(SETTINGS, Context.MODE_PRIVATE).edit().putInt(OPTION_1, which).apply();

                }

            });

            alert = builder.create();
            alert.show();
        }

    }


    @Override
    public void onPause() {
        super.onPause();

        if(alert != null)
            alert.dismiss();

    }

    public void setLocale(String lang) {

        Locale setLocale = new Locale(lang);
        if(lang.equals("default"))
            setLocale = Locale.getDefault();
        Resources res = getActivity().getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = setLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(getActivity(), ListOfShoppingListsActivity.class);
        refresh.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startActivity(refresh);
        getActivity().finish();
    }


    public void deleteAutoCompletionHistoryOption(int position) {
        if (setList.get(position).equals(OPTION_2)) {
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
    private void manageUnits(int position){
        if (setList.get(position).equals(OPTION_3)) {
          /*  AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.settings_option_3_createUnits);
            builder.setView(fragment_unit_settings);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int position) {
                            deleteAutoCompletionHistory();
                    }
            });
               builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int position) {
                    // don't delete history of autocompletion
                    return;
                }
            });
            alert = builder.create();
            alert.show();*/
            //Intent intent = new Intent(getActivity(), ManageUnitsGroupsActivity.class);
            ((SettingActivity) getActivity()).replaceFragments();
        }
    }
}
