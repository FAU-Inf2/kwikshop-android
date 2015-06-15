package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import fau.cs.mad.kwikshop_android.R;
import de.fau.cs.mad.kwikshop.android.model.messages.MoveAllItemsEvent;
import de.greenrobot.event.EventBus;

public class ShoppingListActivity extends BaseActivity {


    private static final String SHOPPING_LIST_ID = "shopping_list_id";


    public static Intent getIntent(Context context, int shoppingListId) {

        Intent intent = new Intent(context, ShoppingListActivity.class);
        intent.putExtra(SHOPPING_LIST_ID, (int) shoppingListId);
        return intent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.shoppinglist_replacement_menu, menu);
        getMenuInflater().inflate(R.menu.shoppinglist_item_movement_menu, menu);
        //menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.overflow_action_menu_move_all_to_shopping_cart);
        // TODO: change this. We should not jump over the onCreateOptionsMenu method of BaseActivity
        return onCreateOptionsMenuSuper(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        ItemSortType type = null;
        switch (item.getItemId()){
            case R.id.sort_by_group_option: type = ItemSortType.GROUP; break;
            case R.id.sort_by_alphabet_option: type = ItemSortType.ALPHABETICALLY; break;
            case R.id.action_move_all_to_shopping_cart:
                EventBus.getDefault().post(MoveAllItemsEvent.moveAllToBoughtEvent);
                break;
            case R.id.action_move_all_from_shopping_cart:
                EventBus.getDefault().post(MoveAllItemsEvent.moveAllFromBoughtEvent);
                break;
        }
        if(type != null) EventBus.getDefault().post(type);

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Get Shopping List ID

            Bundle extras = getIntent().getExtras();
            int id = extras.getInt(SHOPPING_LIST_ID);

        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(frameLayout.getId(), ShoppingListFragment.newInstance(id)).commit();
        }
    }


}
