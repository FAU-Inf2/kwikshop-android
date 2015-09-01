package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import de.fau.cs.mad.kwikshop.android.R;
import de.greenrobot.event.EventBus;

public class LocationActivity extends BaseActivity {

    public static Intent getIntent(Context context) {
        return new Intent(context, ListOfShoppingListsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(frameLayout.getId(), LocationFragment.newInstance()).commit();
        }

    }


   

}
