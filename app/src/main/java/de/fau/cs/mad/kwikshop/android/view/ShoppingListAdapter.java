package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;

import java.util.Iterator;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.viewmodel.ItemViewModel;
import de.fau.cs.mad.kwikshop.common.util.StringHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.ShoppingListViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;
import de.fau.cs.mad.kwikshop.common.Item;


public class ShoppingListAdapter extends com.nhaarman.listviewanimations.ArrayAdapter<ItemViewModel> implements UndoAdapter , ObservableArrayList.Listener<ItemViewModel> {

    private final Context context;
    private final ShoppingListViewModel shoppingListViewModel;
    private final ObservableArrayList<ItemViewModel, Integer> items;
    private final DisplayHelper displayHelper;
    private boolean multipleSelectionIsChecked = false;

    /**
     * Initializes a new instance of ShoppingListAdapter
     *
     */
    public ShoppingListAdapter(Context context, ShoppingListViewModel shoppingListViewModel, ObservableArrayList<ItemViewModel, Integer> items,
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

        shoppingListViewModel.getItems().addListener(this);
    }


    @Override
    public long getItemId(int position) {
        return items.get(position).getItem().getId();
    }


    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.fragment_shoppinglist_row, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }


        final Item item = items.get(position).getItem();
        final ItemViewModel itemViewModel = items.get(position);

        boolean showDivider = false;
        boolean showDividerBelow = false;
        Item before = null;
        Item next = null;
        if(position > 0)
            before = items.get(position - 1).getItem();
        if((position+1) < items.size())
            next = items.get(position +1).getItem();
        // Determine if we need to show the divider. We also need 'before2' if the user drags
        // an Item into the cart to make sure that only one divider is displayed.
        if(before != null) {
            Item before2 = null;
            if(position > 1)
                before2 = items.get(position - 2).getItem();

            if(before2 != null) {
                if (!before2.isBought() && !before.isBought() && item.isBought())
                    showDivider = true;
            } else
                if (!before.isBought() && item.isBought())
                    showDivider = true;
                else
                    showDivider = false;
        } else if(item.isBought())
            showDivider = true;
        if(next == null && !item.isBought())
            showDividerBelow = true;

        if(showDivider) {
            viewHolder.tableRow_divider_table.setVisibility(View.VISIBLE);
            viewHolder.textView_CartCounter.setText(String.valueOf(shoppingListViewModel.getBoughtItemsCount()));

            // Special case: single item -> singular form of "Items"
            if(shoppingListViewModel.getBoughtItemsCount() == 1)
                viewHolder.textView_Items.setText(R.string.item);
            else
                viewHolder.textView_Items.setText(R.string.items);
        } else {
            viewHolder.tableRow_divider_table.setVisibility(View.GONE);
        }

        if(showDividerBelow) {
            viewHolder.tableRow_divider_tableBelow.setVisibility(View.VISIBLE);
            viewHolder.textView_CartCounterBelow.setText(String.valueOf(shoppingListViewModel.getBoughtItemsCount()));

            // Special case: single item -> singular form of "Items"
            if(shoppingListViewModel.getBoughtItemsCount() == 1)
                viewHolder.textView_Items.setText(R.string.item);
            else
                viewHolder.textView_Items.setText(R.string.items);
        } else {
            viewHolder.tableRow_divider_tableBelow.setVisibility(View.GONE);
        }

        // Item name
        viewHolder.textView_Name.setText(item.getName());

        // Comment
        String comment = item.getComment();
        if (StringHelper.isNullOrWhiteSpace(comment)) {
            viewHolder.textView_Comment.setVisibility(View.GONE);
        } else {
            viewHolder.textView_Comment.setVisibility(View.VISIBLE);
            viewHolder.textView_Comment.setText(comment);
        }

        // Brand
        String brand = item.getBrand();
        if (StringHelper.isNullOrWhiteSpace(brand)) {
            viewHolder.textView_Brand.setVisibility(View.GONE);
        } else {
            viewHolder.textView_Brand.setVisibility(View.VISIBLE);
            viewHolder.textView_Brand.setText(brand);
        }

        // Amount
        double amount = item.getAmount();
        boolean unitIsPieces = false;
        if (item.getUnit() != null) {
            if(item.getUnit().getName() != null) {
                if (item.getUnit().getName().equals(context.getString(R.string.unit_piece_name))) {
                    unitIsPieces = true;
                }
            }
        } else {
            unitIsPieces = true;
        }

        if (amount == 1 && unitIsPieces) {
            viewHolder.textView_Amount.setVisibility(View.GONE);
        } else {
            String unitStr = displayHelper.getShortDisplayName(item.getUnit(), amount);

            viewHolder.textView_Amount.setVisibility(View.VISIBLE);
            //This is not the best way to format fractions, but there are only few of them
            if(amount < 1){
                if (amount == 0.25)
                    viewHolder.textView_Amount.setText(String.format("1/4 %s", unitStr));
                if (amount == 0.5)
                    viewHolder.textView_Amount.setText(String.format("1/2 %s", unitStr));
                if (amount == 0.75)
                    viewHolder.textView_Amount.setText(String.format("3/4 %s", unitStr));
            }
            else
                viewHolder.textView_Amount.setText(String.format("%.0f %s", amount, unitStr));
        }

        // Group header
        // determine if we have to show the group header (above the item)
        // TODO: that's super ugly
        boolean showHeader = false;
        if (shoppingListViewModel.getItemSortType() == ItemSortType.GROUP && !item.isBought()) {
            if (position == 0) {
                showHeader = true;
            } else if (position > 0) {
                Item previousItem = items.get(position - 1).getItem();
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
        //TODO
        if (multipleSelectionIsChecked) {
            viewHolder.checkBox_move.setVisibility(View.VISIBLE);
            viewHolder.button_moveDown.setVisibility(View.VISIBLE);
            viewHolder.button_moveUp.setVisibility(View.VISIBLE);
            viewHolder.button_moveDownBelow.setVisibility(View.VISIBLE);
            viewHolder.button_moveUpBelow.setVisibility(View.VISIBLE);
        }
        else {
            viewHolder.checkBox_move.setVisibility(View.GONE);
            viewHolder.button_moveDown.setVisibility(View.GONE);
            viewHolder.button_moveUp.setVisibility(View.GONE);
            viewHolder.button_moveDownBelow.setVisibility(View.GONE);
            viewHolder.button_moveUpBelow.setVisibility(View.GONE);
        }
        // Specific changes for bought Items
        if (item.isBought()) {
            viewHolder.textView_Name.setPaintFlags(viewHolder.textView_Name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            viewHolder.textView_Name.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Small);
            viewHolder.checkBox_move.setChecked(false);
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
                        itemId = items.get(position).getItem().getId();
                    } catch (IndexOutOfBoundsException e) {
                        return;
                    }

                    shoppingListViewModel.deleteItem(itemId,context.getString(R.string.title_delete_item), context.getString(R.string.message_delete_item),
                                    context.getString(R.string.yes), context.getString(R.string.no),
                                    context.getString(R.string.dont_show_this_message_again));

                    Command<Integer> deleteCommand = shoppingListViewModel.getDeleteItemCommand();

                }
            });
        } else {
            // Remove delete button
            viewHolder.imageView_delete.setVisibility(View.GONE);
            viewHolder.imageView_delete.setOnClickListener(null);
            viewHolder.checkBox_move.setChecked(false);

            // Remove strikethrough, reset text appearance
            viewHolder.textView_Name.setPaintFlags(viewHolder.textView_Name.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            viewHolder.textView_Name.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        }


        viewHolder.checkBox_move.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    shoppingListViewModel.getCheckedItems().add(itemViewModel);
                else
                    shoppingListViewModel.getCheckedItems().remove(itemViewModel);
            }
        });

        viewHolder.button_moveDown.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Iterator<ItemViewModel> itr = items.iterator();
                    while (itr.hasNext()){
                        ItemViewModel itemLocal = itr.next();
                        if (shoppingListViewModel.getCheckedItems().contains(itemLocal)) {
                            //Do something
                            itemLocal.getItem().setBought(true);
                        }
                    }
                    shoppingListViewModel.moveBoughtItemsToEnd();
                }
            });
        viewHolder.button_moveUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Iterator<ItemViewModel> itr = items.iterator();
                while (itr.hasNext()){
                    ItemViewModel itemLocal2 = itr.next();
                    if (shoppingListViewModel.getCheckedItems().contains(itemLocal2)) {
                        itemLocal2.getItem().setBought(false);

                    }
                }
                shoppingListViewModel.moveBoughtItemsToEnd();
            }
        });
        viewHolder.checkBox_multipleSelection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //TODO
                    Iterator <ItemViewModel> itr = shoppingListViewModel.getItems().iterator();
                    while(itr.hasNext()){
                        ItemViewModel itemvm = itr.next();
                        if (isChecked) {
                            itemvm.setVisible(true);
                            multipleSelectionIsChecked = true;
                        }
                        else {
                            itemvm.setVisible(false);
                            multipleSelectionIsChecked = false;
                        }
                    }

                    shoppingListViewModel.changeCheckBoxesVisibility();
            }
        });
        viewHolder.button_moveDownBelow.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Iterator<ItemViewModel> itr = items.iterator();
                while (itr.hasNext()){
                    ItemViewModel itemLocal = itr.next();
                    if (shoppingListViewModel.getCheckedItems().contains(itemLocal)) {
                        //Do something
                        itemLocal.getItem().setBought(true);
                    }
                }
                shoppingListViewModel.moveBoughtItemsToEnd();
            }
        });
        viewHolder.button_moveUpBelow.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Iterator<ItemViewModel> itr = items.iterator();
                while (itr.hasNext()){
                    ItemViewModel itemLocal2 = itr.next();
                    if (shoppingListViewModel.getCheckedItems().contains(itemLocal2)) {
                        itemLocal2.getItem().setBought(false);

                    }
                }
                shoppingListViewModel.moveBoughtItemsToEnd();
            }
        });
        viewHolder.checkBox_multipleSelectionBelow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //TODO
                Iterator <ItemViewModel> itr = shoppingListViewModel.getItems().iterator();
                while(itr.hasNext()){
                    ItemViewModel itemvm = itr.next();
                    if (isChecked) {
                        itemvm.setVisible(true);
                        multipleSelectionIsChecked = true;
                    }
                    else {
                        itemvm.setVisible(false);
                        multipleSelectionIsChecked = false;
                    }
                }

                shoppingListViewModel.changeCheckBoxesVisibility();
            }
        });
        // If item is highlighted, set color to red
        if (item.isHighlight()) {
            viewHolder.textView_Name.setTextColor(Color.RED);
        } else {
            viewHolder.textView_Name.setTextColor(context.getResources().getColor(R.color.primary_text));
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

    @Override
    public void onItemAdded(ItemViewModel newItem) {
        notifyDataSetChanged();
    }

    @Override
    public void onItemRemoved(ItemViewModel removedItem) {
        notifyDataSetChanged();
    }

    @Override
    public void onItemModified(ItemViewModel modifiedItem) {
        notifyDataSetChanged();
    }


    static class ViewHolder {

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }

        @InjectView(R.id.divider_table)
        TableRow tableRow_divider_table;

        @InjectView(R.id.textView_cartCounter)
        TextView textView_CartCounter;

        @InjectView(R.id.divider_tableBelow)
        TableRow tableRow_divider_tableBelow;

        @InjectView(R.id.textView_cartCounterBelow)
        TextView textView_CartCounterBelow;

        @InjectView(R.id.textView_items)
        TextView textView_Items;

        @InjectView(R.id.list_row_textView_Main)
        TextView textView_Name;

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

        @InjectView(R.id.button_moveUp)
        Button button_moveUp;

        @InjectView(R.id.button_moveDown)
        Button button_moveDown;

        @InjectView(R.id.checkBox_multipleSelection)
        CheckBox checkBox_multipleSelection;

        @InjectView(R.id.button_moveUpBelow)
        Button button_moveUpBelow;

        @InjectView(R.id.button_moveDownBelow)
        Button button_moveDownBelow;

        @InjectView(R.id.checkBox_multipleSelectionBelow)
        CheckBox checkBox_multipleSelectionBelow;

        @InjectView(R.id.checkBox_move)
        CheckBox checkBox_move;



    }

}
