package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class RecipeDetailActivity extends DetailsActivity {



    public static Intent getIntent(Context context) {
        return new Intent(context, RecipeDetailActivity.class);
    }

    public static Intent getIntent(Context context, int recipeId) {
        return new Intent(context, RecipeDetailActivity.class)
                .putExtra(RecipeDetailActivity.EXTRA_RECIPEID, (long) recipeId);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(frameLayout.getId(), RecipeDetailFragment.newInstance()).commit();
        }

        showCustomActionBar();

    }



}

