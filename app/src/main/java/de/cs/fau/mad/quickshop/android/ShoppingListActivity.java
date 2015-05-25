package de.cs.fau.mad.quickshop.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import cs.fau.mad.quickshop_android.R;

public class ShoppingListActivity extends BaseActivity {


    private static final String SHOPPING_LIST_ID = "shopping_list_id";


    public static Intent getIntent(Activity activity, int shoppingListId) {

        Intent intent = new Intent(activity, ShoppingListActivity.class);
        intent.putExtra(SHOPPING_LIST_ID, (int) shoppingListId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Get Shopping List ID

            Bundle extras = getIntent().getExtras();
            int id = extras.getInt(SHOPPING_LIST_ID);

        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(R.id.content_frame, ShoppingListFragment.newInstance(0,id)).commit();
        }
    }


}
