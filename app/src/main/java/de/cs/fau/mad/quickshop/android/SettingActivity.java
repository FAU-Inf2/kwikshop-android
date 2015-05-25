package de.cs.fau.mad.quickshop.android;


import android.os.Bundle;

import cs.fau.mad.quickshop_android.R;


public class SettingActivity extends BaseActivity  {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set layout in frameLayout from BaseActivity to access the navigation Drawer
        // do not use setContentView()!



        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(R.id.content_frame, SettingFragment.newInstance(0)).commit();
        }

    }



}
