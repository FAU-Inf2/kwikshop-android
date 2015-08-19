package de.fau.cs.mad.kwikshop.android.util;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMapItem implements ClusterItem {
    private final LatLng mPosition;
    private String mName;

    public ClusterMapItem(double lat, double lng, String name) {
        mPosition = new LatLng(lat, lng);
        mName = name;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getName(){
        return mName;
    }

}
