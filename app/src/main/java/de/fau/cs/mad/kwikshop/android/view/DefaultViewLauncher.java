package de.fau.cs.mad.kwikshop.android.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.InternetHelper;
import de.fau.cs.mad.kwikshop.android.model.SpeechRecognitionHelper;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.messages.ActivityResultEvent;
import de.fau.cs.mad.kwikshop.android.util.ItemMerger;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.util.StringHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.android.model.messages.DialogFinishedEvent;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.greenrobot.event.EventBus;
import se.walkercrou.places.Place;

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
                resourceProvider.getString(android.R.string.yes), positiveCommand,
                null, null,
                resourceProvider.getString(android.R.string.no), negativeCommand);
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
        alert.show();
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
    public void showAddRecipeDialog(final ListManager<ShoppingList> listManager, final ListManager<Recipe> recipeManager, final int listId, final boolean fromShoppingList){

        //fromShoppingList indicates whether this method was called from a shopping list to add a recipe (true) or from a recipe to add to a shopping list (false)

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        if(fromShoppingList)
            builder.setTitle(activity.getString(R.string.recipe_add_recipe));
        else
            builder.setTitle(activity.getString(R.string.recipe_add_to_shoppinglist));

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);


        final TextView listTextView = new TextView(activity);
        final NumberPicker numberPicker = new NumberPicker(activity);
        final Spinner spinner = new Spinner(activity);
        final TextView amountTextView = new TextView(activity);

        if(fromShoppingList)
            listTextView.setText("\n" + activity.getString(R.string.recipe_choose_recipe) + "\n");
        else
            listTextView.setText("\n" + activity.getString(R.string.recipe_choose_shoppinglist) + "\n");

        listTextView.setTextColor(activity.getResources().getColor(R.color.primary_text));
        layout.addView(listTextView);


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
        layout.addView(spinner);

        if(!fromShoppingList)amountTextView.setText("\n" + activity.getString(R.string.recipe_choose_amount) + " " + recipeManager.getList(listId).getScaleName() + ":");
        amountTextView.setTextColor(activity.getResources().getColor(R.color.primary_text));
        layout.addView(amountTextView);

        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(50);
        if(!fromShoppingList) numberPicker.setValue(recipeManager.getList(listId).getScaleFactor());
        layout.addView(numberPicker);

        builder.setView(layout);

        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {

                Recipe selectedRecipe;
                if(fromShoppingList)
                    selectedRecipe = (Recipe) recipeManager.getLists().toArray()[spinner.getSelectedItemPosition()];
                else
                    selectedRecipe = recipeManager.getList(listId);

                double scaledValue = (double) numberPicker.getValue() / selectedRecipe.getScaleFactor();

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
                        if (!itemMerger.mergeItem(selectedList.getId(), newItem)) {
                            listManager.addListItem(selectedList.getId(), newItem);
                        }
                    }
                }

            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
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
        builder.setMessage(message);

        final CheckBox checkBox = new CheckBox(activity);
        checkBox.setText(checkBoxMessage);
        checkBox.setChecked(checkBoxDefaultValue);
        builder.setView(checkBox);


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
        alert.show();
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

        progress.show();
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

        progress.show();
    }


    @Override
    public void showMessageDialogWithRadioButtons(String title, CharSequence[] items,
                                                  String positiveMessage, final Command<Void> positiveCommand,
                                                  String neutralMessage, final Command<Void> neutralCommand,
                                                  String negativeMessage, final Command<Void> negativeCommand, final Command<Integer> selectCommand) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);

        builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (selectCommand.getCanExecute()) {
                    selectCommand.execute(which);
                }

            }
        });

        builder.setPositiveButton(positiveMessage, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                if (positiveCommand.getCanExecute()) {
                    positiveCommand.execute(null);
                }
            }
        });

        builder.setNeutralButton(neutralMessage, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                if (neutralCommand.getCanExecute()) {
                    neutralCommand.execute(null);
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
        alert.show();
    }

    @Override
    public void dismissProgressDialog() {
        if (progress != null) {
            progress.dismiss();
            progress = null;
        }
    }

    @Override
    public void showListOfShoppingListsActivity() {
        activity.finish();
        Intent intent = ListOfShoppingListsActivity.getIntent(activity.getApplicationContext());
        activity.startActivity(intent);
    }

    @Override
    public void showLocationActivity() {
        activity.finish();
        Intent intent =  LocationActivity.getIntent(activity.getApplicationContext());
        activity.startActivity(intent);
    }

    @Override
    public boolean checkInternetConnection() {
        return InternetHelper.checkInternetConnection(activity);
    }

    @Override
    public void finishActivity() {
        activity.finish();
    }

    @Override
    public void dismissDialog() {
        if (alert != null) { alert.dismiss(); }
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