package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.util.StringHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.RecipeViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;

public class RecipeAdapter extends com.nhaarman.listviewanimations.ArrayAdapter<Item> implements ObservableArrayList.Listener<Item> {

    private final Context context;
    private final RecipeViewModel recipeViewModel;
    private final ObservableArrayList<Item, Integer> items;
    private final DisplayHelper displayHelper;

    /**
     * Initializes a new instance of RecipeAdapter
     *
     */
    public RecipeAdapter(Context context, RecipeViewModel recipeViewModel, ObservableArrayList<Item, Integer> items,
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

        // amount
        int amount = item.getAmount();
        if (amount <= 1) {
            viewHolder.textView_Amount.setVisibility(View.GONE);
        } else {
            String unitStr = displayHelper.getShortDisplayName(item.getUnit());

            viewHolder.textView_Amount.setVisibility(View.VISIBLE);
            viewHolder.textView_Amount.setText(String.format("%d %s", amount, unitStr));
        }

        viewHolder.view_GroupHeader.setVisibility(View.GONE);
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
    public void onItemAdded(Item newItem) {
        notifyDataSetChanged();
    }

    @Override
    public void onItemRemoved(Item removedItem) {
        notifyDataSetChanged();
    }

    @Override
    public void onItemModified(Item modifiedItem) {
        notifyDataSetChanged();
    }


    static class ViewHolder {

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }

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

    }

}


