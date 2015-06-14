package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


public class ItemDetailsActivity extends DetailsActivity {

    public static final String EXTRA_ITEMID = "extra_ItemId";


    public static Intent getIntent(Context context, int shoppingListId) {
        return new Intent(context, ItemDetailsActivity.class)
                .putExtra(EXTRA_SHOPPINGLISTID, (long) shoppingListId);
    }

    public static Intent getIntent(Context context, int shoppingListId, int itemId) {
        return new Intent(context, ItemDetailsActivity.class)
                .putExtra(EXTRA_SHOPPINGLISTID, (long) shoppingListId)
                .putExtra(EXTRA_ITEMID, (long) itemId);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (savedInstanceState == null) {

            Intent intent = getIntent();
            int listId = ((Long) intent.getExtras().get(EXTRA_SHOPPINGLISTID)).intValue();

            ItemDetailsFragment fragment;
            if (intent.hasExtra(EXTRA_ITEMID)) {
                int itemId = ((Long) intent.getExtras().get(EXTRA_ITEMID)).intValue();
                fragment = ItemDetailsFragment.newInstance(listId, itemId);
            } else {
                fragment = ItemDetailsFragment.newInstance(listId);
            }

            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(frameLayout.getId(), fragment).commit();
        }

        showCustomActionBar();
    }


}
