package de.cs.fau.mad.quickshop.android.view;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;

import de.cs.fau.mad.quickshop.android.DatePickerFragment;
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


    public void showDatePicker(int year, int month, int day, int hour, int minute) {

        DialogFragment newFragment = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putInt("year", year);
        args.putInt("month", month);
        args.putInt("day", day);
        args.putInt("hour", hour);
        args.putInt("minute", minute);
        newFragment.setArguments(args);
        newFragment.show(activity.getFragmentManager(), "datePicker");

    }
}
