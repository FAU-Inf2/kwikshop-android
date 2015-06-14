package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


public class ShoppingListDetailActivity extends DetailsActivity {



    public static Intent getIntent(Context context) {
        return new Intent(context, ShoppingListDetailActivity.class);
    }

    public static Intent getIntent(Context context, int shoppingListId) {
        return new Intent(context, ShoppingListDetailActivity.class)
                .putExtra(ShoppingListDetailActivity.EXTRA_SHOPPINGLISTID, (long) shoppingListId);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(frameLayout.getId(), ShoppingListDetailFragment.newInstance()).commit();
        }

        showCustomActionBar();

    }



}
