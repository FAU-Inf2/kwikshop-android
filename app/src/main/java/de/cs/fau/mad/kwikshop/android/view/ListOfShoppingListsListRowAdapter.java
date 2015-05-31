package de.cs.fau.mad.kwikshop.android.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import cs.fau.mad.kwikshop_android.R;
import de.cs.fau.mad.kwikshop.android.common.ShoppingList;
import de.cs.fau.mad.kwikshop.android.viewmodel.common.ObservableArrayList;

/**
 * ListAdapter for displaying the list of shopping lists
 */
public class ListOfShoppingListsListRowAdapter extends ArrayAdapter<ShoppingList> {


    //region Constants

    private static final int LAYOUT_ID = R.layout.fragment_list_of_shoppinglists_row;

    //endregion


    //region Fields

    private final Activity parentActivity;
    private final ObservableArrayList<ShoppingList> lists;

    //endregion


    //region Constructor

    public ListOfShoppingListsListRowAdapter(Activity parentActivity, ObservableArrayList<ShoppingList> lists) {

        super(parentActivity, R.layout.fragment_list_of_shoppinglists_row, lists);

        if(parentActivity == null) {
            throw new IllegalArgumentException("'parentActivity' must not be null");
        }

        if (lists == null) {
            throw new IllegalArgumentException("'lists' must not be null");
        }

        this.parentActivity = parentActivity;
        this.lists = lists;

        //TODO: subscribe to events from observable list
    }

    //endregion

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        //get the appropriate shopping list
        ShoppingList list = lists.get(position);

        //if view is null, inflate a new one
        if (view == null) {

            LayoutInflater inflater = getLayoutInflater();
            view = inflater.inflate(LAYOUT_ID, null);

        }

        //display the list's name and number of items in the list
        TextView shoppingListNameView = (TextView) view.findViewById(R.id.list_row_textView_Main);
        shoppingListNameView.setText(list.getName());

        TextView itemCountView = (TextView) view.findViewById(R.id.list_row_textView_Secondary);
        itemCountView.setText(list.getItems().size() + " " + parentActivity.getString(R.string.items));

        return view;
    }


    @Override
    public long getItemId(int position) {
        return lists.get(position).getId();
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


}
