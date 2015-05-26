package de.cs.fau.mad.quickshop.android.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

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
            fragmentManager.beginTransaction().add(frameLayout.getId(), ShoppingListFragment.newInstance(id)).commit();
        }
    }


}
