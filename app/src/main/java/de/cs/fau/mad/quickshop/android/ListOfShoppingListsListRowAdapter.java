package de.cs.fau.mad.quickshop.android;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cs.fau.mad.quickshop_android.R;
import de.cs.fau.mad.quickshop.android.common.ShoppingList;
import de.cs.fau.mad.quickshop.android.model.ListStorage;

/**
 * ListAdapter for displaying the list of shopping lists
 */
public class ListOfShoppingListsListRowAdapter implements ListAdapter {


    //region Constants

    private static final int LAYOUT_ID = R.layout.fragment_list_of_shoppinglists_row;

    //endregion


    //region Fields

    private ArrayList<DataSetObserver> m_Observers = new ArrayList<>();

    private final Activity m_ParentActivity;
    private final ListStorage m_ListStorage;

    //endregion


    //region Constructor

    public ListOfShoppingListsListRowAdapter(Activity parentActivity, ListStorage listStorage) {

        if(parentActivity == null) {
            throw new IllegalArgumentException("'parentActivity' must not be null");
        }

        if(listStorage == null) {
            throw new IllegalArgumentException("'listStorage' must not be null");
        }

        this.m_ParentActivity = parentActivity;
        this.m_ListStorage = listStorage;
    }

    //endregion


    //region ListAdapter Implementation

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        //get the appropriate shopping list
        ShoppingList list = m_ListStorage.getAllLists().get(position);

        //if view is null, inflate a new one
        if (view == null) {

            LayoutInflater inflater = getLayoutInflater();
            view = inflater.inflate(LAYOUT_ID, null);

        }

        //display the list's name and number of items in the list
        TextView shoppingListNameView = (TextView) view.findViewById(R.id.list_row_textView_Main);
        shoppingListNameView.setText(list.getName());

        TextView itemCountView = (TextView) view.findViewById(R.id.list_row_textView_Secondary);
        itemCountView.setText(list.getItems().size() + " " + m_ParentActivity.getString(R.string.items));

        return view;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        m_Observers.add(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        m_Observers.remove(observer);
    }

    @Override
    public int getCount() {
        return m_ListStorage.getAllLists().size();
    }

    @Override
    public Object getItem(int position) {
        return m_ListStorage.getAllLists().get(position);
    }

    @Override
    public long getItemId(int position) {
        return m_ListStorage.getAllLists().get(position).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    //endregion


    //region Public Methods

    public void notifyDataSetChanged() {

        for(DataSetObserver observer : m_Observers) {
            observer.onChanged();
        }
    }

    //endregion


    //region Private Methods

    /**
     * Gets the layout inflater from the associated activity
     * @return Returns a LayoutInflater
     */
    private LayoutInflater getLayoutInflater() {
        return (LayoutInflater) m_ParentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //endregion


}
