package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.common.Group;
import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.common.TimePeriodsEnum;
import de.fau.cs.mad.kwikshop.android.common.Unit;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.messages.AutoCompletionHistoryDeletedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangeType;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.model.messages.ShoppingListChangeType;
import de.fau.cs.mad.kwikshop.android.model.messages.ShoppingListChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.mock.SpaceTokenizer;
import de.fau.cs.mad.kwikshop.android.view.interfaces.SaveDeleteActivity;
import de.greenrobot.event.EventBus;

public class ItemDetailsFragment extends Fragment {

    private static final String ARG_LISTID = "list_id";
    private static final String ARG_ITEMID = "item_id";

    private View rootView;


    private int listId;
    private int itemId;
    private boolean isNewItem;

    private ShoppingList shoppingList;
    private Item item;
    private List<Unit> units;
    private int selectedUnitIndex = -1;
    private List<Group> groups;
    private int selectedGroupIndex = -1;

    private String[] numbersForAmountPicker;
    private int amount_numberPickerCalledWith;

    private static AutoCompletionHelper autoCompletion;

    /* UI elements */

    @InjectView(R.id.productname_text)
    MultiAutoCompleteTextView productname_text;

    @InjectView(R.id.numberPicker)
    NumberPicker amount_numberPicker;

    @InjectView(R.id.unit_spinner)
    Spinner unit_spinner;

    @InjectView(R.id.brand_text)
    AutoCompleteTextView brand_text;

    @InjectView(R.id.comment_text)
    EditText comment_text;

    @InjectView(R.id.group_spinner)
    Spinner group_spinner;

    @InjectView(R.id.highlight_checkBox)
    CheckBox highlight_checkbox;

    @InjectView(R.id.repeat_checkBox)
    CheckBox repeat_checkbox;

    @InjectView(R.id.repeat_spinner)
    Spinner repeat_spinner;

    @InjectView(R.id.repeat_numberPicker)
    NumberPicker repeat_numberPicker;

    /**
     * Creates a new instance of ItemDetailsFragment for a new shopping list item in the specified list
     *
     * @param listID
     * @return
     */
    public static ItemDetailsFragment newInstance(int listID) {
        return newInstance(listID, -1);
    }

    /**
     * Creates a new instance of ItemDetailsFragment for the specified shopping list item
     *
     * @param listID
     * @param itemID
     * @return
     */
    public static ItemDetailsFragment newInstance(int listID, int itemID) {
        ItemDetailsFragment fragment = new ItemDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LISTID, listID);
        args.putInt(ARG_ITEMID, itemID);
        fragment.setArguments(args);

