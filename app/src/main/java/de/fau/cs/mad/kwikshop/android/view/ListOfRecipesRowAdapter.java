package de.fau.cs.mad.kwikshop.android.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;

public class ListOfRecipesRowAdapter extends ArrayAdapter<Recipe> implements ObservableArrayList.Listener<Recipe> {


    //region Constants

    private static final int LAYOUT_ID = R.layout.fragment_list_of_recipes_row;

    //endregion


    //region Fields

    private final Activity parentActivity;
    private final ObservableArrayList<Recipe, Integer> recipes;

    //endregion


    //region Constructor

    public ListOfRecipesRowAdapter(Activity parentActivity, ObservableArrayList<Recipe, Integer> recipes) {

        super(parentActivity, LAYOUT_ID, recipes);

        if(parentActivity == null) {
            throw new IllegalArgumentException("'parentActivity' must not be null");
        }

        if (recipes == null) {
            throw new IllegalArgumentException("'recipes' must not be null");
        }

        this.parentActivity = parentActivity;
        this.recipes = recipes;
        this.recipes.addListener(this);
    }

    //endregion

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        ViewHolder viewHolder;
        if (view == null) {
            view = getLayoutInflater().inflate(LAYOUT_ID, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        //get the appropriate shopping list
        Recipe recipe = recipes.get(position);

        viewHolder.textView_RecipeName.setText(recipe.getName());

        return view;
    }


    @Override
    public long getItemId(int position) {
        return recipes.get(position).getId();
    }

    //region Private Methods

    /**
     * Gets the layout inflater from the associated activity
     * @return Returns a LayoutInflater
     */
    private LayoutInflater getLayoutInflater() {
        return (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //endregion


    @Override
    public void onItemAdded(Recipe newItem) {
        notifyDataSetChanged();
    }

    @Override
    public void onItemRemoved(Recipe removedItem) {
        notifyDataSetChanged();
    }

    @Override
    public void onItemModified(Recipe modifiedItem) {
        notifyDataSetChanged();
    }


    static class ViewHolder {

        @InjectView(R.id.list_row_textView_recipe_Main)
        TextView textView_RecipeName;


        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

}
