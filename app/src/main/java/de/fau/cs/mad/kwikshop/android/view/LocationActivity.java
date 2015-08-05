package de.fau.cs.mad.kwikshop.android.view;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import de.fau.cs.mad.kwikshop.android.R;

public class LocationActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(frameLayout.getId(), LocationFragment.newInstance()).commit();
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem location = menu.findItem(R.id.action_location);
        location.setVisible(false);

        return true;
    }


}
