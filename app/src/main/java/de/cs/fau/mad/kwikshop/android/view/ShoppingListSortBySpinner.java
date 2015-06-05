package de.cs.fau.mad.kwikshop.android.view;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import cs.fau.mad.kwikshop_android.R;
import de.greenrobot.event.EventBus;

public class ShoppingListSortBySpinner extends Activity implements AdapterView.OnItemSelectedListener{

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        ItemSortType sortType;
        //make sure cases match the order of the spinner, see sortbynames.xml and ShoppingListFragment.java
        switch (pos){
            case 0: sortType = ItemSortType.MANUAL; break;
            case 1: sortType = ItemSortType.GROUP; break;
            case 2: sortType = ItemSortType.ALPHABETICALLY; break;
            default: sortType = ItemSortType.MANUAL;
        }

        EventBus.getDefault().post(sortType);

    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}
