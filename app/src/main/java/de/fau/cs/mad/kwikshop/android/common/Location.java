package de.fau.cs.mad.kwikshop.android.common;

import android.location.Address;

import com.google.android.gms.maps.model.LatLng;

public class Location {

    private LatLng coordinates;

    private Address address;

    private boolean visited;


    public LatLng getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }
}
