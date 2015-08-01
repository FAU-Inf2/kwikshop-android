package de.fau.cs.mad.kwikshop.android.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.RepeatType;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.TimePeriodsEnum;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.model.LocalListStorage;
import de.fau.cs.mad.kwikshop.android.model.RegularlyRepeatHelper;
import de.fau.cs.mad.kwikshop.android.view.interfaces.SaveDeleteActivity;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;

public class ReminderFragment extends Fragment {

    private static final String ARG_ITEMID = "item_id";
    private static final String ARG_LISTID = "list_id";

    private View rootView;

    private int itemId;
    private Item item;

    private int listId;
    List<ShoppingList> shoppingLists;

    @Inject
    ListManager<ShoppingList> shoppingListManager;

    @Inject
    RegularlyRepeatHelper repeatHelper;

    /* UI elements */

    @InjectView(R.id.reminder_question_text)
    TextView question_text;

    @InjectView(R.id.reminder_doNothingThisTime_radioButton)
    RadioButton doNothing_radioButton;

    @InjectView(R.id.reminder_deleteReminder_radioButton_radioButton)
    RadioButton deleteReminder_radioButton;

    @InjectView(R.id.reminder_addToShoppingList_radioButton)
    RadioButton addToShoppingList_radioButton;

    @InjectView(R.id.reminder_later_radioButton)
    RadioButton later_radioButton;

    @InjectView(R.id.reminder_shoppingList_spinner)
    Spinner shoppingList_spinner;

    @InjectView(R.id.reminder_numberPicker)
    NumberPicker period_numberPicker;

    @InjectView(R.id.reminder_period_spinner)
    Spinner period_spinner;

    /**
     * Creates a new instance of ReminderFragment for the specified item
     *
     * @param itemID
     * @return
     */
    public static ReminderFragment newInstance(int listID, int itemID) {
        ReminderFragment fragment = new ReminderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LISTID, listID);
        args.putInt(ARG_ITEMID, itemID);
        fragment.setArguments(args);

