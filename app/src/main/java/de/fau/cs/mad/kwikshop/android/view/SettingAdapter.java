package de.fau.cs.mad.kwikshop.android.view;

import java.util.ArrayList;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;


import butterknife.ButterKnife;
import butterknife.InjectView;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.common.Setting;


public class SettingAdapter extends ArrayAdapter<Setting> {

    private ArrayList<Setting> settingsList;
    private int row;
    private Activity activity;

    @InjectView(R.id.tvsetname)
    TextView tvSetname;

    @InjectView(R.id.tvsetdesc)
    TextView tvSetdesc;

    @InjectView(R.id.setcheckbox)
    CheckBox checkbox;

    public SettingAdapter(Activity act, int resource, ArrayList<Setting> settingsList) {
        super(act, resource, settingsList);
        this.activity = act;
        this.row = resource;
        this.settingsList = settingsList;

    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {


        //if view is null, inflate a new one
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(row, null);
        }
        ButterKnife.inject(this, view);

        tvSetname.setText(settingsList.get(position).getName());
        tvSetdesc.setText(settingsList.get(position).getCaption());

        // setup checkbox
        if(settingsList.get(position).getViewVisibility() == View.INVISIBLE){
            checkbox.setVisibility(View.INVISIBLE);
        } else {
            checkbox.setVisibility(View.VISIBLE);
            checkbox.setChecked(settingsList.get(position).isChecked());
            checkbox.setFocusable(false);
            checkbox.setFocusableInTouchMode(false);
        }

        return view;
    }



}