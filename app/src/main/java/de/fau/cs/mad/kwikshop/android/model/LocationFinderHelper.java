package de.fau.cs.mad.kwikshop.android.model;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;


import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.android.view.LocationActivity;
import se.walkercrou.places.Place;
import se.walkercrou.places.Status;

public class LocationFinderHelper implements LocationListener {

    private static final String TAG = LocationActivity.class.getSimpleName();
    private LocationManager locationManager;
    Context context;

    // coordinates of location
    private double latitude;
    private double longitude;

    // accuracy of location
    private double accuracy;

    // settings
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;

    // The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 30; // 30 sec
    private Location  location;
    private Location  gpsLocation;
    private Location  networkLocation;


    @Inject
    public LocationFinderHelper(Context context){

        if (context == null) {
            throw new IllegalArgumentException("'context' must not be null");
        }
        this.context = context;

        getLocation();
    }


    public static LocationFinderHelper initiateLocationFinderHelper(Context context){
        return new LocationFinderHelper(context);
    }

    public LastLocation getLastLocation(){
        LastLocation lastLocation = new LastLocation();
        lastLocation.setLatitude(latitude);
        lastLocation.setLongitude(longitude);
        lastLocation.setAddress(getAddressToString());
        lastLocation.setAccuracy(accuracy);
        return lastLocation;
    }

    public Location getLocation() {

        double networkLat = 0, networkLong = 0, networkAcc = Double.MAX_VALUE, gpsLat = 0, gpsLong = 0, gpsAcc = Double.MAX_VALUE;

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        // network
        if (isNetworkEnabled) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            if (locationManager != null) {
                // get last position
                networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (networkLocation != null) {
                    networkAcc = networkLocation.getAccuracy();
                    networkLat = networkLocation.getLatitude();
                    networkLong = networkLocation.getLongitude();
                }
            }
        }

        // gps
        if (isGPSEnabled) {
            if (gpsLocation == null) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (locationManager != null) {
                    // get last position
                    gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (gpsLocation != null) {
                        gpsAcc = gpsLocation.getAccuracy();
                        gpsLat = gpsLocation.getLatitude();
                        gpsLong = gpsLocation.getLongitude();
                    }
                }
            }
        }

        if(networkAcc < gpsAcc){
            accuracy = networkAcc;
            latitude = networkLat;
            longitude = networkLong;
            location = networkLocation;

        } else if(networkAcc > gpsAcc){
            accuracy = gpsAcc;
            latitude = gpsLat;
            longitude = gpsLong;
            location = gpsLocation;
        }

        return location;
    }


    public double getLatitude(){

       return location != null ? latitude : 0.0;
    }

    public double getLongitude(){

        return location != null ? longitude  : 0.0;
    }

    public LatLng getLatLng(){
        return new LatLng(getLatitude(),getLongitude());
    }

    public String getAddressToString() {

        try {
            String address = "";
            int maxLines = getAddress().getMaxAddressLineIndex();
            for (int i = 0; i <= maxLines; i++) {
                address = address + getAddress().getAddressLine(i) + " ";
            }
            return address;
        } catch (NullPointerException e){
            // nothing to do
        }

        return null;
    }

    public Address getAddress(){
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(),  location.getLongitude(), 1);
            if (addresses.size() > 0){
                return addresses.get(0);
            }
        } catch (IOException e) {
         // nothing to do
        }
        return null;
    }


    public static String getAddressConverted(LatLng coord, Context context){
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(coord.latitude,  coord.longitude, 1);
            if (addresses.size() > 0){
                String address = "";
                int maxLines =  addresses.get(0).getMaxAddressLineIndex();
                for(int i = 0; i <= maxLines; i++){
                    address = address + addresses.get(0).getAddressLine(i) + " ";
                }
                return address;
            }
        } catch (IOException e) {
            // nothing to do
        }
        return "";
    }


    @Override
    public void onLocationChanged(Location location) {
        if(isBetterLocation(location, this.location))
            this.location = location;
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        getLocation();
    }

    @Override
    public void onProviderDisabled(String provider) {
        getLocation();
    }

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    public static Place findClickedPlace(Marker marker, List<Place> places){
        for(Place place : places){
            if(place.getLatitude() - marker.getPosition().latitude == 0.0 && place.getLongitude() - marker.getPosition().longitude == 0.0){
                return place;
            }
        }
        return null;
    }

    public static String convertStatus(Status status, Context context){
        if(status.toString().equals(Status.OPENED.toString())){
            return context.getResources().getString(R.string.place_status_opened);
        } else if(status.toString().equals(Status.CLOSED.toString())){
            return context.getResources().getString(R.string.place_status_closed);
        } else
            return "";
    }

    public static String getDistanceBetweenLastLocationAndPlace(Place place, LatLng latLng){
        Location shopLocation = new Location("place");
        shopLocation.setLatitude(place.getLatitude());
        shopLocation.setLongitude(place.getLongitude());

        Location lastLocation = new Location("current");
        lastLocation.setLatitude(latLng.latitude);
        lastLocation.setLongitude(latLng.longitude);

        return distanceConverter(lastLocation.distanceTo(shopLocation));
    }

    private static String distanceConverter(double distance){
        if(distance >= 1000){
            return Math.round((distance / 1000) * 10.0) / 10.0 + " km";
        } else
            return Math.round(distance * 10.0) / 10.0 + " m";
    }

    public static CharSequence[] getNamesFromPlaces(List<Place> places, Context context){
        CharSequence[] placeNames = new CharSequence[places.size()];
        int i = 0;
        for(Place place : places){
            placeNames[i] = place.getName() + " " + LocationFinderHelper.getAddressConverted(new LatLng(place.getLatitude(), place.getLongitude()), context);
            i++;
        }
        return placeNames;
    }

    public static boolean checkPlaces(List<Place> places){
        if(places == null)
            return false;

        if(places.size() == 0)
            return false;

        return true;
    }

}
