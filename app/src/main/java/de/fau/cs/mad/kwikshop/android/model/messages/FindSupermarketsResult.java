package de.fau.cs.mad.kwikshop.android.model.messages;

import com.google.android.gms.maps.model.LatLng;

import se.walkercrou.places.Place;

import java.util.List;


public class FindSupermarketsResult {

    private final List<Place> places;
    private final LatLng currentLocation;


    public FindSupermarketsResult(List<Place> places, LatLng currentLocation) {
        this.places = places;
        this.currentLocation = currentLocation;
    }



    public List<Place> getPlaces() {
        return this.places;
    }


    public LatLng getCurrentLocation() {
        return currentLocation;
    }
}
