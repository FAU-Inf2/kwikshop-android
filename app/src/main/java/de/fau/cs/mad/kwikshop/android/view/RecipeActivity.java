package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import de.fau.cs.mad.kwikshop.android.R;

public class RecipeActivity extends BaseActivity {


    private static final String RECIPE_ID = "recipe_id";


    public static Intent getIntent(Context context, int recipeId) {

        Intent intent = new Intent(context, RecipeActivity.class);
        intent.putExtra(RECIPE_ID, (int) recipeId);
        return intent;
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem addRecipe = menu.findItem(R.id.action_add_to_a_shoppinglist);
        addRecipe.setVisible(true);

        return true;
    }


}