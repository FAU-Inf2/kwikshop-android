package de.fau.cs.mad.kwikshop.android.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import javax.inject.Inject;

import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.viewmodel.RecipeItemDetailsViewModel;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;


public class RecipeItemDetailsFragment extends ItemDetailsFragment<Recipe> {

    // fields cannot be moved to base class because Dagger cant handle generics
    @Inject
    ListManager<Recipe> listManager;

    private RecipeItemDetailsViewModel viewModel;


    /**
     * Creates a new instance of RecipeItemDetailsFragment for a new recipe item in the specified recipe
     */
    public static RecipeItemDetailsFragment newInstance(int recipeID) {
        return newInstance(recipeID, -1);
    }

    /**
     * Creates a new instance of RecipeItemDetailsFragment for the specified recipe item
     */
    public static RecipeItemDetailsFragment newInstance(int recipeID, int itemID) {
        RecipeItemDetailsFragment fragment = new RecipeItemDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LISTID, recipeID);
        args.putInt(ARG_ITEMID, itemID);
        fragment.setArguments(args);

        return fragment;
    }


    public RecipeItemDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = super.onCreateView(inflater, container, savedInstanceState);

        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(getActivity()));
        viewModel = objectGraph.get(RecipeItemDetailsViewModel.class);
        objectGraph.inject(this);

        return view;
    }

    @Override
    protected void saveItem() {

        super.saveItem();

        Toast.makeText(getActivity(), getResources().getString(R.string.itemdetails_saved), Toast.LENGTH_LONG).show();
    }

    @Override
    protected ListType getListType() {
        return ListType.Recipe;
    }

    @Override
    protected ListManager<Recipe> getListManager() {
        return this.listManager;
    }

}
