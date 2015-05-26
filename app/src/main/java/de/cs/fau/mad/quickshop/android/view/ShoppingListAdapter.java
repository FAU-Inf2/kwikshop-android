package de.cs.fau.mad.quickshop.android.view;

import android.content.Context;
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

/**
 * Created by Nicolas on 15/05/2015.
 */
public class ShoppingListAdapter extends ArrayAdapter<Integer> implements UndoAdapter, Swappable{

    ShoppingList shoppingList;

    public ShoppingListAdapter(Context context, int textViewResourceId,
                              List<Integer> objects, ShoppingList shoppingList) {
        super(context, textViewResourceId, objects);
        this.shoppingList = shoppingList;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent){
        if(view == null ){
            view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_shoppinglist_row, parent, false);
        }
        //display items in the list
        TextView shoppingListNameView = (TextView) view.findViewById(R.id.tvItem);

        shoppingListNameView.setText(shoppingList.getItem(getItem(position)).getName());

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
        super.remove(item.getId());
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
            mview = LayoutInflater.from(getContext()).inflate(R.layout.shoppinglist_undo_row, viewGroup, false);
        }
        return mview;
    }

    @NonNull
    @Override
    public View getUndoClickView(@NonNull View view) {
        return view.findViewById(R.id.undo_row_undobutton);
    }

    @Override
    public void swapItems(int i, int i1) {

    }
}
