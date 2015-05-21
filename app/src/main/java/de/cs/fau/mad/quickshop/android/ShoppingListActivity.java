package de.cs.fau.mad.quickshop.android;

import android.os.Bundle;

public class ShoppingListActivity extends BaseActivity {


    private static final String SHOPPING_LIST_ID = "shopping_list_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set layout in frameLayout from BaseActivity to access the navigation Drawer
        // do not use setContentView()!

        mDrawerList.setItemChecked(position, true);
        setTitle(listArray.get(position));

        //Get Shopping List ID


            Bundle extras = getIntent().getExtras();
            int id = extras.getInt(SHOPPING_LIST_ID);



        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(frameLayout.getId(), ShoppingListFragment.newInstance(0,id)).commit();
        }
    }


}