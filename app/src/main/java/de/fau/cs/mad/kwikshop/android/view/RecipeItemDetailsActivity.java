package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class RecipeItemDetailsActivity  extends DetailsActivity {

    public static final String EXTRA_ITEMID = "extra_ItemId";


    public static Intent getIntent(Context context, int recipeId) {
        return new Intent(context, RecipeItemDetailsActivity.class)
                .putExtra(EXTRA_RECIPEID, (long) recipeId);
    }

    public static Intent getIntent(Context context, int recipeId, int itemId) {
        return new Intent(context, RecipeItemDetailsActivity.class)
                .putExtra(EXTRA_RECIPEID, (long) recipeId)
                .putExtra(EXTRA_ITEMID, (long) itemId);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (savedInstanceState == null) {

            Intent intent = getIntent();
            int recipeId = ((Long) intent.getExtras().get(EXTRA_RECIPEID)).intValue();

            RecipeItemDetailsFragment fragment;
            if (intent.hasExtra(EXTRA_ITEMID)) {
                int itemId = ((Long) intent.getExtras().get(EXTRA_ITEMID)).intValue();
                fragment = RecipeItemDetailsFragment.newInstance(recipeId, itemId);
            } else {
                fragment = RecipeItemDetailsFragment.newInstance(recipeId);
            }

            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(frameLayout.getId(), fragment).commit();
        }

        showCustomActionBar();
    }


}
