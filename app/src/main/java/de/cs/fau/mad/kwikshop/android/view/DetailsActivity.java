package de.cs.fau.mad.kwikshop.android.view;

import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.View;

import cs.fau.mad.kwikshop_android.R;
import de.cs.fau.mad.kwikshop.android.view.interfaces.SaveCancelActivity;

public class DetailsActivity extends BaseActivity implements SaveCancelActivity {

    public static final String EXTRA_SHOPPINGLISTID = "extra_ShoppingListId";


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shopping_list_menu, menu);
        return true;
    }


    protected void showCustomActionBar() {

        //hide default action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        //show custom action bar
        View view = getLayoutInflater().inflate(R.layout.actionbar_save_cancel, null);
        actionBar.setCustomView(view);
    }


    @Override
    public View getSaveButton() {
        ActionBar actionBar = getSupportActionBar();
        View button = actionBar.getCustomView().findViewById(R.id.button_save);
        return button;
    }

    @Override
    public View getCancelButton() {

        ActionBar actionBar = getSupportActionBar();
        View button = actionBar.getCustomView().findViewById(R.id.button_remove);
        return button;
    }

}
