package de.fau.cs.mad.kwikshop.android.view;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


import de.fau.cs.mad.kwikshop.android.R;


public class SettingActivity extends BaseActivity {


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

    public void replaceFragments() {
        // Create new fragment and transaction
        ManageUnitGroupFragment newFragment = new ManageUnitGroupFragment();
        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.content_frame, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem settings = menu.findItem(R.id.action_settings);
        settings.setVisible(false);

        return true;
    }


}
