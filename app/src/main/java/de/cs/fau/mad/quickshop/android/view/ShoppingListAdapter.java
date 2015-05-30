package de.cs.fau.mad.quickshop.android.view;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;
import com.nhaarman.listviewanimations.util.Swappable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cs.fau.mad.quickshop_android.R;
import de.cs.fau.mad.quickshop.android.common.Item;
import de.cs.fau.mad.quickshop.android.common.ShoppingList;
import de.cs.fau.mad.quickshop.android.common.Unit;
import de.cs.fau.mad.quickshop.android.util.StringHelper;

public class ShoppingListAdapter extends com.nhaarman.listviewanimations.ArrayAdapter<Integer> implements UndoAdapter{

    ShoppingList shoppingList;
    Context context;

    public ShoppingListAdapter(Context context, int textViewResourceId,
                              List<Integer> objects, ShoppingList shoppingList) {
        super(objects);
        this.shoppingList = shoppingList;
        this.context = context;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent){

        if(view == null ){
            view = LayoutInflater.from(context).inflate(R.layout.fragment_shoppinglist_row, parent, false);
        }

        Item item = shoppingList.getItem(getItem(position));

        // Fill TextViews
        TextView shoppingListNameView = (TextView) view.findViewById(R.id.list_row_textView_Main);
        shoppingListNameView.setText(item.getName());

        String comment = item.getComment();
        TextView commentView = (TextView) view.findViewById(R.id.list_row_textView_comment);
        if (StringHelper.isNullOrWhiteSpace(comment)) {
            commentView.setVisibility(View.GONE);
        } else {
            commentView.setVisibility(View.VISIBLE);
            commentView.setText(comment);
        }

        String brand = item.getBrand();
        TextView brandView = (TextView) view.findViewById(R.id.list_row_textView_brand);
        if (StringHelper.isNullOrWhiteSpace(brand)) {
            brandView.setVisibility(View.GONE);
        } else {
            brandView.setVisibility(View.VISIBLE);
            brandView.setText(brand);
        }


        int amount = item.getAmount();
        TextView amountView = (TextView) view.findViewById(R.id.list_row_textView_amount);
        if (amount <= 1) {
            amountView.setVisibility(View.GONE);

        } else {
            Unit unit = item.getUnit();
            String unitStr = unit != null ? unit.getName() : "";

            amountView.setVisibility(View.VISIBLE);
            amountView.setText(String.format("%d %s", amount, unitStr));
        }

        // Specific changes for bought Items
        if(item.isBought()) {
            shoppingListNameView.setPaintFlags(shoppingListNameView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            shoppingListNameView.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Small);

            // Hide details - maybe allow the user to toggle this
            commentView.setVisibility(View.GONE);
            brandView.setVisibility(View.GONE);
            amountView.setVisibility(View.GONE);
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

        shoppingList.updateItem(item);
        shoppingList.save();
        super.remove(position);
        notifyDataSetChanged();
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

}
