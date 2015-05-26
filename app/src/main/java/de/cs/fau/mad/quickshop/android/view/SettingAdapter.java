package de.cs.fau.mad.quickshop.android.view;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import cs.fau.mad.quickshop_android.R;


public class SettingAdapter extends ArrayAdapter<String> {

    private ArrayList<String> setList;
    private int row;
    private Activity activity;
    private String SETTINGS = "settings";
    private String OPTION_ONE = "option_one";

    private TextView tvSetname;
    private TextView tvSetdesc;
    private CheckBox checkbox;

    public SettingAdapter(Activity act, int resource,
                          ArrayList<String> setList) {
        super(act, resource, setList);
        this.activity = act;
        this.row = resource;
        this.setList = setList;

    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {


        //if view is null, inflate a new one
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(row, null);
        }

        tvSetname = (TextView) view.findViewById(R.id.tvsetname);
        tvSetdesc = (TextView) view.findViewById(R.id.tvsetdesc);
        checkbox = (CheckBox) view.findViewById(R.id.setcheckbox);


        if (setList.get(position).equals(OPTION_ONE)) {

            tvSetname.setText(R.string.settings_option_1_name);
            tvSetdesc.setText(R.string.settings_option_1_desc);
            checkbox.setVisibility(View.VISIBLE);

            if (activity.getSharedPreferences(SETTINGS, 0).getBoolean(OPTION_ONE, true) == false) {
                checkbox.setChecked(true);
            } else
                checkbox.setChecked(false);

            checkbox.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {

                                activity.getSharedPreferences(SETTINGS, 0).edit().putBoolean(OPTION_ONE, false).apply();

                            } else {

                                activity.getSharedPreferences(SETTINGS, 0).edit().putBoolean(OPTION_ONE, true).apply();

                            }


                        }
                    }
            );
        }


        return view;
    }


}