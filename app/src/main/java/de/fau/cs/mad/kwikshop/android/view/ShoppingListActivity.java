package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.messages.MoveAllItemsEvent;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.greenrobot.event.EventBus;

public class ShoppingListActivity extends BaseActivity {


    private static final String SHOPPING_LIST_ID = "shopping_list_id";

    public Menu menu;


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

        MenuItem addRecipe = menu.findItem(R.id.action_add_recipe);
        addRecipe.setVisible(true);

        MenuItem moveToShoppingCart = menu.findItem(R.id.action_move_all_to_shopping_cart);
        moveToShoppingCart.setVisible(true);

        MenuItem moveFromShoppingCart = menu.findItem(R.id.action_move_all_from_shopping_cart);
        moveFromShoppingCart.setVisible(true);

        MenuItem shoppingMode = menu.findItem(R.id.action_shopping_mode);
        shoppingMode.setVisible(true);

        if(SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.SHOPPING_MODE, false, getApplicationContext())){
            for(int i = 0; i <  menu.getItem(1).getSubMenu().size(); i++){
                menu.getItem(1).getSubMenu().getItem(i).setVisible(false);
            }
            moveToShoppingCart.setVisible(true);
            moveFromShoppingCart.setVisible(true);
        }


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        ItemSortType  type = null;
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
            case R.id.action_shopping_mode:
                // save enabled shopping mode setting and restart activity to update view
                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.SHOPPING_MODE, true, getApplicationContext());
                startActivity(ShoppingListActivity.getIntent(getApplicationContext(), getIntent().getExtras().getInt(SHOPPING_LIST_ID)));
                break;
            case R.id.share_option:
                
                break;
        }
        if(type != null) EventBus.getDefault().post(type);

        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Get Shopping List ID

        Intent intent = getIntent();
        String dataString = intent.getDataString();
        int id = -1;
        if(dataString != null) {
            //dataString is not null if Activity was opened by Calendar intent filter
            for (int i = 0; i < dataString.length() - this.getString(R.string.intent_id_separator).length(); i++) {
                if (dataString.substring(i, i + 5).equals(this.getString(R.string.intent_id_separator))) {
                    String idString = dataString.substring(i + 5);
                    id = Integer.parseInt(idString);
                    break;
                }
            }
            intent.putExtra(SHOPPING_LIST_ID, id);
        }else {

            Bundle extras = getIntent().getExtras();
            if(extras != null){
                id = extras.getInt(SHOPPING_LIST_ID);
            }

        }

        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(frameLayout.getId(), ShoppingListFragment.newInstance(id)).commit();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

       if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
           SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.SHOPPING_MODE, true, getApplicationContext());
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }




}
