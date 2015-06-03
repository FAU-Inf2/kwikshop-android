package de.cs.fau.mad.kwikshop.android.view;

import java.util.ArrayList;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import cs.fau.mad.kwikshop_android.R;


public class SettingAdapter extends ArrayAdapter<String>  {

    private ArrayList<String> setList;
    private int row;
    private Activity activity;
    private String OPTION_1 = "option_1";
    private String OPTION_2 = "option_2";

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



        if (setList.get(position).equals(OPTION_1)) {

            tvSetname.setText(R.string.settings_option_1_name);
            tvSetdesc.setText(R.string.settings_option_1_desc);
            checkbox.setVisibility(View.VISIBLE);
        }

        if (setList.get(position).equals(OPTION_2)) {

            tvSetname.setText(R.string.settings_option_2_setlocale);
            tvSetdesc.setText(R.string.settings_option_2_desc);
            checkbox.setVisibility(View.INVISIBLE);
        }



        return view;
    }







}