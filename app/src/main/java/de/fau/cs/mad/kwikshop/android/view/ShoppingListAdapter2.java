package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.util.StringHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.ShoppingListViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;


/**
 * TODO: This name is temporary. This adapter is intended to replace existing ShoppingListAdapter once it's finished
 */
public class ShoppingListAdapter2 extends com.nhaarman.listviewanimations.ArrayAdapter<Item> implements UndoAdapter {

    private final Context context;
    private final ShoppingListViewModel shoppingListViewModel;
    private final ObservableArrayList<Item, Integer> items;
    private final DisplayHelper displayHelper;

    /**
     * Initializes a new instance of ShoppingListAdapter
     *
     */
    public ShoppingListAdapter2(Context context, ShoppingListViewModel shoppingListViewModel, ObservableArrayList<Item, Integer> items,
                                DisplayHelper displayHelper) {

        super(items);

        if(context == null) {
            throw new IllegalArgumentException("'context' must not be null");
        }

        if (shoppingListViewModel == null) {
            throw new IllegalArgumentException("'shoppingListViewModel' must not be null");
        }

        if (items == null) {
            throw new IllegalArgumentException("'items' must not be null");
        }

        if(displayHelper == null) {
            throw new IllegalArgumentException("'displayHelper' must not be null");
        }

        this.context = context;
        this.shoppingListViewModel = shoppingListViewModel;
        this.items = items;
        this.displayHelper = displayHelper;
    }


    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }


    @Override
    public View getView(final int position, View view, ViewGroup parent) {

        ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.fragment_shoppinglist_row, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Item item = items.get(position);

        if (!item.isBought()) {
            viewHolder.textView_ShoppingListName.setPaintFlags(viewHolder.textView_ShoppingListName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            viewHolder.textView_ShoppingListName.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        }

        //if item is highlighted, set color to red
        if (item.isHighlight()) {
            viewHolder.textView_ShoppingListName.setTextColor(Color.RED);
        } else {
            viewHolder.textView_ShoppingListName.setTextColor(context.getResources().getColor(R.color.primary_text));
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
        if (shoppingListViewModel.getItemSortType() == ItemSortType.GROUP && !item.isBought()) {
            if (position == 0) {
                showHeader = true;
            } else if (position > 0) {
                Item previousItem = items.get(position - 1);
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
        if (item.isBought()) {
            viewHolder.textView_ShoppingListName.setPaintFlags(viewHolder.textView_ShoppingListName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            viewHolder.textView_ShoppingListName.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Small);

            // Hide details - maybe allow the user to toggle this
            viewHolder.textView_Comment.setVisibility(View.GONE);
            viewHolder.textView_Brand.setVisibility(View.GONE);
            viewHolder.textView_Amount.setVisibility(View.GONE);
            viewHolder.view_GroupHeader.setVisibility(View.GONE);

            viewHolder.imageView_delete.setVisibility(View.VISIBLE);
            viewHolder.imageView_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Integer itemId;
                    try {
                        itemId = items.get(position).getId();
                    } catch (IndexOutOfBoundsException e) {
                        return;
                    }
                    Command<Integer> deleteCommand = shoppingListViewModel.getDeleteItemCommand();
                    if(deleteCommand.getCanExecute()) {
                        deleteCommand.execute(itemId);
                    }
                }
            });
        }

        return view;
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

        Item i1 = items.get(positionOne);
        Item i2 = items.get(positionTwo);

        shoppingListViewModel.swapItems(i1, i2);

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

        @InjectView(R.id.list_row_imageView_delete)
        ImageView imageView_delete;
    }

}
