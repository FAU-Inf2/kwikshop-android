package de.cs.fau.mad.quickshop.android.view;

import android.app.Activity;
import android.content.Intent;

import de.cs.fau.mad.quickshop.android.ShoppingListActivity;
import de.cs.fau.mad.quickshop.android.ShoppingListDetailActivity;
import de.cs.fau.mad.quickshop.android.viewmodel.common.ViewLauncher;

public class DefaultViewLauncher implements ViewLauncher {

    private final Activity activity;


    public DefaultViewLauncher(Activity activity) {

        if (activity == null) {
            throw new IllegalArgumentException("'activity' must not be null");
        }

        this.activity = activity;
    }


    @Override
    public void showAddShoppingListView() {
        activity.startActivity(ShoppingListDetailActivity.getIntent(activity));
    }

    @Override
    public void showShoppingListDetailsView(int shoppingListId) {

        activity.startActivity(ShoppingListDetailActivity.getIntent(activity, shoppingListId));

    }

    @Override
    public void showShoppingList(int shoppingListId) {
        activity.startActivity(ShoppingListActivity.getIntent(activity, shoppingListId));
    }

}
