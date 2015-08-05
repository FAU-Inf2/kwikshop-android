package de.fau.cs.mad.kwikshop.android.model;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import java.util.List;
import de.fau.cs.mad.kwikshop.android.R;
import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Param;
import se.walkercrou.places.Place;

public class SupermarketPlace {

    public interface AsyncPlaceRequestListener{
        void postResult(List<Place> places);
    }

    private Context context;


    public SupermarketPlace(Activity activity){
        this.context = activity.getApplicationContext();
    }

    public static SupermarketPlace initiateSupermarketPlace(Activity activity){
        return new SupermarketPlace(activity);
    }

    public void performAsyncPlaceRequest(){

        final LatLng latlng =  getLastPosition();
        new AsyncTask<Void, Void, List<Place>>(){

            private final String googleBrowserApiKey = context.getResources().getString(R.string.google_browser_api_key);
            public AsyncPlaceRequestListener listener = null;
            @Override
            protected List<Place> doInBackground(Void... params) {
                GooglePlaces client = new GooglePlaces(googleBrowserApiKey);
                List<Place> places = null;
                try {
                    places = client.getNearbyPlaces(latlng.latitude, latlng.longitude, 5000, 30, Param.name("types").value("grocery_or_supermarket"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return places;
            }

            @Override
            protected void onPostExecute(List<Place> places) {
                super.onPostExecute(places);
                if(listener != null){
                    listener.postResult(places);
                } else {
                    if(places == null){
                        Log.e("TEST", "Place is null 1");
                    }
                }
            }
        }.execute();
    }


    public LatLng getLastPosition(){
        LocationFinderHelper lastLocation = new LocationFinderHelper(context);
        return lastLocation.getLatLng();
    }





}
