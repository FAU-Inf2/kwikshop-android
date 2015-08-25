package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import javax.inject.Inject;

import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.model.RegularlyRepeatHelper;

public class ListOfShoppingListsActivity extends BaseActivity {

    private RegularlyRepeatHelper repeatHelper;

    public static Intent getIntent(Context context) {
        return new Intent(context, ListOfShoppingListsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new ListStorageFragment().SetupLocalListStorageFragment(this);
        repeatHelper = ObjectGraph.create(new KwikShopModule(this)).get(RegularlyRepeatHelper.class);

        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(R.id.content_frame, ListOfShoppingListsFragment.newInstance()).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        repeatHelper.checkIfReminderIsOver();
    }



}
