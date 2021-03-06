package de.fau.cs.mad.kwikshop.android.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.util.ItemMerger;
import de.fau.cs.mad.kwikshop.common.util.StringHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;

public class DefaultViewLauncher implements ViewLauncher {

    private static final String MAILTO = "mailto";

    private ProgressDialog progress;
    private AlertDialog alert;
    private final Activity activity;
    private final ResourceProvider resourceProvider;


    @Inject
    public DefaultViewLauncher(Activity activity, ResourceProvider resourceProvider) {

        if (activity == null) {
            throw new ArgumentNullException("activity");
        }

        if(resourceProvider == null) {
            throw new ArgumentNullException("resourceProvider");
        }

        this.activity = activity;
        this.resourceProvider = resourceProvider;
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
        activity.startActivity(ShoppingListActivity.getIntent(activity, shoppingListId)
                .putExtra(ShoppingListFragment.DO_NOT_ASK_FOR_SUPERMARKET, true));
    }

    @Override
    public void returnToShoppingList(int shoppingListId) {
        activity.startActivity(ShoppingListActivity.getIntent(activity, shoppingListId)
                .putExtra(ShoppingListFragment.DO_NOT_ASK_FOR_SUPERMARKET, true).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    @Override
    public void showShoppingListInShoppingMode(int shoppingListId) {
        activity.startActivity(ShoppingListActivity.getIntent(activity, shoppingListId)
                .putExtra(ShoppingListActivity.SHOPPING_MODE, true)
                .putExtra(ShoppingListFragment.DO_NOT_ASK_FOR_SUPERMARKET, true));
    }

    @Override
    public void showShoppingListInEditMode(int shoppingListId) {
        activity.startActivity(ShoppingListActivity.getIntent(activity, shoppingListId)
                .putExtra(ShoppingListActivity.EDIT_MODE, true)
                .putExtra(ShoppingListFragment.DO_NOT_ASK_FOR_SUPERMARKET, true));
    }

    @Override
    public void showShoppingListWithSupermarketDialog(int shoppingListId) {
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
    public void showYesNoDialog(String title, String message, final Command<Void> positiveCommand, final Command<Void> negativeCommand) {

        showMessageDialog(title, message,
                resourceProvider.getString(R.string.yes), positiveCommand,
                null, null,
                resourceProvider.getString(R.string.no), negativeCommand);
    }

    @Override
    public void showTextInputDialog(String title, String value, final Command<String> positiveCommand,
                                    final Command<String> negativeCommand) {

        showTextInputDialog(title, value,
                resourceProvider.getString(android.R.string.ok), positiveCommand,
                null, null,
                resourceProvider.getString(android.R.string.cancel), negativeCommand);
    }

    @Override
    public void showTextInputDialog(String title, String description, String value, Command<String> positiveCommand, Command<String> negativeCommand) {
        showInputDialog(title, description, value, InputType.TYPE_CLASS_TEXT,
                resourceProvider.getString(android.R.string.ok), positiveCommand,
                null, null,
                resourceProvider.getString(android.R.string.cancel), negativeCommand);
    }


    @Override
    public void showTextInputDialog(String title, String value,
                                    String positiveText, final Command<String> positiveCommand,
                                    String neutralText,  final Command<String> neutralCommand,
                                    String negativeText, final Command<String> negativeCommand) {

        showInputDialog(title, null, value, InputType.TYPE_CLASS_TEXT,
                positiveText, positiveCommand,
                neutralText, neutralCommand,
                negativeText, negativeCommand);
    }

    @Override
    public void showNumberInputDialog(String title, String message, int value,
                                      String positiveText, Command<String> positiveCommand,
                                      String neutralText, Command<String> neutralCommand,
                                      String negativeText, Command<String> negativeCommand) {

        showInputDialog(title, message, Integer.toString(value), InputType.TYPE_CLASS_NUMBER,
                        positiveText, positiveCommand,
                        neutralText, neutralCommand,
                        negativeText, negativeCommand);
    }

    @Override
    public void showMessageDialog(String title, String message,
                                  String positiveMessage, final Command<Void> positiveCommand,
                                  String neutralMessage, final Command<Void> neutralCommand,
                                  String negativeMessage, final Command<Void> negativeCommand) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);

        //android.R.string.yes
        builder.setPositiveButton(positiveMessage, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {

                if (positiveCommand.getCanExecute()) {
                    positiveCommand.execute(null);
                }
            }
        });

        if(neutralCommand != null) {
            builder.setNeutralButton(neutralMessage, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(neutralCommand.getCanExecute()) {
                        neutralCommand.execute(null);
                    }
                }
            });
        }

        //android.R.string.no
        builder.setNegativeButton(negativeMessage, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                if (negativeCommand.getCanExecute()) {
                    negativeCommand.execute(null);
                }
            }
        });

        alert = builder.create();
        if(!activity.isFinishing()) {
            alert.show();
        }
    }

    @Override
    public void showMessageDialog(String title, String message, String positiveMessage, final Command<Void> positiveCommand, String negativeMessage, final Command<Void> negativeCommand) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton(positiveMessage, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {

                if (positiveCommand.getCanExecute()) {
                    positiveCommand.execute(null);
                }
            }
        });

        builder.setNegativeButton(negativeMessage, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                if (negativeCommand.getCanExecute()) {
                    negativeCommand.execute(null);
                }
            }
        });

        alert = builder.create();
        if(!activity.isFinishing()) {
            alert.show();
        }
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
    public void showAddRecipeDialog(final ListManager<ShoppingList> listManager, final ListManager<Recipe> recipeManager, final int listId, final boolean fromShoppingList){

        //fromShoppingList indicates whether this method was called from a shopping list to add a recipe (true) or from a recipe to add to a shopping list (false)

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        if(fromShoppingList)
            builder.setTitle(activity.getString(R.string.recipe_add_recipe));
        else
            builder.setTitle(activity.getString(R.string.recipe_add_to_shoppinglist));


        final View view = activity.getLayoutInflater().inflate(R.layout.dialog_add_recipe_to_shoppinglist, null);
        final TextView listTextView = (TextView) view.findViewById(R.id.dialog_add_recipe_textview1);
        final Spinner spinner = (Spinner) view.findViewById(R.id.dialog_add_recipe_spinner);
        final TextView amountTextView = (TextView) view.findViewById(R.id.dialog_add_recipe_textview2);
        final NumberPicker numberPicker = (NumberPicker) view.findViewById(R.id.dialog_add_recipe_numberpicker);


        // style number picker
        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    pf.set(numberPicker, resourceProvider.getDrawable(R.drawable.np_numberpicker_selection_divider_green));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }




        if(fromShoppingList)
            listTextView.setText("\n" + activity.getString(R.string.recipe_choose_recipe));
        else
            listTextView.setText("\n" + activity.getString(R.string.recipe_choose_shoppinglist));


        List<String> names = new ArrayList<>();
        if(fromShoppingList) {
            for (Recipe recipe : recipeManager.getLists()) {
                names.add(recipe.getName());
            }
        }
        else{
            for (ShoppingList shoppingList : listManager.getLists()) {
                names.add(shoppingList.getName());
            }
        }
        final ArrayAdapter<String> arrayAdapter = new  ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, names);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (fromShoppingList) {
                    Recipe curRecipe = (Recipe) (recipeManager.getLists().toArray()[position]);
                    amountTextView.setText("\n" + activity.getString(R.string.recipe_choose_amount) + " " + curRecipe.getScaleName() + ":");
                    numberPicker.setValue(curRecipe.getScaleFactor());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if(!fromShoppingList)amountTextView.setText("\n" + activity.getString(R.string.recipe_choose_amount) + " " + recipeManager.getList(listId).getScaleName() + ":");
        amountTextView.setTextColor(activity.getResources().getColor(R.color.primary_text));

        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(50);
        if(!fromShoppingList) numberPicker.setValue(recipeManager.getList(listId).getScaleFactor());

        builder.setView(view);

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {

                Recipe selectedRecipe;
                if(fromShoppingList)
                    selectedRecipe = (Recipe) recipeManager.getLists().toArray()[spinner.getSelectedItemPosition()];
                else
                    selectedRecipe = recipeManager.getList(listId);

                double scaledValue = (double) numberPicker.getValue() / selectedRecipe.getScaleFactor();

                int shoppinglistId = -1;

                ItemMerger itemMerger = new ItemMerger(listManager);
                for(Item item : selectedRecipe.getItems()){
                    Item newItem = new Item();
                    newItem.setName(item.getName());
                    newItem.setAmount(scaledValue * item.getAmount());
                    newItem.setBrand(item.getBrand());
                    newItem.setComment(item.getComment());
                    newItem.setHighlight(item.isHighlight());
                    newItem.setGroup(item.getGroup());
                    newItem.setUnit(item.getUnit());

                    if(fromShoppingList) {
                        if (!itemMerger.mergeItem(listId, newItem)) {
                            listManager.addListItem(listId, newItem);
                        }
                    }else{
                        ShoppingList selectedList = (ShoppingList) listManager.getLists().toArray()[spinner.getSelectedItemPosition()];
                        shoppinglistId = selectedList.getId();
                        if (!itemMerger.mergeItem(shoppinglistId, newItem)) {
                            listManager.addListItem(shoppinglistId, newItem);
                        }
                    }
                }

                if(!fromShoppingList){
                    showShoppingListWithSupermarketDialog(shoppinglistId);
                }

            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
            }
        });

        alert = builder.create();
        alert.show();

    }

    //TODO: move somewhere else, this is not a responsibility of ViewLauncher
    @Deprecated
    @Override
    public void notifyUnitSpinnerChange(ArrayAdapter<String> adapter){
        List<Unit> units;
        adapter.clear();
        //populate unit picker with units from database
        DisplayHelper displayHelper = new DisplayHelper(new DefaultResourceProvider(activity));


        //get units from the database and sort them by name
        units = ListStorageFragment.getUnitStorage().getItems();
        Collections.sort(units, new Comparator<Unit>() {
            @Override
            public int compare(Unit lhs, Unit rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });

        //TODO implement adapter for Unit instead of String

        ArrayList<String> unitNames = new ArrayList<>();
        for (Unit u : units) {
            unitNames.add(displayHelper.getDisplayName(u));
        }
        adapter.addAll(unitNames);
        adapter.notifyDataSetChanged();
        //onQuickAddTextChanged();
    }

    //TODO: move somewhere else, this is not a responsibility of ViewLauncher
    @Deprecated
    @Override
    public void notifyGroupSpinnerChange(ArrayAdapter<String> adapter){
        List<Group> groups;
        adapter.clear();
        //populate group picker with groups from database
        DisplayHelper displayHelper = new DisplayHelper(new DefaultResourceProvider(activity));



        groups = ListStorageFragment.getGroupStorage().getItems();

        //TODO implement adapter for Unit instead of String

        ArrayList<String> groupNames = new ArrayList<>();
        for (Group g : groups) {
            groupNames.add(displayHelper.getDisplayName(g));
        }
        adapter.addAll(groupNames);
        adapter.notifyDataSetChanged();
        //onQuickAddTextChanged();
    }

    @Override
    public void launchEmailChooser(String chooserTitle, String recipient, String subject, String body) {

        Intent sendIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(MAILTO, recipient, null));

        sendIntent.putExtra(Intent.EXTRA_TEXT, body);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);

        activity.startActivity(Intent.createChooser(sendIntent, chooserTitle));
    }


    @Override
    public void showMessageDialogWithCheckbox(String title, String message,
                                  String positiveMessage, final Command<Void> positiveCommand,
                                  String neutralMessage, final Command<Void> neutralCommand,
                                  String negativeMessage, final Command<Void> negativeCommand,
                                  String checkBoxMessage, boolean checkBoxDefaultValue,
                                  final Command <Void> checkedCommand, final Command<Void> uncheckedCommand) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);

        final View view = activity.getLayoutInflater().inflate(R.layout.dialog_checkbox, null);
        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox_dialog_checkbox);

        builder.setMessage(message);

        checkBox.setText(checkBoxMessage);
        checkBox.setChecked(checkBoxDefaultValue);

        builder.setView(view);


        //android.R.string.yes
        builder.setPositiveButton(positiveMessage, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {

                if (positiveCommand != null && positiveCommand.getCanExecute()) {
                    if(checkBox.isChecked() && checkedCommand != null && checkedCommand.getCanExecute())
                        checkedCommand.execute(null);
                    else if (!checkBox.isChecked() && uncheckedCommand != null && uncheckedCommand.getCanExecute())
                        uncheckedCommand.execute(null);
                    positiveCommand.execute(null);
                }
            }
        });

        if(neutralCommand != null) {
            builder.setNeutralButton(neutralMessage, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(neutralCommand.getCanExecute()) {
                        if(checkBox.isChecked() && checkedCommand != null && checkedCommand.getCanExecute())
                            checkedCommand.execute(null);
                        else if (!checkBox.isChecked() && uncheckedCommand != null && uncheckedCommand.getCanExecute())
                            uncheckedCommand.execute(null);

                        neutralCommand.execute(null);
                    }
                }
            });
        }

        //android.R.string.no
        builder.setNegativeButton(negativeMessage, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                if (negativeCommand != null && negativeCommand.getCanExecute()) {
                    if (checkBox.isChecked() && checkedCommand != null && checkedCommand.getCanExecute())
                        checkedCommand.execute(null);
                    else if (!checkBox.isChecked() && uncheckedCommand != null && uncheckedCommand.getCanExecute())
                        uncheckedCommand.execute(null);

                    negativeCommand.execute(null);
                }
            }
        });

        alert = builder.create();
        if(!activity.isFinishing()) {
            alert.show();
        }
    }

    @Override
    public void showProgressDialog(String message, String negativeMessage, boolean cancelable, final Command<Void> negativeCommand) {

        progress = new ProgressDialog(activity);
        progress.setMessage(message);
        progress.setCancelable(true);
        progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (negativeCommand != null && negativeCommand.getCanExecute()) {
                    negativeCommand.execute(null);
                }

            }
        });
        progress.setButton(DialogInterface.BUTTON_NEGATIVE, negativeMessage, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (negativeCommand != null && negativeCommand.getCanExecute()) {
                    negativeCommand.execute(null);
                }
            }
        });

        if(!activity.isFinishing()) {
            progress.show();
        }
    }

    @Override
    public void showProgressDialogWithoutButton(String message, final Command<Void> command) {

        progress = new ProgressDialog(activity);
        progress.setCanceledOnTouchOutside(true);
        progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (command != null && command.getCanExecute()) {
                    command.execute(null);
                }
            }}
        );
        progress.setMessage(message);
        if(!activity.isFinishing()) {
                progress.show();
        }

    }

    @Override
    public void showProgressDialogWithListID(String message, String negativeMessage, final int listId, boolean cancelable, final Command<Integer> negativeCommand) {

        progress = new ProgressDialog(activity);
        progress.setMessage(message);
        progress.setCancelable(true);
        progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (negativeCommand != null && negativeCommand.getCanExecute()) {
                    negativeCommand.execute(listId);
                }

            }
        });
        progress.setButton(DialogInterface.BUTTON_NEGATIVE, negativeMessage, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (negativeCommand != null && negativeCommand.getCanExecute()) {
                    negativeCommand.execute(listId);
                }
            }
        });

        if(!activity.isFinishing()) {
            progress.show();
        }

    }

    @Override
    public void restartActivity() {
        Intent intent = activity.getIntent();
        activity.finish();
        startActivity(intent);
    }


    private class IntClosure {
        public int value = -1;
    }

    @Override
    public void showMessageDialogWithRadioButtons(String title, CharSequence[] items,
                                                  String positiveMessage, final Command<Integer> positiveCommand,
                                                  String neutralMessage, final Command<Integer> neutralCommand,
                                                  String negativeMessage, final Command<Integer> negativeCommand) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);


        final IntClosure closure = new IntClosure();
        closure.value = 0;

        builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                closure.value = which;
            }
        });

        builder.setPositiveButton(positiveMessage, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {

                dialog.dismiss();

                if (positiveCommand.getCanExecute()) {
                    positiveCommand.execute(closure.value);
                }
            }
        });

        builder.setNeutralButton(neutralMessage, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {

                dialog.dismiss();

                if (neutralCommand.getCanExecute()) {
                    neutralCommand.execute(closure.value);
                }
            }
        });

        builder.setNegativeButton(negativeMessage, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {

                dialog.dismiss();

                if (negativeCommand.getCanExecute()) {
                    negativeCommand.execute(closure.value);
                }
            }
        });

        alert = builder.create();

        if(!activity.isFinishing()) {
            alert.show();
        }

    }



    @Override
    public void showListOfShoppingListsActivity() {
        activity.finish();
        Intent intent = ListOfShoppingListsActivity.getIntent(activity.getApplicationContext());
        activity.startActivity(intent);
    }

    @Override
    public void showMultiplyChoiceDialog(String title, final String[] typeNames, boolean[] checkedTypes,
                                         final Command<Integer> selectCommand,
                                         final Command<Integer> deSelectCommand,
                                         String positiveMessage, final Command<Void> positiveCommand,
                                         String negativeMessage, final Command<Void> negativeCommand) {


        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);

        builder.setMultiChoiceItems(typeNames, checkedTypes, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                       if(isChecked){
                           if(selectCommand.getCanExecute()){
                               selectCommand.execute(which);
                           }
                       } else {
                           if(deSelectCommand.getCanExecute()){
                               deSelectCommand.execute(which);
                           }
                       }
                    }
                }
        );
        builder.setPositiveButton(positiveMessage, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(positiveCommand.getCanExecute()) {
                    positiveCommand.execute(null);
                }

            }
        });

        builder.setNegativeButton(negativeMessage, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(negativeCommand.getCanExecute()){
                    negativeCommand.execute(null);
                }
            }
        });

        alert = builder.create();

        if(!activity.isFinishing()) {
            alert.show();
        }
    }


    @Override
    public void showLocationActivity() {
        activity.finish();
        Intent intent =  LocationActivity.getIntent(activity.getApplicationContext());
        activity.startActivity(intent);
    }

    @Override
    public void finishActivity() {
        activity.finish();
    }

    @Override
    public void dismissDialog() {

        try {
            if (alert != null && alert.isShowing()) {
                alert.dismiss();
            }

            if (progress != null && progress.isShowing()) {
                progress.dismiss();
            }

        } catch (final IllegalArgumentException e) {
            // Handle or log or ignore
        } finally {
            progress = null;
            alert = null;
        }

    }


    @Override
    public void startActivity(Intent intent) {
        activity.startActivity(intent);
    }



    private void showInputDialog(String title, String message, String value, int inputType,
                                 String positiveText, final Command<String> positiveCommand,
                                 String neutralText,  final Command<String> neutralCommand,
                                 String negativeText, final Command<String> negativeCommand) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);

        if(!StringHelper.isNullOrWhiteSpace(message)) {
            builder.setMessage(message);
        }

        if(value == null) {
            value = "";
        }

        // Set up the input
        final View view = activity.getLayoutInflater().inflate(R.layout.dialog_textinput, null);
        final TextView input = (TextView) view.findViewById(R.id.textView);
        input.setInputType(inputType);
        input.setSingleLine();

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setText(value);
        builder.setView(view);

        // Set up the buttons
        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(positiveCommand.getCanExecute()) {
                    positiveCommand.execute(input.getText().toString());
                }

            }
        });

        if(negativeCommand != null && !StringHelper.isNullOrWhiteSpace(neutralText)) {

            builder.setNeutralButton(neutralText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if(neutralCommand.getCanExecute()) {
                        neutralCommand.execute(input.getText().toString());
                    }

                }
            });
        }

        builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

                if(negativeCommand.getCanExecute()) {
                    negativeCommand.execute(input.getText().toString());
                }
            }
        });

        builder.show();

    }



}