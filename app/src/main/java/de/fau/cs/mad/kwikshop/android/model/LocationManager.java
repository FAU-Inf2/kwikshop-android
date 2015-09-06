package de.fau.cs.mad.kwikshop.android.model;

import android.os.AsyncTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.common.util.StringHelper;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import se.walkercrou.places.Place;

public class LocationManager {

    private CountDownLatch loadLatch = new CountDownLatch(1);
    private final Map<String, LastLocation> locationsByPlacesId = new HashMap<>();
    private final SimpleStorage<LastLocation> locationStorage;



    @Inject
    public LocationManager(final SimpleStorage<LastLocation> locationStorage) {

        if(locationStorage == null) {
            throw new ArgumentNullException("locationStorage");
        }

        this.locationStorage = locationStorage;
        loadLocationsAsync();
    }



    public LastLocation getLocationForPlace(Place place) {

        try {
            loadLatch.await();
        } catch (InterruptedException e) {
            return null;
        }

        if(place == null) {
            return null;
        }

        String placeId = place.getPlaceId();

        if(StringHelper.isNullOrWhiteSpace(placeId)) {
            throw new IllegalArgumentException("Place with invalid PlaceId encountered");
        }

        if(locationsByPlacesId.containsKey(placeId)) {
            return locationsByPlacesId.get(placeId);
        } else {
            LastLocation location = new LastLocation();

            location.setName(place.getName());
            location.setLatitude(place.getLatitude());
            location.setLongitude(place.getLongitude());
            location.setAddress(place.getAddress());
            location.setPlaceId(placeId);

            locationsByPlacesId.put(placeId, location);
            locationStorage.addItem(location);

            return location;
        }

    }



    private void loadLocationsAsync() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {

                List<LastLocation> locations = locationStorage.getItems();
                for(LastLocation location : locations) {
                    String id = location.getPlaceId();
                    if(id != null) {
                        locationsByPlacesId.put(id, location);
                    }
                }

                loadLatch.countDown();
                return null;
            }
        }.execute();
    }

}
