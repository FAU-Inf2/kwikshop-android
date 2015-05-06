package de.cs.fau.mad.quickshop_android;

/**
 * Created by Robert on 01.05.2015.
 */

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import java.util.Set;
import java.util.TreeSet;

import cs.fau.mad.quickshop_android.R;


public  class AddListFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String DATA = "data";
    private static final String SHOPPING_LIST = "shopping_list";

    private ListView listView;
    private ListRowAdapter mListRowAdapter;
    private EditText etNewListName;

    public static AddListFragment newInstance(int sectionNumber) {
        AddListFragment fragment = new AddListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_list, container, false);
        etNewListName = (EditText) rootView.findViewById(R.id.et_add_list_name);
        setHasOptionsMenu(true); // Report that this fragment would like to participate in populating the options menu

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()){
            case R.id.action_add_list_save:
                saveNewShoppingList();
                return true;

            default:
                return true;
        }
    }

    private void saveNewShoppingList() {

        SharedPreferences data = getActivity().getSharedPreferences(DATA, 0);
        SharedPreferences.Editor editor = data.edit();
        Set<String> shoppingList = data.getStringSet(SHOPPING_LIST, null);

        if(shoppingList == null){
            shoppingList = new TreeSet<String>();
            Log.d("AddListFragment", "shoppingList is null");
        }

        Set<String> copyShoppinsList = new TreeSet<String>(shoppingList);
        copyShoppinsList.add(etNewListName.getText().toString());
        editor.putStringSet(SHOPPING_LIST,copyShoppinsList);
        editor.apply();

        // TODO

    }
}
