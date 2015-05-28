package de.cs.fau.mad.quickshop.android.view;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import cs.fau.mad.quickshop_android.R;

/**
 * Created by Robert on 21.05.2015.
 */
public class SettingFragment extends Fragment{

    private static final String ARG_SECTION_NUMBER = "section_number";
    private String OPTION_ONE = "option_one";
    private View rootView;
    private ListView listView;
    private ArrayList setList;



    public static SettingFragment newInstance(int sectionNumber) {

        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rootView = inflater.inflate(R.layout.fragment_setting, container, false);
        listView = (ListView) rootView.findViewById(android.R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        // set title for actionbar
        getActivity().setTitle(R.string.title_activity_settings);

        setList = new ArrayList<String>();
        setList.add(OPTION_ONE);


        SettingAdapter objAdapter = new SettingAdapter(getActivity(), R.layout.fragment_setting_row, setList);
        listView.setAdapter(objAdapter);


        return rootView;




    }
}
