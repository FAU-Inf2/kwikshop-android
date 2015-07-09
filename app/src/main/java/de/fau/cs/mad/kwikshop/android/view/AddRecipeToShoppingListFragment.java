package de.fau.cs.mad.kwikshop.android.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.view.binding.ListViewItemCommandBinding;
import de.fau.cs.mad.kwikshop.android.viewmodel.AddRecipeToShoppingListViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;

public class AddRecipeToShoppingListFragment extends FragmentWithViewModel implements AddRecipeToShoppingListViewModel.Listener {

    @InjectView(android.R.id.list)
    ListView listView_Recipes;


    private AddRecipeToShoppingListViewModel viewModel;
    private ListOfRecipesRowAdapter listAdapter;
    private int shoppingListId;


    public static AddRecipeToShoppingListFragment newInstance(int shoppingListId){
        AddRecipeToShoppingListFragment fragment = new AddRecipeToShoppingListFragment();
        Bundle args = new Bundle();
        args.putInt("shoppingListId", shoppingListId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shoppingListId = getArguments().getInt("shoppingListId");
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        //add recipes
        menu.getItem(1).getSubMenu().getItem(2).setVisible(false);
        //mark everything as bought
        menu.getItem(1).getSubMenu().getItem(6).setVisible(false);
        //mark nothing as bought
        menu.getItem(1).getSubMenu().getItem(7).setVisible(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // set title for actionbar
        getActivity().setTitle(R.string.recipes);

        new ListStorageFragment().SetupLocalListStorageFragment(getActivity());

        // get view model (injected using dagger)
        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(getActivity()));
        viewModel = objectGraph.get(AddRecipeToShoppingListViewModel.class);
        viewModel.setListener(this);
        viewModel.initialize(shoppingListId);


        View rootView = inflater.inflate(R.layout.fragment_add_recipe_to_shoppinglist, container, false);
        ButterKnife.inject(this, rootView);

        // create adapter for list
        listAdapter = new ListOfRecipesRowAdapter(getActivity(), viewModel.getRecipes());
        listView_Recipes.setAdapter(listAdapter);

        // bind view to view model

        //click on list item
        bindListViewItem(listView_Recipes, ListViewItemCommandBinding.ListViewItemCommandType.Click, viewModel.getSelectRecipeCommand());


        return rootView;
    }


    @Override
    public void onRecipeChanged(final ObservableArrayList<Recipe, Integer> oldValue,
                                final ObservableArrayList<Recipe, Integer> newValue) {

        oldValue.removeListener(listAdapter);
        listAdapter = new ListOfRecipesRowAdapter(getActivity(), viewModel.getRecipes());
        listView_Recipes.setAdapter(listAdapter);
    }

    @Override
    public void onFinish() {
        //nothing to do
    }

}
