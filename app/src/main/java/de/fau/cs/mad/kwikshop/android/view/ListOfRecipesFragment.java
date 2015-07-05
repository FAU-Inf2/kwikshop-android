package de.fau.cs.mad.kwikshop.android.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.common.Recipe;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.view.binding.ListViewItemCommandBinding;
import de.fau.cs.mad.kwikshop.android.viewmodel.ListOfRecipesViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;
import de.fau.cs.mad.kwikshop.android.viewmodel.di.KwikShopViewModelModule;

public class ListOfRecipesFragment extends FragmentWithViewModel implements ListOfRecipesViewModel.Listener<Recipe> {

    @InjectView(android.R.id.list)
    ListView listView_Recipes;

    @InjectView(R.id.fab)
    View floatingActionButton;

    private ListOfRecipesViewModel viewModel;
    private ListOfRecipesRowAdapter listAdapter;



    public static ListOfRecipesFragment newInstance(){
        return new ListOfRecipesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // set title for actionbar
        getActivity().setTitle(R.string.recipes);



        new ListStorageFragment().SetupLocalListStorageFragment(getActivity());

        // get view model (injected using dagger)
        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopViewModelModule(getActivity()));
        viewModel = objectGraph.get(ListOfRecipesViewModel.class);
        viewModel.setListener(this);

        View rootView = inflater.inflate(R.layout.fragment_list_of_recipes, container, false);
        ButterKnife.inject(this, rootView);


        // create adapter for list
        listAdapter = new ListOfRecipesRowAdapter(getActivity(), viewModel.getLists());
        listView_Recipes.setAdapter(listAdapter);

        // bind view to view model

        //click on list item
        bindListViewItem(listView_Recipes, ListViewItemCommandBinding.ListViewItemCommandType.Click, viewModel.getSelectRecipeCommand());
        //long click on list item
        bindListViewItem(listView_Recipes, ListViewItemCommandBinding.ListViewItemCommandType.LongClick, viewModel.getSelectRecipeDetailsCommand());

        //click on floating action button (add)
        bindButton(floatingActionButton, viewModel.getAddRecipeCommand());

        return rootView;
    }


    @Override
    public void onListsChanged(final ObservableArrayList<Recipe, Integer> oldValue,
                               final ObservableArrayList<Recipe, Integer> newValue) {

        oldValue.removeListener(listAdapter);
        listAdapter = new ListOfRecipesRowAdapter(getActivity(), viewModel.getLists());
        listView_Recipes.setAdapter(listAdapter);
    }

    @Override
    public void onFinish() {
        //nothing to do
    }


}
