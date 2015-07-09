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
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;

/**
 * ListAdapter for displaying the list of shopping lists
 */
public class ListOfShoppingListsListRowAdapter extends ArrayAdapter<ShoppingList> implements ObservableArrayList.Listener<ShoppingList> {


    //region Constants

    private static final int LAYOUT_ID = R.layout.fragment_list_of_shoppinglists_row;

    //endregion


    //region Fields

    private final DateFormatter dateFormatter;

    private final Activity parentActivity;
    private final ObservableArrayList<ShoppingList, Integer> lists;

    //endregion


    //region Constructor

    public ListOfShoppingListsListRowAdapter(Activity parentActivity, ObservableArrayList<ShoppingList, Integer> lists) {

        super(parentActivity, R.layout.fragment_list_of_shoppinglists_row, lists);

        if(parentActivity == null) {
            throw new IllegalArgumentException("'parentActivity' must not be null");
        }

        if (lists == null) {
            throw new IllegalArgumentException("'lists' must not be null");
        }

        this.parentActivity = parentActivity;
        this.lists = lists;
        this.lists.addListener(this);
        this.dateFormatter = new DateFormatter(new DefaultResourceProvider(parentActivity));
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
        ShoppingList list = lists.get(position);

        //display the list's name and number of items in the list
        viewHolder.textView_ShoppingListName.setText(list.getName());
        viewHolder.textView_LastModified.setText(dateFormatter.formatDate(list.getLastModifiedDate()));
        viewHolder.textView_ItemCount.setText(list.getItems().size() + " " + parentActivity.getString(R.string.items));

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


    @Override
    public void onItemAdded(ShoppingList newItem) {
        notifyDataSetChanged();
    }

    @Override
    public void onItemRemoved(ShoppingList removedItem) {
        notifyDataSetChanged();
    }

    @Override
    public void onItemModified(ShoppingList modifiedItem) {
        notifyDataSetChanged();
    }

    static class ViewHolder {

        @InjectView(R.id.list_row_textView_Main)
        TextView textView_ShoppingListName;

        @InjectView(R.id.list_row_textView_Secondary)
        TextView textView_ItemCount;

        @InjectView(R.id.shoppinglist_textView_lastModifiedDate)
        TextView textView_LastModified;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }

    }

}
