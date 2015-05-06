package de.cs.fau.mad.quickshop_android;

/**
 * Created by Robert on 01.05.2015.
 */

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import cs.fau.mad.quickshop_android.R;


public  class ListFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String DATA = "data";
    private static final String SHOPPING_LIST = "shopping_list";
    private ListView listView;
    private ListRowAdapter mListRowAdapter;


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

        SharedPreferences data = getActivity().getSharedPreferences(DATA, 0);
        Set<String> shoppingList = data.getStringSet(SHOPPING_LIST, null);
        ArrayList<String> items = new ArrayList<String>();

        if(shoppingList != null){
            Iterator<String> it = shoppingList.iterator();
            while(it.hasNext()){
                items.add(it.next());
            }
        }

        // currently no shopping list available
  
        mListRowAdapter = new ListRowAdapter(getActivity(), R.layout.fragment_list_row, items);
        listView.setAdapter(mListRowAdapter);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }



}
