package de.fau.cs.mad.kwikshop.android.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.fau.cs.mad.kwikshop.android.R;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.view.binding.ListViewItemCommandBinding;
import de.fau.cs.mad.kwikshop.android.viewmodel.ListOfShoppingListsViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;
import de.fau.cs.mad.kwikshop.android.viewmodel.di.KwikShopViewModelModule;

/**
 * Fragment for list of shopping lists
 */
public class ListOfShoppingListsFragment extends FragmentWithViewModel implements ListOfShoppingListsViewModel.Listener {


    @InjectView(android.R.id.list)
    ListView listView_ShoppingLists;

    @InjectView(R.id.fab)
    View floatingActionButton;

    private ListOfShoppingListsViewModel viewModel;
    private ListOfShoppingListsListRowAdapter listAdapter;


    public static ListOfShoppingListsFragment newInstance() {

        ListOfShoppingListsFragment fragment = new ListOfShoppingListsFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // set title for actionbar
        getActivity().setTitle(R.string.app_name);



        //TODO: storage setup needs to be simpler
        new ListStorageFragment().SetupLocalListStorageFragment(getActivity());

        // get view model (injected using dagger)
        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopViewModelModule(getActivity()));
        viewModel = objectGraph.get(ListOfShoppingListsViewModel.class);
        viewModel.setListener(this);

        View rootView = inflater.inflate(R.layout.fragment_list_of_shoppinglists, container, false);
        ButterKnife.inject(this, rootView);



        // create adapter for list
        listAdapter = new ListOfShoppingListsListRowAdapter(getActivity(), viewModel.getShoppingLists());
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
    public void onShoppingListsChanged(final ObservableArrayList<ShoppingList, Integer> oldValue,
                                       final ObservableArrayList<ShoppingList, Integer> newValue) {

        oldValue.replaceListener(listAdapter, null);
        listAdapter = new ListOfShoppingListsListRowAdapter(getActivity(), viewModel.getShoppingLists());
        listView_ShoppingLists.setAdapter(listAdapter);
    }

    @Override
    public void onFinish() {
        //nothing to dp
    }



}