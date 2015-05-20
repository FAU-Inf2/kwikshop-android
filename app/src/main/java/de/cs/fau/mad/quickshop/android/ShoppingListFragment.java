package de.cs.fau.mad.quickshop.android;


import android.app.Activity;
//import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.support.v4.app.Fragment;

import java.util.ArrayList;

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
    private ShoppingListAdapter m_ShoppingListAdapter;

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
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

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
        ListView shoppingListView = (ListView) rootView.findViewById(R.id.list_shoppingList);

        m_ShoppingListAdapter = new ShoppingListAdapter(getActivity(), R.id.list_shoppingList,
                generateData(m_ListStorageFragment.getListStorage().loadList(listID)),
                m_ListStorageFragment.getListStorage().loadList(listID));

        shoppingListView.setAdapter(m_ShoppingListAdapter);


    /*
        // OnClickListener to open the item details view
        shoppingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Open item details view
                Toast.makeText(getActivity(), "ID: " + id + " - PID: " + parent.getItemIdAtPosition(position), Toast.LENGTH_LONG).show();
                fm.beginTransaction().replace(container, ItemDetailsFragment.newInstance(listID, (int) id))
                        .addToBackStack(null).commit();
            }
        });
    */

        //Setting spinner adapter to sort by button
        Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.sort_by_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        EventBus.getDefault().register(this);

        return rootView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    //endregion


    //region Event Handlers

    public void onEvent(ShoppingListChangedEvent event) {

        if (event.getListId() == this.listID && this.m_ShoppingListAdapter != null) {
            this.m_ShoppingListAdapter.clear();
            this.m_ShoppingListAdapter.addAll(generateData(m_ListStorageFragment.getListStorage().loadList(listID)));
            this.m_ShoppingListAdapter.notifyDataSetChanged();
        }
    }

    public void onEvent(ItemChangedEvent event) {
        if (event.getShoppingListId() == this.listID && this.m_ShoppingListAdapter != null) {
            this.m_ShoppingListAdapter.clear();
            this.m_ShoppingListAdapter.addAll(generateData(m_ListStorageFragment.getListStorage().loadList(listID)));
            this.m_ShoppingListAdapter.notifyDataSetChanged();
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
