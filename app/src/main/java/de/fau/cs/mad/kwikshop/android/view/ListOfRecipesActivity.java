package de.fau.cs.mad.kwikshop.android.view;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import de.fau.cs.mad.kwikshop.android.R;

public class ListOfRecipesActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        baseViewModel.setCurrentActivityName(this.getClass().getSimpleName());

        if(savedInstanceState == null){
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(R.id.content_frame, ListOfRecipesFragment.newInstance()).commit();
        }
    }


}
