package de.cs.fau.mad.quickshop.android.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import cs.fau.mad.quickshop_android.R;
import de.cs.fau.mad.quickshop.android.common.Item;
import de.cs.fau.mad.quickshop.android.common.ShoppingList;
import de.cs.fau.mad.quickshop.android.model.messages.ItemChangeType;
import de.cs.fau.mad.quickshop.android.model.messages.ItemChangedEvent;
import de.cs.fau.mad.quickshop.android.model.ListStorageFragment;
import de.greenrobot.event.EventBus;

public class ItemDetailsFragment extends Fragment {
    private static final String ARG_LISTID = "list_id";
    private static final String ARG_ITEMID = "item_id";

    private View mFragmentView;

    private ListStorageFragment m_ListStorageFragment;

    private int listID;
    private int itemID;
    private boolean isNewItem;

    private ShoppingList mShoppingList;
    private Item mItem;

    //TODO: move to db (otherwise changes are lost when exiting app)
    private static ArrayList<String> autocompleteSuggestions = new ArrayList<>();

    /* UI elements */
    private AutoCompleteTextView productname_text;
    private NumberPicker numberPicker;
    private Spinner  unit_spinner;
    private EditText brand_text;
    private EditText comment_text;

    /**
     * Creates a new instance of ItemDetailsFragment for a new shooping list item in the specified list
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
            listID = getArguments().getInt(ARG_LISTID);
            itemID = getArguments().getInt(ARG_ITEMID);
        }
        isNewItem = itemID == -1;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFragmentView = inflater.inflate(R.layout.fragment_item_details, container, false);
        SetupUI();
        setHasOptionsMenu(true);

        // set actionbar title
        getActivity().setTitle(R.string.title_fragment_item_details);

        // disable go back arrow
        ((ActionBarActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        EventBus.getDefault().register(this);

        return mFragmentView;
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void saveItem() {

        if (isNewItem) {
            mItem = new Item();
        }

        mItem.setName(productname_text.getText().toString());
        mItem.setAmount(numberPicker.getValue());
        mItem.setBrand(brand_text.getText().toString());
        //mItem.setUnit(); // TODO: Set Unit
        mItem.setBrand(brand_text.getText().toString());
        mItem.setComment(comment_text.getText().toString());

        if(!autocompleteSuggestions.contains(productname_text.getText().toString())) {
            autocompleteSuggestions.add(productname_text.getText().toString());
        }

        mShoppingList.updateItem(mItem);
        m_ListStorageFragment.getLocalListStorage().saveList(mShoppingList);
        Toast.makeText(getActivity(), getResources().getString(R.string.itemdetails_saved), Toast.LENGTH_LONG).show();

        ItemChangeType changeType = isNewItem ? ItemChangeType.Added : ItemChangeType.PropertiesModified;
        EventBus.getDefault().post(new ItemChangedEvent(changeType, mShoppingList.getId(), mItem.getId()));
    }

    private void SetupUI() {
        productname_text = (AutoCompleteTextView) mFragmentView.findViewById(R.id.productname_text);
        numberPicker = (NumberPicker) mFragmentView.findViewById(R.id.numberPicker);
        unit_spinner = (Spinner) mFragmentView.findViewById(R.id.unit_spinner);
        brand_text = (EditText) mFragmentView.findViewById(R.id.brand_text);
        comment_text = (EditText) mFragmentView.findViewById(R.id.comment_text);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, autocompleteSuggestions);
        productname_text.setAdapter(adapter);

        final FragmentManager fm = getActivity().getSupportFragmentManager();
        m_ListStorageFragment = (ListStorageFragment) fm.findFragmentByTag(ListStorageFragment.TAG_LISTSTORAGE);

        mShoppingList = m_ListStorageFragment.getLocalListStorage().loadList(listID);
        if(isNewItem) {

            productname_text.setText("");
            numberPicker.setValue(1);
            brand_text.setText("");
            comment_text.setText("");

        } else {
            mItem = m_ListStorageFragment.getLocalListStorage().loadList(listID).getItem(itemID);

            // Fill UI elements with data from Item
            productname_text.setText(mItem.getName());
            numberPicker.setValue(mItem.getAmount());
            brand_text.setText(mItem.getBrand());
            comment_text.setText(mItem.getComment());
        }


        // TODO: Fill spinner with real data from Units + select correct unit
        // http://stackoverflow.com/questions/2390102/how-to-set-selected-item-of-spinner-by-value-not-by-position
        String colors[] = {"Red","Blue","White","Yellow","Black", "Green","Purple","Orange","Grey"};
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, colors);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unit_spinner.setAdapter(spinnerArrayAdapter);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }


    //region Event Handlers

    public void onEvent(ItemChangedEvent event) {
        if (mShoppingList.getId() == event.getShoppingListId() && event.getItemId() == mItem.getId()) {
            SetupUI();
        }
    }

    //endregion

}
