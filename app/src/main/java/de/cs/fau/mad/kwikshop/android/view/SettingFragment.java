package de.cs.fau.mad.kwikshop.android.view;

import android.app.AlertDialog;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import cs.fau.mad.kwikshop_android.R;


public class SettingFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private String SETTINGS = "settings";
    private String OPTION_1 = "option_1";
    private String OPTION_2 = "option_2";
    private View rootView;
    private AlertDialog alert;

    private ArrayList setList;

    private ListView listView;
    private TextView tvSetname;
    private TextView tvSetdesc;
    private CheckBox checkbox;


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

        tvSetname = (TextView) listView.findViewById(R.id.tvsetname);
        tvSetdesc = (TextView) listView.findViewById(R.id.tvsetdesc);
        checkbox = (CheckBox) listView.findViewById(R.id.setcheckbox);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // dummy option 1
                dummyOption(position);

                // local change
                changeLocalOption(position);

            }
        });


        // set title for actionbar
        getActivity().setTitle(R.string.title_activity_settings);

        setList = new ArrayList<String>();
       // setList.add(OPTION_1);
        setList.add(OPTION_2);

        SettingAdapter objAdapter = new SettingAdapter(getActivity(), R.layout.fragment_setting_row, setList);
        listView.setAdapter(objAdapter);

        return rootView;


    }

    private void changeLocalOption(int position){

        if (setList.get(position).equals(OPTION_2)) {

            final CharSequence[] localeSelectionNames = {"english", "deutsch"};
            final CharSequence[] localeIDs = {"en", "de"};

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.settings_option_2_setlocale);
            builder.setSingleChoiceItems(localeSelectionNames, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getActivity(), "Test", Toast.LENGTH_LONG);
                    setLocale(localeIDs[which].toString());
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
        Resources res = getActivity().getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = setLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(getActivity(), SettingActivity.class);
        getActivity().startActivity(refresh);
    }


    public void dummyOption(int position){

        if (setList.get(position).equals(OPTION_1)) {


            if (getActivity().getSharedPreferences(SETTINGS, 0).getBoolean(OPTION_1, true) == false) {
                checkbox.setChecked(true);
            } else
                checkbox.setChecked(false);

            checkbox.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                getActivity().getSharedPreferences(SETTINGS, 0).edit().putBoolean(OPTION_1, false).apply();
                            } else {
                                getActivity().getSharedPreferences(SETTINGS, 0).edit().putBoolean(OPTION_1, true).apply();
                            }
                        }
                    }
            );
        }
    }


}
