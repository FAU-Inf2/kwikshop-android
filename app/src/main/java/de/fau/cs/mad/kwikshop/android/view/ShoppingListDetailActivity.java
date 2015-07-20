package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;

import java.util.List;


public class ShoppingListDetailActivity extends DetailsActivity {



    public static Intent getIntent(Context context) {
        return new Intent(context, ShoppingListDetailActivity.class);
    }

    public static Intent getIntent(Context context, int shoppingListId) {
        return new Intent(context, ShoppingListDetailActivity.class)
                .putExtra(ShoppingListDetailActivity.EXTRA_SHOPPINGLISTID, (long) shoppingListId);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(frameLayout.getId(), ShoppingListDetailFragment.newInstance()).commit();
        }

        showCustomActionBar();

    }

    // This callback is invoked when the Speech Recognizer returns.
// This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

}
