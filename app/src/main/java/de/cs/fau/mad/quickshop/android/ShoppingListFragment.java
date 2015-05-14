package de.cs.fau.mad.quickshop.android;

import android.app.Activity;
//import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v4.app.Fragment;

import cs.fau.mad.quickshop_android.R;
/**
 * Created by Nicolas on 14/05/2015.
 */
public class ShoppingListFragment extends Fragment {
    //region Constants

    private static final String ARG_SECTION_NUMBER = "section_number";

    //endregion


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

        View rootView = inflater.inflate(R.layout.fragment_shoppinglist, container, false);
        ListView m_ListView = (ListView) rootView.findViewById(android.R.id.list);

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
