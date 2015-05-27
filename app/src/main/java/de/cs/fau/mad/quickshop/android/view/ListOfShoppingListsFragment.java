package de.cs.fau.mad.quickshop.android.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cs.fau.mad.quickshop_android.R;
import de.cs.fau.mad.quickshop.android.common.ShoppingList;
import de.cs.fau.mad.quickshop.android.model.ListStorageFragment;
import de.cs.fau.mad.quickshop.android.view.binding.ListViewItemCommandBinding;
import de.cs.fau.mad.quickshop.android.viewmodel.ListOfShoppingListsViewModel;

/**
 * Fragment for list of shopping lists
 */
public class ListOfShoppingListsFragment extends FragmentWithViewModel implements ListOfShoppingListsViewModel.Listener {


    @InjectView(android.R.id.list)
    ListView listView_ShoppingLists;

    @InjectView(R.id.fab)
    View floatingActionButton;

    private ListOfShoppingListsViewModel viewModel;

    public static ListOfShoppingListsFragment newInstance() {

        ListOfShoppingListsFragment fragment = new ListOfShoppingListsFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //TODO: storage setup needs to be simpler
        new ListStorageFragment().SetupLocalListStorageFragment(getActivity());

        View rootView = inflater.inflate(R.layout.fragment_list_of_shoppinglists, container, false);
        ButterKnife.inject(this, rootView);

        //create view model instance
        viewModel = new ListOfShoppingListsViewModel(new DefaultViewLauncher(getActivity()),
                ListStorageFragment.getLocalListStorage());
        viewModel.setListener(this);


        // create adapter for list
        ListOfShoppingListsListRowAdapter listAdapter = new ListOfShoppingListsListRowAdapter(getActivity(), viewModel.getShoppingLists());
        listView_ShoppingLists.setAdapter(listAdapter);

        // wire up event handlers

        //click on list item
        bindListViewItem(listView_ShoppingLists, ListViewItemCommandBinding.ListViewItemCommandType.Click, viewModel.getSelectShoppingListCommand());
        //long click on list item
        bindListViewItem(listView_ShoppingLists, ListViewItemCommandBinding.ListViewItemCommandType.LongClick, viewModel.getSelectShoppingListDetailsCommand());

        //click on floating action button (add)
        bindButton(floatingActionButton, viewModel.getAddShoppingListCommand());

        return rootView;
    }


    @Override
    public void onShoppingListsChanged(List<ShoppingList> newValue) {

        listView_ShoppingLists.setAdapter(new ListOfShoppingListsListRowAdapter(getActivity(), viewModel.getShoppingLists()));
    }

    @Override
    public void onFinish() {
        //nothing to dp
    }



}
