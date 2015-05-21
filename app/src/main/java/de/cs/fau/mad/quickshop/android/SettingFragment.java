package de.cs.fau.mad.quickshop.android;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cs.fau.mad.quickshop_android.R;

/**
 * Created by Robert on 21.05.2015.
 */
public class SettingFragment extends Fragment{

    private static final String ARG_SECTION_NUMBER = "section_number";
    private View rootView;


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

        return rootView;




    }
}
