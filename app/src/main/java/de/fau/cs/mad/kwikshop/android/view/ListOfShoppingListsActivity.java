package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import javax.inject.Inject;

import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.model.RegularlyRepeatHelper;
import de.fau.cs.mad.kwikshop.android.model.tasks.RedeemSharingCodeTask;
import de.fau.cs.mad.kwikshop.common.ShoppingList;

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
        baseViewModel.setCurrentActivityName(this.getClass().getSimpleName());

        //Get Shopping List ID

        Intent intent = getIntent();
        String dataString = intent.getDataString();
        int listId = -1;
        if (dataString != null) {
            //dataString is not null if Activity was opened by intent filter (calender)

            // Calendar
            for (int i = 0; i < dataString.length() - this.getString(R.string.intent_id_separator).length(); i++) {
                if (dataString.substring(i, i + 5).equals(this.getString(R.string.intent_id_separator))) {
                    String idString = dataString.substring(i + 5);
                    listId = Integer.parseInt(idString);
                    break;
                }
            }

            if(listId != -1) {
                intent = new Intent(getApplicationContext(), ShoppingListActivity.class);
                intent.putExtra(ShoppingListActivity.SHOPPING_LIST_ID, listId);
                startActivity(intent);
            }

            for (int i = 0; i < dataString.length() - this.getString(R.string.intent_share_separator).length(); i++) {
                if (dataString.substring(i, i + 5).equals(this.getString(R.string.intent_share_separator))) {
                    String scString = dataString.substring(i + 5);
                    new RedeemSharingCodeTask(this, this).execute(scString);
                    break;
                }
            }


        }

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