        return fragment;
    }


    public ReminderFragment() {
        // Required empty public constructor
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // calling onCreateOptionsMenu
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            itemId = getArguments().getInt(ARG_ITEMID);
            listId = getArguments().getInt(ARG_LISTID);
        } else {
            throw new IllegalArgumentException("Missing arguments for creating ReminderFragment.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_reminder, container, false);
        ButterKnife.inject(this, rootView);

        setupUI();

        // set actionbar with save and cancel buttons
        setCustomActionBar();

        // set actionbar title
        getActivity().setTitle(R.string.title_fragment_reminder);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void setCustomActionBar() {

        if (getActivity() instanceof SaveDeleteActivity) {

            SaveDeleteActivity parent = (SaveDeleteActivity) getActivity();

            View saveButton = parent.getSaveButton();
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (doNothing_radioButton.isChecked()) {
                        skipThisReminder();
                    } else if (deleteReminder_radioButton.isChecked()) {
                        deleteReminder();
                    } else if (addToShoppingList_radioButton.isChecked()) {
                        addToShoppingList();
                    } else if (later_radioButton.isChecked()) {
                        remindLater();
                    }
                    //repeatHelper.checkIfReminderIsOver();
                    getActivity().finish();
                }
            });

            View delete = parent.getDeleteButton();

            delete.setVisibility(View.GONE);
            //delete.setOnClickListener(new View.OnClickListener() {
            //    @Override
            //    public void onClick(View v) {
            //        removeItemFromShoppingList();
            //        getActivity().finish();
            //    }
            //});


        }
    }

    private void skipThisReminder() {
        if (item.isRemindFromNextPurchaseOn()) {
            item.setLastBought(null);
            item.setRemindAtDate(null);
            repeatHelper.offerRepeatData(item);

            saveInDatabase();

            Toast.makeText(getActivity(), R.string.reminder_nextTimeBought_msg, Toast.LENGTH_LONG).show();
        } else {
            changeReminderDate();
        }
    }

    private void deleteReminder() {
        item.setRemindAtDate(null);
        item.setLastBought(null);
        item.setRepeatType(RepeatType.None);

        saveInDatabase();

        Toast.makeText(getActivity(), R.string.reminder_deleted_msg, Toast.LENGTH_LONG).show();
    }

    private void addToShoppingList() {
        Item newItem = new Item(item);
        ShoppingList selectedList = shoppingLists.get(shoppingList_spinner.getSelectedItemPosition());

        shoppingListManager.addListItem(selectedList.getId(), newItem);

        changeReminderDate(getString(R.string.reminder_itemAdded));
    }

    private void remindLater() {
        int repeatSpinnerPos = period_spinner.getSelectedItemPosition();
        switch (repeatSpinnerPos) {
            case 0:
                item.setPeriodType(TimePeriodsEnum.DAYS);
                break;
            case 1:
                item.setPeriodType(TimePeriodsEnum.WEEKS);
                break;
            case 2:
                item.setPeriodType(TimePeriodsEnum.MONTHS);
                break;
            default:
                break;
        }
        item.setSelectedRepeatTime(period_numberPicker.getValue());
        item.setRemindFromNowOn(true);
        changeReminderDate();
    }

    private void saveInDatabase() {
        shoppingListManager.saveList(listId);
    }

    private void changeReminderDate() {
        changeReminderDate("");
    }

    private void changeReminderDate(String additionalMessage) {
        Calendar remindDate = Calendar.getInstance();
        remindDate.setTime(item.getRemindAtDate());

        switch (item.getPeriodType()) {
            case DAYS:
                remindDate.add(Calendar.DAY_OF_MONTH, item.getSelectedRepeatTime());
                break;
            case WEEKS:
                remindDate.add(Calendar.DAY_OF_MONTH, item.getSelectedRepeatTime() * 7);
                break;
            case MONTHS:
                remindDate.add(Calendar.MONTH, item.getSelectedRepeatTime());
                break;
        }

        item.setRemindAtDate(remindDate.getTime());
        repeatHelper.offerRepeatData(item); // in order to re-sort the Priority Queue

        saveInDatabase();

        if (additionalMessage == null)
            additionalMessage = "";

        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.DEFAULT, SimpleDateFormat.DEFAULT, getResources().getConfiguration().locale);
        String message = additionalMessage + getString(R.string.reminder_set_msg) + " " + dateFormat.format(remindDate.getTime());
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    public void onDestroyView() {
        super.onDestroyView();
    }





    private void setupUI() {

        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(getActivity()));
        objectGraph.inject(this);

        item = repeatHelper.getItemForId(itemId);

        if (item == null) {
            // can happen if a reminder was set for the past
            getActivity().finish();
            return;
        }

        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.DEFAULT, SimpleDateFormat.DEFAULT, getResources().getConfiguration().locale);
        String date;
        if (item.getRemindAtDate() != null) {
            date = dateFormat.format(item.getRemindAtDate().getTime());
        } else {
            date = getString(R.string.now);
        }

        String question = String.format(getString(R.string.reminder_question), item.getName(), date);
        question_text.setText(question);

        period_numberPicker.setMinValue(1);
        period_numberPicker.setMaxValue(10);
        period_numberPicker.setWrapSelectorWheel(false);


        ArrayList<String> period_spinner_entries = new ArrayList<String>(3);
        period_spinner_entries.add(0, getString(R.string.days));
        period_spinner_entries.add(1, getString(R.string.weeks));
        period_spinner_entries.add(2, getString(R.string.months));
        ArrayAdapter<String> period_spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, period_spinner_entries);
        period_spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        period_spinner.setAdapter(period_spinnerAdapter);

        new ListStorageFragment().SetupLocalListStorageFragment(getActivity());
        LocalListStorage listStorage = new LocalListStorage();
        shoppingLists = listStorage.getAllLists();
        ArrayList<String> namesOfShoppingLists = new ArrayList<>(shoppingLists.size());
        for(ShoppingList shoppingList : shoppingLists) {
            namesOfShoppingLists.add(shoppingList.getName());
        }

        ArrayAdapter<String> shoppingList_spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, namesOfShoppingLists);
        shoppingList_spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shoppingList_spinner.setAdapter(shoppingList_spinnerAdapter);
    }

}
