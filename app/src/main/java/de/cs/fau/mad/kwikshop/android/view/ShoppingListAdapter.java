package de.cs.fau.mad.kwikshop.android.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cs.fau.mad.kwikshop_android.R;
import de.cs.fau.mad.kwikshop.android.common.Item;
import de.cs.fau.mad.kwikshop.android.common.ShoppingList;
import de.cs.fau.mad.kwikshop.android.common.Unit;
import de.cs.fau.mad.kwikshop.android.util.AsyncTaskHelper;
import de.cs.fau.mad.kwikshop.android.util.StringHelper;

public class ShoppingListAdapter extends com.nhaarman.listviewanimations.ArrayAdapter<Integer> implements UndoAdapter {

    private final ShoppingList shoppingList;
    private final Context context;
    private final DisplayHelper displayHelper;
    private final boolean groupItems;

    /**
     * Initializes a new instance of ShoppingListAdapter
     *
     * @param context      The adapter's context
     * @param objects      The ids of the shopping list items to be displayed
     * @param shoppingList The shopping list which's items to display
     * @param groupItems   Group shopping list items by their group and display group headers
     */
    public ShoppingListAdapter(Context context, List<Integer> objects, ShoppingList shoppingList, boolean groupItems) {

        super(objects);
        this.shoppingList = shoppingList;
        this.context = context;
        this.displayHelper = new DisplayHelper(context);
        this.groupItems = groupItems;

    }

    @Override
    public long getItemId(int position) {
        return getItem(position);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        ViewHolder viewHolder;
        if(view == null ){
            view = LayoutInflater.from(context).inflate(R.layout.fragment_shoppinglist_row, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Item item = shoppingList.getItem(getItem(position));


        //if item is highlighted, set color to red
        if(item.isHighlight()){
            viewHolder.textView_ShoppingListName.setTextColor(Color.RED);
        }

        // Fill Views

        // Item name
        viewHolder.textView_ShoppingListName.setText(item.getName());

        // Comment
        String comment = item.getComment();
        if (StringHelper.isNullOrWhiteSpace(comment)) {
            viewHolder.textView_Comment.setVisibility(View.GONE);
        } else {
            viewHolder.textView_Comment.setVisibility(View.VISIBLE);
            viewHolder.textView_Comment.setText(comment);
        }

        // brand
        String brand = item.getBrand();
        if (StringHelper.isNullOrWhiteSpace(brand)) {
            viewHolder.textView_Brand.setVisibility(View.GONE);
        } else {
            viewHolder.textView_Brand.setVisibility(View.VISIBLE);
            viewHolder.textView_Brand.setText(brand);
        }

        // amount
        int amount = item.getAmount();
        if (amount <= 1) {
            viewHolder.textView_Amount.setVisibility(View.GONE);
        } else {
            String unitStr = displayHelper.getShortDisplayName(item.getUnit());

            viewHolder.textView_Amount.setVisibility(View.VISIBLE);
            viewHolder.textView_Amount.setText(String.format("%d %s", amount, unitStr));
        }

        // group header
        //determine if we have to show the group header (above the item)
        //TODO: that's super ugly
        boolean showHeader = false;
        if (groupItems && !item.isBought()) {
            if (position == 0) {
                showHeader = true;
            } else if (position > 0) {
                Item previousItem = shoppingList.getItem(getItem(position - 1));
                if (item.getGroup() == null && previousItem == null) {
                    showHeader = false;
                } else if (item.getGroup() != null) {
                    showHeader = !item.getGroup().equals(previousItem.getGroup());
                } else if (previousItem.getGroup() != null) {
                    showHeader = !previousItem.getGroup().equals(item.getGroup());
                }
            }
        }

        viewHolder.view_GroupHeader.setVisibility(showHeader ? View.VISIBLE : View.GONE);
        if (showHeader) {
            String text = displayHelper.getDisplayName(item.getGroup());
            viewHolder.textView_GroupHeaderName.setText(text);
        }


        // Specific changes for bought Items
        if(item.isBought()) {
            viewHolder.textView_ShoppingListName.setPaintFlags(viewHolder.textView_ShoppingListName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            viewHolder.textView_ShoppingListName.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Small);

            // Hide details - maybe allow the user to toggle this
            viewHolder.textView_Comment.setVisibility(View.GONE);
            viewHolder.textView_Brand.setVisibility(View.GONE);
            viewHolder.textView_Amount.setVisibility(View.GONE);
            viewHolder.view_GroupHeader.setVisibility(View.GONE);
        }

        return view;
    }

    public void removeByPosition(int position) { // 'remove from this list'
        Item item = shoppingList.getItem(getItem(position));

        if(item.isBought()) {
            item.setBought(false);
        } else {
            item.setBought(true);
        }

        shoppingList.save();
        super.remove(position);
        notifyDataSetChanged();
    }

    public void updateOrderOfList(ShoppingList shoppingList){
        int i = 0;
        for(Integer item : this.getItems()){
            shoppingList.getItem(item).setOrder(i);
            i++;
        }
        new AsyncTaskHelper.ShoppingListSaveTask().execute(shoppingList);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @NonNull
    @Override
    public View getUndoView(int i, View view, ViewGroup viewGroup) {
        View mview = view;
        if (mview == null) {
            mview = LayoutInflater.from(context).inflate(R.layout.shoppinglist_undo_row, viewGroup, false);
        }
        mview.setMinimumHeight(viewGroup.getHeight()); // Match the Item's height
        return mview;
    }

    @NonNull
    @Override
    public View getUndoClickView(@NonNull View view) {
        return view.findViewById(R.id.undo_row_undobutton);
    }

    // Used by drag and drop
    @Override
    public void swapItems(final int positionOne, final int positionTwo) {
        Item i1 = shoppingList.getItem(getItem(positionOne));
        Item i2 = shoppingList.getItem(getItem(positionTwo));
        i1.setOrder(positionTwo);
        i2.setOrder(positionOne);

        new AsyncTaskHelper.ShoppingListSaveTask().execute(shoppingList);

        super.swapItems(positionOne, positionTwo);
    }


    static class ViewHolder {

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }

        @InjectView(R.id.list_row_textView_Main)
        TextView textView_ShoppingListName;

        @InjectView(R.id.list_row_textView_comment)
        TextView textView_Comment;

        @InjectView(R.id.list_row_textView_brand)
        TextView textView_Brand;

        @InjectView(R.id.list_row_textView_amount)
        TextView textView_Amount;

        @InjectView(R.id.group_header)
        View view_GroupHeader;

        @InjectView(R.id.group_header_text)
        TextView textView_GroupHeaderName;
    }

}
