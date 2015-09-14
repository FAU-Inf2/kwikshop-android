package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.viewmodel.ItemViewModel;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.util.StringHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.RecipeViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;

public class RecipeAdapter extends com.nhaarman.listviewanimations.ArrayAdapter<ItemViewModel> implements ObservableArrayList.Listener<ItemViewModel> {

    private final Context context;
    private final RecipeViewModel recipeViewModel;
    private final ObservableArrayList<ItemViewModel, Integer> items;
    private final DisplayHelper displayHelper;

    /**
     * Initializes a new instance of RecipeAdapter
     *
     */
    public RecipeAdapter(Context context, RecipeViewModel recipeViewModel, ObservableArrayList<ItemViewModel, Integer> items,
                               DisplayHelper displayHelper) {

        super(items);

        if(context == null) {
            throw new IllegalArgumentException("'context' must not be null");
        }

        if (recipeViewModel == null) {
            throw new IllegalArgumentException("'recipeViewModel' must not be null");
        }

        if (items == null) {
            throw new IllegalArgumentException("'items' must not be null");
        }

        if(displayHelper == null) {
            throw new IllegalArgumentException("'displayHelper' must not be null");
        }

        this.context = context;
        this.recipeViewModel = recipeViewModel;
        this.items = items;
        this.displayHelper = displayHelper;

        items.addListener(this);
    }


    @Override
    public long getItemId(int position) {
        return items.get(position).getItem().getId();
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

        viewHolder.divider_layout.setVisibility(View.GONE);
        viewHolder.divider_layout_below.setVisibility(View.GONE);

        Item item = items.get(position).getItem();

        if (!item.isBought()) {
            viewHolder.textView_RecipeName.setPaintFlags(viewHolder.textView_RecipeName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            viewHolder.textView_RecipeName.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        }

        //if item is highlighted, set color to red
        if (item.isHighlight()) {
            viewHolder.textView_RecipeName.setTextColor(Color.RED);
        } else {
            viewHolder.textView_RecipeName.setTextColor(context.getResources().getColor(R.color.primary_text));
        }

        // Fill Views

        // Item name
        viewHolder.textView_RecipeName.setText(item.getName());

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

        boolean unitIsPieces = false;
        if (item.getUnit() != null) {
            if (item.getUnit().getName().equals(context.getString(R.string.unit_piece_name))) {
                unitIsPieces = true;
            }
        } else {
            unitIsPieces = true;
        }
        // amount
        double amount = item.getAmount();
        if (amount == 1 && unitIsPieces) {
            viewHolder.textView_Amount.setVisibility(View.GONE);
        } else {
            String unitStr = displayHelper.getShortDisplayName(item.getUnit());

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

        viewHolder.view_GroupHeader.setVisibility(View.GONE);
        viewHolder.checkBox_move.setVisibility(View.GONE);
       return view;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }



//    // Used by drag and drop
//    @Override
//    public void swapItems(final int positionOne, final int positionTwo) {
//
//        items.disableEvents();
//
//        shoppingListViewModel.itemsSwapped(positionOne, positionTwo);
//
//        super.swapItems(positionOne, positionTwo);
//
//        items.enableEvents();
//    }

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

        @InjectView(R.id.divider_layout)
        RelativeLayout divider_layout;

        @InjectView(R.id.divider_layout_below)
        RelativeLayout divider_layout_below;

        @InjectView(R.id.list_row_textView_Main)
        TextView textView_RecipeName;

        @InjectView(R.id.list_row_textView_comment)
        TextView textView_Comment;

        @InjectView(R.id.list_row_textView_brand)
        TextView textView_Brand;

        @InjectView(R.id.list_row_textView_amount)
        TextView textView_Amount;

        @InjectView(R.id.group_header)
        View view_GroupHeader;

        @InjectView(R.id.checkBox_edit)
        CheckBox checkBox_move;

    }

}


