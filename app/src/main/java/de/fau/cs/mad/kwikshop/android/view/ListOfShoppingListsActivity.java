package de.fau.cs.mad.kwikshop.android.view;

import android.os.Bundle;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.RegularlyRepeatHelper;

public class ListOfShoppingListsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(R.id.content_frame, ListOfShoppingListsFragment.newInstance()).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        RegularlyRepeatHelper repeatHelper = RegularlyRepeatHelper.getRegularlyRepeatHelper(this);
        repeatHelper.checkIfReminderIsOver();
    }

}
