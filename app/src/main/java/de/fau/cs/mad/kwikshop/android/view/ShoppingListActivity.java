package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import de.fau.cs.mad.kwikshop.android.R;
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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        //add recipes
        menu.getItem(1).getSubMenu().getItem(2).setVisible(true);
        //mark everything as bought
        menu.getItem(1).getSubMenu().getItem(6).setVisible(true);
        //mark nothing as bought
        menu.getItem(1).getSubMenu().getItem(7).setVisible(true);
        return true;
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
            case R.id.action_add_recipe:
                Bundle extras = getIntent().getExtras();
                int id = extras.getInt(SHOPPING_LIST_ID);

                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().add(frameLayout.getId(), AddRecipeToShoppingListFragment.newInstance(id)).commit();
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
