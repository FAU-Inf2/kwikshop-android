package de.cs.fau.mad.quickshop.android.view;


import android.os.Bundle;


public class SettingActivity extends BaseActivity  {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set layout in frameLayout from BaseActivity to access the navigation Drawer
        // do not use setContentView()!


        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(frameLayout.getId(), SettingFragment.newInstance(0)).commit();
        }

    }



}
