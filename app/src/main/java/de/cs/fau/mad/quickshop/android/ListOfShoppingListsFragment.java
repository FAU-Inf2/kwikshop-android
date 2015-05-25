package de.cs.fau.mad.quickshop.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import cs.fau.mad.quickshop_android.R;
import de.cs.fau.mad.quickshop.android.common.ShoppingList;
import de.cs.fau.mad.quickshop.android.model.ListStorageFragment;
import de.cs.fau.mad.quickshop.android.view.ButtonBinding;
import de.cs.fau.mad.quickshop.android.view.DefaultViewLauncher;
import de.cs.fau.mad.quickshop.android.view.FragmentWithViewModel;
import de.cs.fau.mad.quickshop.android.view.ListViewItemCommandBinding;
import de.cs.fau.mad.quickshop.android.viewmodel.ListOfShoppingListsViewModel;
import de.cs.fau.mad.quickshop.android.viewmodel.common.Command;

/**
 * Fragment for list of shopping lists
 */
public class ListOfShoppingListsFragment extends FragmentWithViewModel implements ListOfShoppingListsViewModel.Listener {

    private ListView listView;
    private View rootView;

    private ListOfShoppingListsViewModel viewModel;

    public static ListOfShoppingListsFragment newInstance() {

        ListOfShoppingListsFragment fragment = new ListOfShoppingListsFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //TODO: storage setup needs to be simpler
        new ListStorageFragment().SetupLocalListStorageFragment(getActivity().getSupportFragmentManager(), getActivity().getApplicationContext());

        //create view model instance
        viewModel = new ListOfShoppingListsViewModel(new DefaultViewLauncher(getActivity()),
                ListStorageFragment.getLocalListStorage());
        viewModel.setListener(this);

        rootView = inflater.inflate(R.layout.fragment_list_of_shoppinglists, container, false);
        listView = (ListView) rootView.findViewById(android.R.id.list);

        // create adapter for list
        ListOfShoppingListsListRowAdapter listAdapter = new ListOfShoppingListsListRowAdapter(getActivity(), viewModel.getShoppingLists());
        listView.setAdapter(listAdapter);

        // wire up event handlers

        //click on list item
        bindListViewItem(android.R.id.list, ListViewItemCommandBinding.ListViewItemCommandType.Click, viewModel.getSelectShoppingListCommand());

        //long click on list item
        bindListViewItem(android.R.id.list, ListViewItemCommandBinding.ListViewItemCommandType.LongClick, viewModel.getSelectShoppingListDetailsCommand());

        //click on floating action button (add)
        bindButton(R.id.fab, viewModel.getAddShoppingListCommand());

        return rootView;
    }


    @Override
    public void onShoppingListsChanged(List<ShoppingList> newValue) {

        listView.setAdapter(new ListOfShoppingListsListRowAdapter(getActivity(), viewModel.getShoppingLists()));
    }

    @Override
    public void onFinish() {
        //nothing to dp
    }


    private void showToast(String text) {

        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(getActivity(), text, duration);
        toast.show();
    }

    @Override
    protected View getRootView() {
        return rootView;
    }



}
