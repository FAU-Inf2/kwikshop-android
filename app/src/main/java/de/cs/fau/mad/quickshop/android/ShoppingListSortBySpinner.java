package de.cs.fau.mad.quickshop.android;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;

/**
 * Created by Nicolas on 19/05/2015.
 */
public class ShoppingListSortBySpinner extends Activity implements AdapterView.OnItemSelectedListener {
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}
