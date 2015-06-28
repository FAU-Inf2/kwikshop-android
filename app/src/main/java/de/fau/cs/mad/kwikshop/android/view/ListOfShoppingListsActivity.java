package de.fau.cs.mad.kwikshop.android.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.model.RegularlyRepeatHelper;
import de.fau.cs.mad.kwikshop.android.model.messages.ReminderTimeIsOverEvent;
import de.greenrobot.event.EventBus;

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
