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
import dagger.ObjectGraph;
import de.cs.fau.mad.quickshop.android.common.ShoppingList;
import de.cs.fau.mad.quickshop.android.model.ListStorageFragment;
import de.cs.fau.mad.quickshop.android.view.binding.ListViewItemCommandBinding;
import de.cs.fau.mad.quickshop.android.viewmodel.ListOfShoppingListsViewModel;
import de.cs.fau.mad.quickshop.android.viewmodel.di.QuickshopViewModelModule;

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

        // set title for actionbar
        getActivity().setTitle(R.string.title_activity_list_of_shopping_list);

        //TODO: storage setup needs to be simpler
        new ListStorageFragment().SetupLocalListStorageFragment(getActivity());

        // get view model (injected using dagger)
        ObjectGraph objectGraph = ObjectGraph.create(new QuickshopViewModelModule(getActivity()));
        viewModel = objectGraph.get(ListOfShoppingListsViewModel.class);
        viewModel.setListener(this);

        View rootView = inflater.inflate(R.layout.fragment_list_of_shoppinglists, container, false);
        ButterKnife.inject(this, rootView);



        // create adapter for list
        ListOfShoppingListsListRowAdapter listAdapter = new ListOfShoppingListsListRowAdapter(getActivity(), viewModel.getShoppingLists());
        listView_ShoppingLists.setAdapter(listAdapter);

        // bind view to view model

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
