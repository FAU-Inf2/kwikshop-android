package de.cs.fau.mad.quickshop.android;

import android.app.Activity;
//import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.Iterator;

import cs.fau.mad.quickshop_android.R;
import de.cs.fau.mad.quickshop.android.messages.ItemChangedEvent;
import de.cs.fau.mad.quickshop.android.messages.ShoppingListChangedEvent;
import de.cs.fau.mad.quickshop.android.model.ListStorageFragment;
import de.cs.fau.mad.quickshop.android.model.mock.ListStorageMock;
import de.greenrobot.event.EventBus;


public class ShoppingListFragment extends Fragment {

    //region Constants

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_LISTID = "list_id";

    //endregion


    //region Fields

    private ListStorageFragment m_ListStorageFragment;
    private ShoppingListAdapter m_ShoppingList;

    private int listID;

    //endregion


    //region Construction

    public static ShoppingListFragment newInstance(int sectionNumber, int listID) {
        ShoppingListFragment fragment = new ShoppingListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putInt(ARG_LISTID, listID);
        fragment.setArguments(args);
        return fragment;
    }

    //endregion


    //region Overrides

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            listID = getArguments().getInt(ARG_LISTID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final FragmentManager fm = getActivity().getSupportFragmentManager();

        m_ListStorageFragment = (ListStorageFragment) fm.findFragmentByTag(ListStorageFragment.TAG_LISTSTORAGE);
        if (m_ListStorageFragment == null) {
            m_ListStorageFragment = new ListStorageFragment();
            m_ListStorageFragment.setListStorage(new ListStorageMock());
            fm.beginTransaction().add(
                    m_ListStorageFragment, ListStorageFragment.TAG_LISTSTORAGE)
                    .commit();
        }
        View rootView = inflater.inflate(R.layout.fragment_shoppinglist, container, false);
        ListView m_ShoppingListView = (ListView) rootView.findViewById(R.id.list_shoppingList);
        if (m_ShoppingList == null) {
            m_ShoppingList = new ShoppingListAdapter(getActivity(), R.id.list_shoppingList, generateData(m_ListStorageFragment.getListStorage().loadList(listID)),
                    m_ListStorageFragment.getListStorage().loadList(listID));
            m_ShoppingListView.setAdapter(m_ShoppingList);
        }

        // OnClickListener to open the item details view
        m_ShoppingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Open item details view
                Toast.makeText(getActivity(), "ID: " + id + " - PID: " + parent.getItemIdAtPosition(position), Toast.LENGTH_LONG).show();
                fm.beginTransaction().replace(R.id.container, ItemDetailsFragment.newInstance(listID, (int) id))
                        .addToBackStack(null).commit();
            }
        });

        EventBus.getDefault().register(this);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    //endregion


    //region Event Handlers

    public void onEvent(ShoppingListChangedEvent event) {

        if (event.getListId() == this.listID && this.m_ShoppingList != null) {
            this.m_ShoppingList.notifyDataSetChanged();
        }
    }

    public void onEvent(ItemChangedEvent event) {
        if (event.getShoppingListId() == this.listID && this.m_ShoppingList != null) {
            this.m_ShoppingList.notifyDataSetChanged();
        }
    }

    //endregion


    //region Private Methods

    private ArrayList<String> generateData(ShoppingList shoppingList) {
        ArrayList<String> items = new ArrayList<>();
        for (Item item : shoppingList.getItems()) {
            items.add(item.getName());
        }
        return items;
    }

    //endregion

}
