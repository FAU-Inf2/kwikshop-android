package de.fau.cs.mad.kwikshop.android.model.messages;

import se.walkercrou.places.Place;

import java.util.List;


public class FindSupermarketsResult {

    private final List<Place> places;


    public FindSupermarketsResult(List<Place> places) {
        this.places = places;
    }



    public List<Place> getPlaces() {
        return this.places;
    }

}
