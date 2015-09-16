package de.fau.cs.mad.kwikshop.android.view;

import android.os.Bundle;

public class TutorialActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(frameLayout.getId(), TutorialFragment.newInstance()).commit();
        }

    }
}
