package de.fau.cs.mad.kwikshop.android.view;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import de.fau.cs.mad.kwikshop.android.R;


public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        baseViewModel.setCurrentActivityName(this.getClass().getSimpleName());

        if(savedInstanceState == null){
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(R.id.content_frame, AboutFragment.newInstance()).commit();
        }
    }

}
