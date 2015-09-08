package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import de.fau.cs.mad.kwikshop.android.R;

public class RecipeActivity extends BaseActivity {


    private static final String RECIPE_ID = "recipe_id";


    public static Intent getIntent(Context context, int recipeId) {

        Intent intent = new Intent(context, RecipeActivity.class);
        intent.putExtra(RECIPE_ID, recipeId);
        return intent;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.recipe_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get Shopping List ID
        Bundle extras = getIntent().getExtras();
        int id = extras.getInt(RECIPE_ID);

        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(frameLayout.getId(), RecipeFragment.newInstance(id)).commit();
        }
    }


}