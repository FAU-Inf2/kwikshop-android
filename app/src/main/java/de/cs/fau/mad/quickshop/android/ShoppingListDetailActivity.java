package de.cs.fau.mad.quickshop.android;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import cs.fau.mad.quickshop_android.R;

public class ShoppingListDetailActivity extends ActionBarActivity {

    //region Overrides

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list_detail);

        showActionBar();


        TextView textView_ShoppingListName = (TextView) findViewById(R.id.textView_ShoppingListName);
        textView_ShoppingListName.setFocusable(true);
        textView_ShoppingListName.setFocusableInTouchMode(true);
        textView_ShoppingListName.requestFocus();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        attachEventHandlers();

    }

    //endregion


    //region Private Methods

    private void showActionBar() {

        //hide default action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled (false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);


        //show custom action bar
        View view = getLayoutInflater().inflate(R.layout.actionbar_save_cancel, null);
        actionBar.setCustomView(view);
    }


    private void attachEventHandlers() {

        ActionBar actionBar = getSupportActionBar();

        Button cancelButton = (Button) actionBar.getCustomView().findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancel();
            }
        });


        Button saveButton= (Button) actionBar.getCustomView().findViewById(R.id.button_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave();
            }
        });
    }


    private void onCancel() {
        finish();
    }

    private void onSave() {
        finish();
    }

    //endregion



}