        return fragment;
    }


    public ItemDetailsFragment() {
        // Required empty public constructor
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // calling onCreateOptionsMenu
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            listId = getArguments().getInt(ARG_LISTID);
            itemId = getArguments().getInt(ARG_ITEMID);
        }
        isNewItem = itemId == -1;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_item_details, container, false);
        ButterKnife.inject(this, rootView);

        // initialize autoCompletion if necessary
        if (autoCompletion == null)
            autoCompletion = AutoCompletionHelper.getAutoCompletionHelper(getActivity().getBaseContext());

        setupUI();

        // set actionbar with save and cancel buttons
        setCustomActionBar();

        EventBus.getDefault().register(this);

        // set actionbar title
        if (isNewItem) {
            getActivity().setTitle(R.string.title_fragment_item_details);
        } else {
            getActivity().setTitle(productname_text.getText().toString());
        }


        return rootView;
    }

    @Override
    public void onResume() {

        super.onResume();

        productname_text.requestFocus();

        if (isNewItem) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(productname_text, InputMethodManager.SHOW_IMPLICIT);
        } else {
            Window window = getActivity().getWindow();
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

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
                    if (productname_text.getText().length() > 0) {
                        saveItem();
                        getActivity().finish();
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.error_empty_productname), Toast.LENGTH_LONG).show();
                    }
                }
            });

            View delete = parent.getDeleteButton();

            if (isNewItem) {
                delete.setVisibility(View.GONE);
            } else {
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeItemFromShoppingList();
                        getActivity().finish();
                    }
                });
            }

        }


    }

    private void removeItemFromShoppingList() {

        if(!isNewItem){

            shoppingList.removeItem(itemId);

            EventBus.getDefault().post(new ShoppingListChangedEvent(ShoppingListChangeType.ItemsRemoved, listId));
            EventBus.getDefault().post(new ItemChangedEvent(ItemChangeType.Deleted, listId, itemId));
        }

        hideKeyboard();
    }


    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);

        Window window = getActivity().getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }


    public void onEventMainThread(ItemChangedEvent event) {
        if (shoppingList.getId() == event.getShoppingListId() && event.getItemId() == item.getId()) {
            setupUI();
        }
    }

    public void onEvent(AutoCompletionHistoryDeletedEvent event){
        if (autoCompletion != null) {
            productname_text.setAdapter(autoCompletion.getNameAdapter(getActivity()));
            brand_text.setAdapter(autoCompletion.getBrandAdapter(getActivity()));
        }
    }


    private void saveItem() {


        if (isNewItem) {
            item = new Item();
        }

        item.setName(productname_text.getText().toString());
        if(amount_numberPickerCalledWith != amount_numberPicker.getValue()){
            //only set amount if it got changed, so values written by parser which are not listed
            //in the amount_numberPicker don't get overwritten
            item.setAmount(Integer.parseInt(numbersForAmountPicker[amount_numberPicker.getValue()-1]));
        }
        item.setBrand(brand_text.getText().toString());

        if (selectedUnitIndex >= 0) {
            Unit u = units.get(selectedUnitIndex);
            item.setUnit(u);
        } else {
            item.setUnit(ListStorageFragment.getUnitStorage().getDefaultValue());
        }

        if (selectedGroupIndex >= 0) {
            Group g = groups.get(selectedGroupIndex);
            item.setGroup(g);
        } else {
            item.setGroup(ListStorageFragment.getGroupStorage().getDefaultValue());
        }

        item.setBrand(brand_text.getText().toString());
        item.setComment(comment_text.getText().toString());
        item.setHighlight(highlight_checkbox.isChecked());


        if (repeat_checkbox.isChecked()){
            boolean newRegularRepeat = !item.isRegularyRepeatItem(); // is repeat_checkbox checked for the first time?
            item.setRegularyRepeatItem(true);
            int repeatSpinnerPos = repeat_spinner.getSelectedItemPosition();
            switch (repeatSpinnerPos){
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
            item.setSelectedRepeatTime(repeat_numberPicker.getValue());
            if (newRegularRepeat) {
                // TODO add the item somewhere where it can be found quickly on app start
            }
        } else {
            item.setRegularyRepeatItem(false);
        }


        autoCompletion.offerNameAndGroup(productname_text.getText().toString(), item.getGroup());
        autoCompletion.offerBrand(brand_text.getText().toString());

        if (isNewItem) {
            shoppingList.addItem(item);
        }


        AsyncTask task = new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

                ListStorageFragment.getLocalListStorage().saveList(shoppingList);

                ItemChangeType itemChangeType = isNewItem
                        ? ItemChangeType.Added
                        : ItemChangeType.PropertiesModified;
                EventBus.getDefault().post(new ItemChangedEvent(itemChangeType, shoppingList.getId(), item.getId()));

                if (isNewItem) {
                    EventBus.getDefault().post(new ShoppingListChangedEvent(ShoppingListChangeType.ItemsAdded, shoppingList.getId()));
                }
                return null;
            }
        };
        task.execute();


        Toast.makeText(getActivity(), getResources().getString(R.string.itemdetails_saved), Toast.LENGTH_LONG).show();

        hideKeyboard();
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }


    private void setupUI() {

        //populate number picker
       numbersForAmountPicker = new String[1000];
        String [] numsOnce = new String[]{
          "1","2","3","4","5","6","7","8","9","10","11", "12","15", "20","25","30", "40", "50", "60",
                "70", "75", "80", "90", "100", "125", "150", "175", "200", "250", "300", "350", "400",
                "450", "500", "600", "700", "750", "800", "900", "1000"
        };
        int [] intNumsOnce = new int[numsOnce.length];
        for(int i = 0; i < intNumsOnce.length; i++){
            intNumsOnce[i] = Integer.parseInt(numsOnce[i]);
        }

        //setDisplayedValues length must be as long as range
        for(int i = 0; i < numbersForAmountPicker.length; i++){
            numbersForAmountPicker[i] = numsOnce[i%numsOnce.length];
        }
        amount_numberPicker.setMinValue(1);
        amount_numberPicker.setMaxValue(1000);
        amount_numberPicker.setWrapSelectorWheel(false);
        amount_numberPicker.setDisplayedValues(numbersForAmountPicker);

        repeat_numberPicker.setMinValue(1);
        repeat_numberPicker.setMaxValue(10);
        repeat_numberPicker.setWrapSelectorWheel(false);

        //wire up auto-complete for product name and brand
        productname_text.setAdapter(autoCompletion.getNameAdapter(getActivity()));
        productname_text.setTokenizer(new SpaceTokenizer());
        brand_text.setAdapter(autoCompletion.getBrandAdapter(getActivity()));

        // load shopping list and item and set values in UI
        shoppingList = ListStorageFragment.getLocalListStorage().loadList(listId);
        if (isNewItem) {

            productname_text.setText("");
            amount_numberPicker.setValue(1);
            brand_text.setText("");
            comment_text.setText("");

        } else {
            item = shoppingList.getItem(itemId);

            //thats not a pretty way to get it done, but the only one that came to my mind
            //amount_numberPicker.setValue(index) sets the picker to the index + amount_numberPicker.minValue()
            int itemAmount = item.getAmount();
            for(int i = 1; i < intNumsOnce.length; i++){
                if(itemAmount > 1000) itemAmount = 1000;
                if(intNumsOnce[i] == itemAmount){
                    itemAmount = i+1;
                    break;
                }
                if(intNumsOnce[i-1] < itemAmount && intNumsOnce[i+1] > itemAmount){
                    //if amount is not in the spinner, the next lower value gets selected
                    itemAmount = i+1;
                    break;
                }
            }

            // Fill UI elements with data from Item
            productname_text.setText(item.getName());
            amount_numberPicker.setValue(itemAmount);
            brand_text.setText(item.getBrand());
            comment_text.setText(item.getComment());
        }
        amount_numberPickerCalledWith = amount_numberPicker.getValue();

        //populate unit picker with units from database
        DisplayHelper displayHelper = new DisplayHelper(getActivity());


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

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, unitNames);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unit_spinner.setAdapter(spinnerArrayAdapter);
        unit_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUnitIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedUnitIndex = -1;
            }
        });

        Unit selectedUnit = isNewItem || item.getUnit() == null
                ? ListStorageFragment.getUnitStorage().getDefaultValue()
                : item.getUnit();

        if (selectedUnit != null) {
            unit_spinner.setSelection(units.indexOf(selectedUnit));
        }


        ArrayList<String> repeat_spinner_entries = new ArrayList<String>(3);
        repeat_spinner_entries.add(0, getString(R.string.days));
        repeat_spinner_entries.add(1, getString(R.string.weeks));
        repeat_spinner_entries.add(2, getString(R.string.months));
        ArrayAdapter<String> repeat_spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, repeat_spinner_entries);
        repeat_spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeat_spinner.setAdapter(repeat_spinnerAdapter);


        //get groups from the database and populate group spinner
        groups = ListStorageFragment.getGroupStorage().getItems();
        ArrayList<String> groupNames = new ArrayList<>();
        for (Group g : groups) {
            groupNames.add(displayHelper.getDisplayName(g));
        }

        ArrayAdapter<String> groupSpinnerArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, groupNames);
        groupSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        group_spinner.setAdapter(groupSpinnerArrayAdapter);
        group_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroupIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedGroupIndex = -1;
            }
        });


        Group selectedGroup = isNewItem || item.getGroup() == null
                ? ListStorageFragment.getGroupStorage().getDefaultValue()
                : item.getGroup();

        if (selectedGroup != null) {
            group_spinner.setSelection(groups.indexOf(selectedGroup));
        }

        //check highlight_checkbox, if item is already highlighted
        if (!isNewItem && item.isHighlight()) {
            highlight_checkbox.setChecked(true);
        } else {
            highlight_checkbox.setChecked(false);
        }

        if (!isNewItem) {
            boolean repeatItem = item.isRegularyRepeatItem();
            repeat_checkbox.setChecked(repeatItem);
            if (repeatItem) {
                TimePeriodsEnum timePeriod = item.getPeriodType();
                int spinnerPos = 0;
                switch (timePeriod) {
                    case DAYS:
                        spinnerPos = 0;
                        break;
                    case WEEKS:
                        spinnerPos = 1;
                        break;
                    case MONTHS:
                        spinnerPos = 2;
                        break;
                }
                repeat_spinner.setSelection(spinnerPos);
                int numberPickerVal = item.getSelectedRepeatTime();
                repeat_numberPicker.setValue(numberPickerVal);
            }
        }

        addTextWatcher();
    }

    /**
     * adds a Text watcher to productname_text in order to make it possible to set the group for an
     * already entered item
     */
    private void addTextWatcher() {
        productname_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                return; //do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = AutoCompletionHelper.removeSpacesAtEndOfWord(s.toString());
                Group group = autoCompletion.getGroup(name);

                if (group != null) {
                    group_spinner.setSelection(groups.indexOf(group));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                return; //do nothing
            }
        });
    }

}
