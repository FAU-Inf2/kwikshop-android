package de.cs.fau.mad.quickshop.android;

/**
 * Created by Robert on 01.05.2015.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import cs.fau.mad.quickshop_android.R;


public  class ListFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String DATA = "data";
    private static final String SHOPPING_LIST = "shopping_list";
    private ArrayList<String> entry = new ArrayList<String>();
    private ListView listView;
    private ListRowAdapter mListRowAdapter;
    private TextView tvListEntry;


    public static ListFragment newInstance(int sectionNumber) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_overview, container, false);

        listView = (ListView) rootView.findViewById(android.R.id.list);

        // retrieve stored data for list overview
        SharedPreferences data = getActivity().getSharedPreferences(DATA, 0);
        Set<String> shoppingList = data.getStringSet(SHOPPING_LIST, null);

        // copy data to ArrayList
        if(shoppingList != null){
            Iterator<String> it = shoppingList.iterator();
            while(it.hasNext()){
                entry.add(it.next());
            }
        }

        // create adapter for list
        mListRowAdapter = new ListRowAdapter(getActivity(), R.layout.fragment_list_row, entry);
        listView.setAdapter(mListRowAdapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                new AlertDialog.Builder(getActivity())
                        .setTitle("Delete shopping list")   // todo: fix hardcoded
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                SharedPreferences data = getActivity().getSharedPreferences(DATA, 0);
                                SharedPreferences.Editor editor = data.edit();
                                Set<String> shoppingList = data.getStringSet(SHOPPING_LIST, null);
                                // create copy of list to manipulate
                                TreeSet<String> copyShoppingList = new TreeSet<String>(shoppingList);

                                if (copyShoppingList.contains(entry.get(position))) {
                                    // Remove Entry from List
                                    copyShoppingList.remove(entry.get(position));
                                    editor.putStringSet(SHOPPING_LIST, copyShoppingList);
                                    editor.apply();
                                    if (editor.commit()) {
                                        Toast.makeText(getActivity(), entry.get(position) + " wurde gelöscht!", Toast.LENGTH_LONG).show();
                                        // refresh Adapter
                                        mListRowAdapter.remove(entry.get(position));
                                        mListRowAdapter.notifyDataSetChanged();
                                    }
                                }

                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                return true;
            }


        });


        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }



}
