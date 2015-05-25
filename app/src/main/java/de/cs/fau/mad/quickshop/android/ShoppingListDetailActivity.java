package de.cs.fau.mad.quickshop.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.Button;

import cs.fau.mad.quickshop_android.R;
import de.cs.fau.mad.quickshop.android.view.interfaces.SaveCancelActivity;


public class ShoppingListDetailActivity extends BaseActivity implements SaveCancelActivity {


    //region Constants

    public static final String EXTRA_SHOPPINGLISTID = "extra_ShoppingListId";

    //endregion


    public static Intent getIntent(Activity activity) {
        return new Intent(activity, ShoppingListDetailActivity.class);
    }

    public static Intent getIntent(Activity activity, int shoppingListId) {
        return new Intent(activity, ShoppingListDetailActivity.class)
                .putExtra(ShoppingListDetailActivity.EXTRA_SHOPPINGLISTID, (long) shoppingListId);
    }

    //region Overrides

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set layout in frameLayout from BaseActivity to access the navigation Drawer
        // do not use setContentView()!

        //.setItemChecked(position, true);
        //setTitle(listArray.get(position));

        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(frameLayout.getId(), ShoppingListDetailFragment.newInstance()).commit();
        }

        showActionBar();

    }

    private void showActionBar() {

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

    //region ISaveCancelActivity Implementation

    @Override
    public View getSaveButton() {
        ActionBar actionBar = getSupportActionBar();
        Button button = (Button) actionBar.getCustomView().findViewById(R.id.button_save);
        return button;
    }

    @Override
    public View getCancelButton() {

        ActionBar actionBar = getSupportActionBar();
        Button button = (Button) actionBar.getCustomView().findViewById(R.id.button_cancel);
        return button;
    }

    //endregion
}
