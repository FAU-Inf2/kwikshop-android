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
        //make sure cases are listed in String.xml sort_by_array
        switch (parent.getItemAtPosition(pos).toString()){
            case "Manual": sortType = ItemSortType.MANUAL; break;
            case "Group": sortType = ItemSortType.GROUP; break;
            case "Alphabetically": sortType = ItemSortType.ALPHABETICALLY; break;
            default: sortType = ItemSortType.MANUAL;
        }

        EventBus.getDefault().post(sortType);

    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}
