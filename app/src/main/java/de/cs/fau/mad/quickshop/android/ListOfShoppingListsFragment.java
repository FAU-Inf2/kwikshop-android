package de.cs.fau.mad.quickshop.android;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;


import cs.fau.mad.quickshop_android.R;
import de.cs.fau.mad.quickshop.android.model.mock.ListStorageMock;

/**
 * Fragement for list of shopping lists
 */
public  class ListOfShoppingListsFragment extends Fragment {


    //region Constants

    private static final String ARG_SECTION_NUMBER = "section_number";

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

        View rootView = inflater.inflate(R.layout.fragment_list_of_shoppinglists, container, false);
        ListView m_ListView = (ListView) rootView.findViewById(android.R.id.list);


        // create adapter for list
        ListAdapter m_ListRowAdapter = new ListOfShoppingListsListRowAdapter(getActivity(), new ListStorageMock());
        m_ListView.setAdapter(m_ListRowAdapter);


        // wire up event handlers

        //click on list item
        m_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO: Open shopping list
                showToast("Shopping list selected");
            }
        });

        //long click on list item
        m_ListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO open shopping list details view
                showToast("Shopping list item log press");
                return true;
            }
        });

        //click on floating action button (add)
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Just for debugging
                showToast("Floating action button clicked");
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

    private void showToast(String text) {
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(getActivity(), text, duration);
        toast.show();
    }

    //endregion

}
