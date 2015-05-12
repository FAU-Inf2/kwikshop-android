package de.cs.fau.mad.quickshop.android;

/**
 * Created by Robert on 01.05.2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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
        etNewListName.setFocusableInTouchMode(true);
        etNewListName.requestFocus();

        // listener for enter
        etNewListName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if(etNewListName.length() != 0){
                        saveNewShoppingList();
                    }
                    return true;
                }
                return false;
            }
        });

        // Report that this fragment would like to participate in populating the options menu
        setHasOptionsMenu(true);


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
                if(etNewListName.length() != 0){
                    saveNewShoppingList();
                }
                return true;
            default:
                return true;
        }
    }

    private void saveNewShoppingList() {

        // hide keyboard after pressing enter or save
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etNewListName.getWindowToken(), 0);

        // save shopping list name
        SharedPreferences data = getActivity().getSharedPreferences(DATA, 0);
        SharedPreferences.Editor editor = data.edit();
        Set<String> shoppingList = data.getStringSet(SHOPPING_LIST, null);

        // create new shopping list
        if(shoppingList == null){
            shoppingList = new TreeSet<String>();
            Toast.makeText(getActivity(), "shoppingList is null",Toast.LENGTH_LONG).show();
        }

        // save shopping list name
        Set<String> copyShoppinsList = new TreeSet<String>(shoppingList);
        copyShoppinsList.add(etNewListName.getText().toString());
        editor.putStringSet(SHOPPING_LIST,copyShoppinsList);
        editor.apply();

        // change current fragment to listFragment
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, ListFragment.newInstance(0)).commit();

    }


}
