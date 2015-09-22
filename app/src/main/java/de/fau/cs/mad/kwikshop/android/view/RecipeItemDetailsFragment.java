package de.fau.cs.mad.kwikshop.android.view;

import android.os.Bundle;

import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.viewmodel.ItemDetailsViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.RecipeItemDetailsViewModel;
import de.fau.cs.mad.kwikshop.common.Recipe;

public class RecipeItemDetailsFragment extends ItemDetailsFragment<Recipe> {


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
    protected ItemDetailsViewModel<Recipe> getViewModel(ObjectGraph objectGraph) {
        if(this.viewModel == null) {
            this.viewModel = objectGraph.get(RecipeItemDetailsViewModel.class);
        }
        return this.viewModel;
    }

    @Override
    protected void subscribeToViewModelEvents() {
        this.viewModel.setListener(this);
    }


}
