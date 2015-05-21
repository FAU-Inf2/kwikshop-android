package de.cs.fau.mad.quickshop.android;

import android.os.Bundle;

public class ListOfShoppingListsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set layout in frameLayout from BaseActivity to access the navigation Drawer
        // do not use setContentView()!

        mDrawerList.setItemChecked(position, true);
        setTitle(listArray.get(position));

        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(frameLayout.getId(), ListOfShoppingListsFragment.newInstance(1)).commit();
        }
    }


}
