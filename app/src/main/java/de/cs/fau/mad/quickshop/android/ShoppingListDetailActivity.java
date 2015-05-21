package de.cs.fau.mad.quickshop.android;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.Button;

import cs.fau.mad.quickshop_android.R;
import de.cs.fau.mad.quickshop.android.interfaces.ISaveCancelActivity;


public class ShoppingListDetailActivity extends BaseActivity implements ISaveCancelActivity {


    //region Constants

    public static final String EXTRA_SHOPPINGLISTID = "extra_ShoppingListId";

    //endregion

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
            fragmentManager.beginTransaction().add(frameLayout.getId(), ShoppingListDetailFragment.newInstance(1)).commit();
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
    public void setOnSaveClickListener(View.OnClickListener listener) {

        ActionBar actionBar = getSupportActionBar();
        Button button = (Button) actionBar.getCustomView().findViewById(R.id.button_save);
        button.setOnClickListener(listener);
    }

    @Override
    public void setOnCancelClickListener(View.OnClickListener listener) {

        ActionBar actionBar = getSupportActionBar();
        Button button = (Button) actionBar.getCustomView().findViewById(R.id.button_cancel);
        button.setOnClickListener(listener);
    }

    //endregion
}
