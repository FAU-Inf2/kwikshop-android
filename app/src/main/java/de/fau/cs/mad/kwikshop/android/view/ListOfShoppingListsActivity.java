package de.fau.cs.mad.kwikshop.android.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import de.fau.cs.mad.kwikshop.android.R;

public class ListOfShoppingListsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String dataString = intent.getDataString();
        int id = -1;
        if(dataString != null) {
            for (int i = 0; i < dataString.length(); i++) {
                if (dataString.substring(i, i + 5).equals("/#id=")) {
                    String idString = dataString.substring(i + 5);
                    id = Integer.parseInt(idString);
                    break;
                }
            }
        }

        if (savedInstanceState == null) {
            if(id != -1){
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().add(R.id.content_frame, ShoppingListFragment.newInstance(id)).commit();
            }
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(R.id.content_frame, ListOfShoppingListsFragment.newInstance()).commit();
        }
    }


}
