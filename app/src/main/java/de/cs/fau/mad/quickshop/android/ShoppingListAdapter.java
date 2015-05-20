package de.cs.fau.mad.quickshop.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import cs.fau.mad.quickshop_android.R;

/**
 * Created by Nicolas on 15/05/2015.
 */
public class ShoppingListAdapter extends ArrayAdapter<String> {

    HashMap<String, Integer> mIdMap = new HashMap<>();
    ShoppingList shoppingList;

    public ShoppingListAdapter(Context context, int textViewResourceId,
                              List<String> objects, ShoppingList shoppingList) {

        super(context, textViewResourceId, objects);
        this.shoppingList = shoppingList;
    }

    @Override
    public long getItemId(int position) {
        //String item = getItem(position);
        //return mIdMap.get(item);
        return shoppingList.getItem(position).getId();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent){

        if(view == null ){
            view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_shoppinglist_row, parent, false);
        }
        //display items in the list
        TextView shoppingListNameView = (TextView) view.findViewById(R.id.tvItem);

        shoppingListNameView.setText(shoppingList.getItem(position).getName());

        return view;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}
