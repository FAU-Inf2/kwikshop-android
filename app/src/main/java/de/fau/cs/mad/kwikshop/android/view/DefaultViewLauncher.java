package de.fau.cs.mad.kwikshop.android.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.common.Recipe;
import de.fau.cs.mad.kwikshop.android.model.messages.DialogFinishedEvent;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.greenrobot.event.EventBus;

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

    @Override
    public void showAddRecipeView(){
        activity.startActivity(RecipeDetailActivity.getIntent(activity));
    }

    @Override
    public void showRecipeDetailsView(int recipeId) {
        activity.startActivity(RecipeDetailActivity.getIntent(activity, recipeId));
     }

    @Override
    public void showRecipe(int recipeId) {
        activity.startActivity(RecipeActivity.getIntent(activity, recipeId));
    }

    @Override
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

    @Override
    public void showYesNoDialog(String title, String message, final Command positiveCommand, final Command negativeCommand) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {

                if (positiveCommand.getCanExecute()) {
                    positiveCommand.execute(null);
                }
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                if (negativeCommand.getCanExecute()) {
                    negativeCommand.execute(null);
                }
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void showToast(String message, int duration) {
        Toast.makeText(activity, message, duration).show();
    }

    @Override
    public void showToast(int resId, int duration) {
        Toast.makeText(activity, resId, duration).show();
    }

    @Override
    public void showItemDetailsView(int shoppingListId) {
        Intent intent = ItemDetailsActivity.getIntent(activity, shoppingListId);
        activity.startActivity(intent);
    }

    @Override
    public void showItemDetailsView(int shoppingListId, int itemId) {
        Intent intent = ItemDetailsActivity.getIntent(activity, shoppingListId, itemId);
        activity.startActivity(intent);
    }

    @Override
    public void RecipeShowItemDetailsView(int recipeId){
        Intent intent = RecipeItemDetailsActivity.getIntent(activity, recipeId);
        activity.startActivity(intent);
    }

    @Override
    public void RecipeShowItemDetailsView(int recipeId, int itemId){
        Intent intent = RecipeItemDetailsActivity.getIntent(activity, recipeId, itemId);
        activity.startActivity(intent);
    }

    @Override
    public void showReminderView(int listId, int itemId) {
        Intent intent = ReminderActivity.getIntent(activity, listId, itemId);
        activity.startActivity(intent);
    }

    @Override
    public void showAddRecipeDialog(final Recipe recipe){

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(recipe.getName());
        builder.setMessage(activity.getString(R.string.recipe_choose_amount) + " " + recipe.getScaleName() + ":");

        final NumberPicker numberPicker = new NumberPicker(activity);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(50);
        numberPicker.setValue(recipe.getScaleFactor());
        builder.setView(numberPicker);

        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {

                double scaledValue = (double) numberPicker.getValue() / recipe.getScaleFactor();
                EventBus.getDefault().post(new DialogFinishedEvent(scaledValue, recipe));

            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

    }
}