package de.cs.fau.mad.quickshop.android;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Collection;

import cs.fau.mad.quickshop_android.R;
import de.cs.fau.mad.quickshop.android.messages.ItemChangeType;
import de.cs.fau.mad.quickshop.android.messages.ItemChangedEvent;
import de.cs.fau.mad.quickshop.android.model.ListStorageFragment;
import de.greenrobot.event.EventBus;

public class ItemDetailsFragment extends Fragment {
    private static final String ARG_LISTID = "list_id";
    private static final String ARG_ITEMID = "item_id";

    private View mFragmentView;

    private ListStorageFragment m_ListStorageFragment;

    private int listID;
    private int itemID;

    private ShoppingList mShoppingList;
    private Item mItem;

    /* UI elements */
    private EditText productname_text;
    private EditText amount_text;
    private Spinner  unit_spinner;
    private EditText brand_text;
    private EditText comment_text;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFragmentView = inflater.inflate(R.layout.fragment_item_details, container, false);
        SetupUI();
        setHasOptionsMenu(true);

        EventBus.getDefault().register(this);

        return mFragmentView;
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void saveItem() {
        mItem.setName(productname_text.getText().toString());
        mItem.setAmount(Integer.parseInt(amount_text.getText().toString()));
        mItem.setBrand(brand_text.getText().toString());
        //mItem.setUnit(); // TODO: Set Unit
        mItem.setBrand(brand_text.getText().toString());

        mShoppingList.updateItem(mItem);
        m_ListStorageFragment.getLocalListStorage().saveList(mShoppingList);
        Toast.makeText(getActivity(), getResources().getString(R.string.itemdetails_saved), Toast.LENGTH_LONG).show();

        EventBus.getDefault().post(new ItemChangedEvent(ItemChangeType.PropertiesModified, mItem.getId(), mShoppingList.getId()));
    }

    private void SetupUI() {
        productname_text = (EditText) mFragmentView.findViewById(R.id.productname_text);
        amount_text = (EditText) mFragmentView.findViewById(R.id.amount_text);
        unit_spinner = (Spinner) mFragmentView.findViewById(R.id.unit_spinner);
        brand_text = (EditText) mFragmentView.findViewById(R.id.brand_text);
        comment_text = (EditText) mFragmentView.findViewById(R.id.comment_text);

        final FragmentManager fm = getActivity().getSupportFragmentManager();
        m_ListStorageFragment = (ListStorageFragment) fm.findFragmentByTag(ListStorageFragment.TAG_LISTSTORAGE);

        mShoppingList = m_ListStorageFragment.getLocalListStorage().loadList(listID);
        mItem = m_ListStorageFragment.getLocalListStorage().loadList(listID).getItem(itemID);

        // Fill UI elements with data from Item
        productname_text.setText(mItem.getName());
        amount_text.setText(Integer.toString(mItem.getAmount()));
        brand_text.setText(mItem.getBrand());
        comment_text.setText(mItem.getComment());

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
