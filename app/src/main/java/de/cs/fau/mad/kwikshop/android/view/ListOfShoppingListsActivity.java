package de.cs.fau.mad.kwikshop.android.view;

import android.os.Bundle;

import cs.fau.mad.kwikshop_android.R;

public class ListOfShoppingListsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // disable go back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(R.id.content_frame, ListOfShoppingListsFragment.newInstance()).commit();
        }
    }


}
