package de.cs.fau.mad.quickshop.android;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import cs.fau.mad.quickshop_android.R;
import de.cs.fau.mad.quickshop.android.model.mock.ListStorageMock;

/**
 * Fragement for list of shopping lists
 */
public  class ListOfShoppingListsFragment extends Fragment {


    //region Constants

    private static final String ARG_SECTION_NUMBER = "section_number";

    //endregion


    //region Fields

    private ListView m_ListView;
    private ListAdapter m_ListRowAdapter;

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
        m_ListView = (ListView) rootView.findViewById(android.R.id.list);


        // create adapter for list
        m_ListRowAdapter = new ListOfShoppingListsListRowAdapter(getActivity(), new ListStorageMock());
        m_ListView.setAdapter(m_ListRowAdapter);
//        m_ListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
//
//                new AlertDialog.Builder(getActivity())
//                        .setTitle("Delete shopping list")   // todo: fix hardcoded
//                        .setMessage("Are you sure you want to delete this m_Entries?")
//                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                // continue with delete
//                                SharedPreferences data = getActivity().getSharedPreferences(DATA, 0);
//                                SharedPreferences.Editor editor = data.edit();
//                                Set<String> shoppingList = data.getStringSet(LIST_OF_SHOPPING_LISTS, null);
//                                // create copy of list to manipulate
//                                TreeSet<String> copyShoppingList = new TreeSet<String>(shoppingList);
//
//                                if (copyShoppingList.contains(m_Entries.get(position))) {
//                                    // Remove Entry from List
//                                    copyShoppingList.remove(m_Entries.get(position));
//                                    editor.putStringSet(LIST_OF_SHOPPING_LISTS, copyShoppingList);
//                                    editor.apply();
//                                    if (editor.commit()) {
//                                        Toast.makeText(getActivity(), m_Entries.get(position) + " wurde gelöscht!", Toast.LENGTH_LONG).show();
//                                        // refresh Adapter
//                                        m_ListRowAdapter.remove(m_Entries.get(position));
//                                        m_ListRowAdapter.notifyDataSetChanged();
//                                    }
//                                }
//
//                            }
//                        })
//                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                // do nothing
//                            }
//                        })
//                        .setIcon(android.R.drawable.ic_dialog_alert)
//                        .show();
//
//                return true;
//            }
//
//
//        });


        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }


    //endregion

}
