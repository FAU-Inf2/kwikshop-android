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
import de.cs.fau.mad.quickshop.android.model.ListStorageFragment;
import de.cs.fau.mad.quickshop.android.model.mock.ListStorageMock;

/**
 * Created by Nicolas on 14/05/2015.
 */
public class ShoppingListFragment extends Fragment {
    //region Constants

    private static final String ARG_SECTION_NUMBER = "section_number";

    //endregion

    private ListStorageFragment m_ListStorageFragment;
    private ShoppingListAdapter m_ShoppingList;
    //region Construction

    public static ShoppingListFragment newInstance(int sectionNumber) {


        ShoppingListFragment fragment = new ShoppingListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;

    }

    //endregion


    //region Overrides

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
            m_ShoppingList = new ShoppingListAdapter(getActivity(), R.id.list_shoppingList, generateData(m_ListStorageFragment.getListStorage().getAllLists().firstElement()));
            m_ShoppingListView.setAdapter(m_ShoppingList);
        }

        // OnClickListener to open the item details view
        m_ShoppingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO: Open item details view
                Toast.makeText(getActivity(), "Test" + id, Toast.LENGTH_LONG).show();
                fm.beginTransaction().replace(R.id.container, ItemDetailsFragment.newInstance(2)).commit();
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
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
