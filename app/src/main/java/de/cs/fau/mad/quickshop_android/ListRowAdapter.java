package de.cs.fau.mad.quickshop_android;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import cs.fau.mad.quickshop_android.R;

/**
 * Created by Robert on 01.05.2015.
 */
public class ListRowAdapter extends ArrayAdapter<String> {

    private ArrayList<String> mList;
    private int row;
    private Activity activity;

    public ListRowAdapter(Activity act, int resource, ArrayList<String> objects) {
        super(act, resource, objects);
            activity = act;
            row = resource;
            mList = objects;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        ViewHolder holder;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(row, null);

            holder = new ViewHolder();
            holder.tvItem = (TextView) view.findViewById(R.id.tvItem);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.tvItem.setText(mList.get(position));

        return view;

    }

    public class ViewHolder {
        public TextView tvItem;
    }
}
