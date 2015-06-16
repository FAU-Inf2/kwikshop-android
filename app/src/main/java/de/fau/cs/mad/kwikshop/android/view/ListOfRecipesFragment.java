package de.fau.cs.mad.kwikshop.android.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.fau.cs.mad.kwikshop.android.R;

public class ListOfRecipesFragment extends android.support.v4.app.Fragment {

    @InjectView(android.R.id.list)
    ListView listView_Recipes;

    @InjectView(R.id.fab)
    View floatingActionButton;


    public static ListOfRecipesFragment newInstance(){
        return new ListOfRecipesFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        getActivity().setTitle(R.string.recipes);

        View rootView = inflater.inflate(R.layout.fragment_list_of_recipes, container, false);
        ButterKnife.inject(this, rootView);



        return rootView;
    }


}
