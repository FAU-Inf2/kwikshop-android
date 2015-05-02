package de.cs.fau.mad.quickshop_android;

/**
 * Created by Robert on 01.05.2015.
 */

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import cs.fau.mad.quickshop_android.R;


public  class ListFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
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
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        /*
        listView = (ListView) rootView.findViewById(android.R.id.list);
        ArrayList<String> items = new ArrayList<String>();
        for (int i = 0; i < 10; i++){
            items.add("List " + i);
        }

        mListRowAdapter = new ListRowAdapter(getActivity(), R.layout.fragment_listview_row, items);
        listView.setAdapter(mListRowAdapter);
        */

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }



}
