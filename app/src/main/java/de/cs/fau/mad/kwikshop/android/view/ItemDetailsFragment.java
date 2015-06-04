package de.cs.fau.mad.kwikshop.android.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import cs.fau.mad.kwikshop_android.R;
import de.cs.fau.mad.kwikshop.android.common.Group;
import de.cs.fau.mad.kwikshop.android.common.Item;
import de.cs.fau.mad.kwikshop.android.common.ShoppingList;
import de.cs.fau.mad.kwikshop.android.common.Unit;
import de.cs.fau.mad.kwikshop.android.model.messages.ItemChangeType;
import de.cs.fau.mad.kwikshop.android.model.messages.ItemChangedEvent;
import de.cs.fau.mad.kwikshop.android.model.ListStorageFragment;
import de.cs.fau.mad.kwikshop.android.view.interfaces.SaveCancelActivity;
import de.greenrobot.event.EventBus;

public class ItemDetailsFragment extends Fragment {

    private static final String ARG_LISTID = "list_id";
    private static final String ARG_ITEMID = "item_id";

    private View rootView;

    private ActionBar actionBar;

    private int listId;
    private int itemId;
    private boolean isNewItem;

    private ShoppingList shoppingList;
    private Item item;
    private List<Unit> units;
    private int selectedUnitIndex = -1;
    private List<Group> groups;
    private int selectedGroupIndex = -1;


    //TODO: move to db (otherwise changes are lost when exiting app)
    private static ArrayList<String> autocompleteSuggestions = new ArrayList<>();

    /* UI elements */

    @InjectView(R.id.productname_text)
    AutoCompleteTextView productname_text;

    @InjectView(R.id.numberPicker)
    NumberPicker numberPicker;

    @InjectView(R.id.unit_spinner)
    Spinner unit_spinner;

    @InjectView(R.id.brand_text)
    EditText brand_text;

    @InjectView(R.id.comment_text)
    EditText comment_text;

    @InjectView(R.id.group_spinner)
    Spinner group_spinner;

    @InjectView(R.id.highlight_checkBox)
    CheckBox highlight_checkbox;


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
     * Creats a new instance of ItemDetailsFragment for the specified shopping list item
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        MenuItem menuItem = menu.findItem(R.id.empty);
        menuItem.setVisible(false);

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            listId = getArguments().getInt(ARG_LISTID);
            itemId = getArguments().getInt(ARG_ITEMID);
        }
        actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        isNewItem = itemId == -1;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_item_details, container, false);
        ButterKnife.inject(this, rootView);

        SetupUI();

        // set actionbar with save and cancel buttons
        setCustomActionBar();

        EventBus.getDefault().register(this);

        // set actionbar title
        if (isNewItem) {
            getActivity().setTitle(R.string.title_fragment_item_details);
        } else {
            getActivity().setTitle(productname_text.getText().toString());
        }
        // disable go back arrow
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);


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

    void setCustomActionBar() {

        //show custom action bar
        actionBar.setDisplayShowCustomEnabled(true);
        View view = getActivity().getLayoutInflater().inflate(R.layout.actionbar_save_cancel, null);
        final View savedActionBarView = actionBar.getCustomView();
        actionBar.setCustomView(view);

        Button saveButton = (Button) actionBar.getCustomView().findViewById(R.id.button_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (productname_text.getText().length() > 0) {
                    handleHighlight();
                    saveItem();
                    getActivity().getSupportFragmentManager().popBackStackImmediate();
                    actionBar.setCustomView(savedActionBarView);
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_empty_productname), Toast.LENGTH_LONG).show();
                }
            }
        });

        Button cancelButton = (Button) actionBar.getCustomView().findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionBar.setCustomView(savedActionBarView);
                getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });
    }


    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }


    public void onEvent(ItemChangedEvent event) {
        if (shoppingList.getId() == event.getShoppingListId() && event.getItemId() == item.getId()) {
            SetupUI();
        }
    }


    private void saveItem() {

        if (isNewItem) {
            item = new Item();
        }

        item.setName(productname_text.getText().toString());
        item.setAmount(numberPicker.getValue());
        item.setBrand(brand_text.getText().toString());

        if (selectedUnitIndex >= 0) {
            Unit u = units.get(selectedUnitIndex);
            item.setUnit(u);
        } else {
            item.setUnit(null);
        }

        if (selectedGroupIndex >= 0) {
            Group g = groups.get(selectedGroupIndex);
            item.setGroup(g);
        } else {
            item.setGroup(null);
        }

        item.setBrand(brand_text.getText().toString());
        item.setComment(comment_text.getText().toString());

        if (!autocompleteSuggestions.contains(productname_text.getText().toString())) {
            autocompleteSuggestions.add(productname_text.getText().toString());
        }

        if (isNewItem) {
            shoppingList.addItem(item);
        }

        ListStorageFragment.getLocalListStorage().saveList(shoppingList);
        Toast.makeText(getActivity(), getResources().getString(R.string.itemdetails_saved), Toast.LENGTH_LONG).show();

        ItemChangeType changeType = isNewItem ? ItemChangeType.Added : ItemChangeType.PropertiesModified;
        EventBus.getDefault().post(new ItemChangedEvent(changeType, shoppingList.getId(), item.getId()));
    }

    //highlights the item, if checkbox is checked
    private void handleHighlight(){
        if(highlight_checkbox.isChecked()){
            item.setHighlight(true);
        }else{
            item.setHighlight(false);
        }
    }

    private void SetupUI() {

        //populate number picker
        String[] nums = new String[1000];
        for (int i = 0; i < nums.length; i++) {
            nums[i] = Integer.toString(i + 1);
        }
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(1000);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setDisplayedValues(nums);

        //wire up auto-complete for product name
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, autocompleteSuggestions);
        productname_text.setAdapter(adapter);

        // load shopping list and item and set values in UI
        shoppingList = ListStorageFragment.getLocalListStorage().loadList(listId);
        if (isNewItem) {

            productname_text.setText("");
            numberPicker.setValue(1);
            brand_text.setText("");
            comment_text.setText("");

        } else {
            item = shoppingList.getItem(itemId);

            // Fill UI elements with data from Item
            productname_text.setText(item.getName());
            numberPicker.setValue(item.getAmount());
            brand_text.setText(item.getBrand());
            comment_text.setText(item.getComment());
        }

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

        if (!isNewItem && item.getUnit() != null) {
            int index = units.indexOf(item.getUnit());
            unit_spinner.setSelection(index);
        }

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


        if (!isNewItem && item.getGroup() != null) {
            int index = groups.indexOf(item.getGroup());
            group_spinner.setSelection(index);
        }

        //check highlight_checkbox, if item is already highlighted
        if(item.isHighlight()) highlight_checkbox.setChecked(true);
        else highlight_checkbox.setChecked(false);

/*
        //Button that shows important if unhighlighted item should be highlighted
        if(item.isHighlight()) highlightbutton.setText("Not important");
        highlightbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                item.setHighlight(!item.isHighlight());
                if(item.isHighlight()) highlightbutton.setText("Not important");
                else highlightbutton.setText("Important");
            }
        });

*/
        }


    }
