package de.cs.fau.mad.quickshop.android;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cs.fau.mad.quickshop_android.R;

/**
 * Created by Robert on 01.05.2015.
 */
public class NavigationDrawerAdapter extends ArrayAdapter<String> {

    private ArrayList<String> mList;
    private int res;
    private Activity activity;
    private int[] icons = {R.drawable.ic_list, R.drawable.ic_list, R.drawable.ic_list};


    public NavigationDrawerAdapter(Activity act, int resource, ArrayList<String> objects) {
        super(act, resource, objects);
            activity = act;
            res = resource;
            mList = objects;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        ViewHolder holder;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(res, null);

            holder = new ViewHolder();
            holder.tvItemName = (TextView) view.findViewById(R.id.tvNavItemName);
            holder.ivItemIcon = (ImageView) view.findViewById(R.id.ivNavItemIcon);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.tvItemName.setText(mList.get(position));
        holder.ivItemIcon.setImageResource(icons[position]);

        return view;

    }

    public class ViewHolder {
        public TextView tvItemName;
        public ImageView ivItemIcon;
    }
}
