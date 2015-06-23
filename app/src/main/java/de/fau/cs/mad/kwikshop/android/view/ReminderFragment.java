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

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.model.LocalListStorage;
import de.fau.cs.mad.kwikshop.android.view.interfaces.SaveDeleteActivity;

public class ReminderFragment extends Fragment {

    private static final String ARG_ITEMID = "item_id";
    private static final String ARG_LISTID = "list_id";

    private View rootView;

    private int itemId;
    private Item item;

    private int listId;

    /*private int listId;

    private boolean isNewItem;

    private ShoppingList shoppingList;
    private List<Unit> units;
    private int selectedUnitIndex = -1;
    private List<Group> groups;
    private int selectedGroupIndex = -1;

    private String[] numbersForAmountPicker;
    private int amount_numberPickerCalledWith;

    private static AutoCompletionHelper autoCompletion;*/

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

        ShoppingList shoppingList = ListStorageFragment.getLocalListStorage().loadList(listId);
        item = shoppingList.getItem(itemId);

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
                    //if (productname_text.getText().length() > 0) {
                    //    saveItem();
                        getActivity().finish();
                    //} else {
                    //    Toast.makeText(getActivity(), getResources().getString(R.string.error_empty_productname), Toast.LENGTH_LONG).show();
                    //}
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

    public void onDestroyView() {
        super.onDestroyView();
    }





    private void setupUI() {

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
        List<ShoppingList> shoppingLists = listStorage.getAllLists();
        ArrayList<String> namesOfShoppingLists = new ArrayList<>(shoppingLists.size());
        for(ShoppingList shoppingList : shoppingLists) {
            namesOfShoppingLists.add(shoppingList.getName());
        }

        ArrayAdapter<String> shoppingList_spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, namesOfShoppingLists);
        shoppingList_spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shoppingList_spinner.setAdapter(shoppingList_spinnerAdapter);

    }

}
