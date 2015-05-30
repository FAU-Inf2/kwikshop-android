package de.cs.fau.mad.quickshop.android.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cs.fau.mad.quickshop_android.R;
import de.cs.fau.mad.quickshop.android.common.Item;
import de.cs.fau.mad.quickshop.android.common.ShoppingList;
import de.cs.fau.mad.quickshop.android.common.Unit;
import de.cs.fau.mad.quickshop.android.model.DatabaseHelper;
import de.cs.fau.mad.quickshop.android.model.messages.ItemChangeType;
import de.cs.fau.mad.quickshop.android.model.messages.ItemChangedEvent;
import de.cs.fau.mad.quickshop.android.model.ListStorageFragment;
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
    private int selectedUnit = -1;

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
        inflater.inflate(R.menu.item_details_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_item_details_save:
                if(productname_text.getText().length() > 0) {

                    saveItem();
                    getActivity().getSupportFragmentManager().popBackStack();

                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_empty_productname), Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

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

        SetupUI();
        setHasOptionsMenu(true);

        // hide soft keys until users decides he wants to
        hideKeyboard(); //todo does not work

        // set actionbar title
        getActivity().setTitle(R.string.title_fragment_item_details);

        // disable go back arrow
        ((ActionBarActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        EventBus.getDefault().register(this);

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
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

        if (selectedUnit >= 0) {
            Unit u = units.get(selectedUnit);
            item.setUnit(u);
        } else {
            item.setUnit(null);
        }

        item.setBrand(brand_text.getText().toString());
        item.setComment(comment_text.getText().toString());

        if (!autocompleteSuggestions.contains(productname_text.getText().toString())) {
            autocompleteSuggestions.add(productname_text.getText().toString());
        }

        shoppingList.updateItem(item);
        ListStorageFragment.getLocalListStorage().saveList(shoppingList);
        Toast.makeText(getActivity(), getResources().getString(R.string.itemdetails_saved), Toast.LENGTH_LONG).show();

        ItemChangeType changeType = isNewItem ? ItemChangeType.Added : ItemChangeType.PropertiesModified;
        EventBus.getDefault().post(new ItemChangedEvent(changeType, shoppingList.getId(), item.getId()));
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
        UnitDisplayHelper unitDisplayHelper = new UnitDisplayHelper(getActivity());


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
            unitNames.add(unitDisplayHelper.getDisplayName(u));
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, unitNames);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unit_spinner.setAdapter(spinnerArrayAdapter);
        unit_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUnit = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedUnit = -1;
            }
        });

        if (!isNewItem && item.getUnit() != null) {
            int index = units.indexOf(item.getUnit());
            unit_spinner.setSelection(index);
        }


    }



}
