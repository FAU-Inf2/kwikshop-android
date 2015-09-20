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
import de.fau.cs.mad.kwikshop.android.viewmodel.ItemDetailsViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.RecipeItemDetailsViewModel;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;


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
    protected ItemDetailsViewModel<Recipe> createViewModel(ObjectGraph objectGraph) {
        return objectGraph.get(RecipeItemDetailsViewModel.class);
    }


}
