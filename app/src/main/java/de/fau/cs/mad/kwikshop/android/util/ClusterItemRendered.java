package de.fau.cs.mad.kwikshop.android.util;


import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

public class ClusterItemRendered extends DefaultClusterRenderer<ClusterMapItem> {

    GoogleMap gMap;
    Context context;
    private IconGenerator iconFactory;

    public ClusterItemRendered(Context context, GoogleMap map, ClusterManager<ClusterMapItem> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
        this.gMap = map;
        this.iconFactory = new IconGenerator(context);
    }

    @Override
    protected void onBeforeClusterItemRendered(ClusterMapItem item, MarkerOptions markerOptions) {

        markerOptions.
                icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(item.getName()))).
                position(new LatLng(item.getPosition().latitude,item.getPosition().longitude)).
                anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

        super.onBeforeClusterItemRendered(item, markerOptions);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<ClusterMapItem> cluster) {
        return cluster.getSize() > 3;
    }
}