package de.fau.cs.mad.kwikshop.android.view;

import java.util.ArrayList;
import java.util.TreeSet;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;


import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.common.Setting;


public class SettingAdapter extends ArrayAdapter<Setting> {

    private ArrayList<Setting> settingsList;
    private int row;
    private Activity activity;

    @Optional
    @InjectView(R.id.tv_settings_name)
    TextView tvSettingsName;

    @Optional
    @InjectView(R.id.tv_settings_desc)
    TextView tvSettingsDesc;

    @Optional
    @InjectView(R.id.tv_header)
    TextView tvSettingsHeader;

    @Optional
    @InjectView(R.id.cb_settings)
    CheckBox checkbox;

    public SettingAdapter(Activity act, int resource, ArrayList<Setting> settingsList) {
        super(act, resource, settingsList);
        this.activity = act;
        this.row = resource;
        this.settingsList = settingsList;

    }


    @Override
    public View getView(int position, View view, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (!settingsList.get(position).isHeader()) {

            view = inflater.inflate(row, null);
            ButterKnife.inject(this, view);
            tvSettingsName.setText(settingsList.get(position).getName());
            tvSettingsDesc.setText(settingsList.get(position).getCaption());

            // setup checkbox
            if (settingsList.get(position).getViewVisibility() == View.INVISIBLE) {
                checkbox.setVisibility(View.INVISIBLE);
            } else {
                checkbox.setVisibility(View.VISIBLE);
                checkbox.setChecked(settingsList.get(position).isChecked());
                checkbox.setFocusable(false);
                checkbox.setFocusableInTouchMode(false);
            }

        } else {

            view = inflater.inflate(R.layout.fragment_setting_header, null);
            ButterKnife.inject(this, view);
            view.setEnabled(false);
            view.setOnClickListener(null);
            tvSettingsHeader.setText(settingsList.get(position).getName());

        }

        return view;
    }


}