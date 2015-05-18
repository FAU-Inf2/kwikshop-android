package de.cs.fau.mad.quickshop.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;


import cs.fau.mad.quickshop_android.R;
import de.cs.fau.mad.quickshop.android.model.ListStorageFragment;
import de.cs.fau.mad.quickshop.android.model.mock.ListStorageMock;

/**
 * Fragement for list of shopping lists
 */
public  class ListOfShoppingListsFragment extends Fragment {


    //region Constants

    private static final String ARG_SECTION_NUMBER = "section_number";

    //endregion


    //region Fields

    private ListStorageFragment m_ListStorageFragment;
    private ListView m_ListView;
    private ListOfShoppingListsListRowAdapter m_ListRowAdapter;

    //endregion


    //region Construction

    public static ListOfShoppingListsFragment newInstance(int sectionNumber) {

        ListOfShoppingListsFragment fragment = new ListOfShoppingListsFragment();
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


        View rootView = inflater.inflate(R.layout.fragment_list_of_shoppinglists, container, false);
        m_ListView = (ListView) rootView.findViewById(android.R.id.list);


        // create adapter for list
        m_ListRowAdapter = new ListOfShoppingListsListRowAdapter(getActivity(), m_ListStorageFragment.getListStorage());
        m_ListView.setAdapter(m_ListRowAdapter);


        // wire up event handlers

        //click on list item
        m_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO: Open shopping list
                showToast("Shopping list selected, ID: " + id);
                fm.beginTransaction().replace(R.id.container, ShoppingListFragment.newInstance(0, (int)id)).commit();

            }
        });

        //long click on list item
        m_ListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getActivity(), ShoppingListDetailActivity.class)
                        .putExtra(ShoppingListDetailActivity.EXTRA_SHOPPINGLISTID, id);

                startActivity(intent);
                return true;
            }
        });

        //click on floating action button (add)
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), ShoppingListDetailActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));

        if (m_ListRowAdapter != null) {
            m_ListRowAdapter.reload();
        }

    }


    //endregion


    //region Private Methods

    private void showToast(String text) {
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(getActivity(), text, duration);
        toast.show();
    }

    //endregion

}
