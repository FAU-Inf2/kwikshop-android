package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


public class ReminderActivity extends DetailsActivity {

    public static final String EXTRA_ITEMID = "extra_ItemId";

    public static Intent getIntent(Context context, int listId, int itemId) {
        return new Intent(context, ReminderActivity.class)
                .putExtra(EXTRA_SHOPPINGLISTID, (long) listId)
                .putExtra(EXTRA_ITEMID, (long) itemId);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (savedInstanceState == null) {

            Intent intent = getIntent();

            ReminderFragment fragment;

            int itemId = ((Long) intent.getExtras().get(EXTRA_ITEMID)).intValue();
            int listId = ((Long) intent.getExtras().get(EXTRA_SHOPPINGLISTID)).intValue();

            fragment = ReminderFragment.newInstance(listId, itemId);


            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(frameLayout.getId(), fragment).commit();
        }

        showCustomActionBar();
    }


}
