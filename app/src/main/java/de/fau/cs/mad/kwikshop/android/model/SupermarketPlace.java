package de.fau.cs.mad.kwikshop.android.model;

import android.content.Context;
import android.os.AsyncTask;
import com.google.android.gms.maps.model.LatLng;
import java.util.List;
import de.fau.cs.mad.kwikshop.android.R;
import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Param;
import se.walkercrou.places.Place;
import se.walkercrou.places.Status;

public class SupermarketPlace {

    public interface AsyncPlaceRequestListener{
        void postResult(List<Place> places);
    }

    private Context context;

    public SupermarketPlace(Context context){
        this.context = context;
    }

    public static void initiateSupermarketPlaceRequest(Context context, final Object instance){
       new SupermarketPlace(context).performAsyncPlaceRequest(instance);
    }

    public void performAsyncPlaceRequest(final Object instance){

        final LatLng latlng =  getLastPosition();
        new AsyncTask<Void, Void, List<Place>>(){

            private final String googleBrowserApiKey = context.getResources().getString(R.string.google_browser_api_key);
            public AsyncPlaceRequestListener listener = (AsyncPlaceRequestListener) instance;

            @Override
            protected List<Place> doInBackground(Void... params) {
                GooglePlaces client = new GooglePlaces(googleBrowserApiKey);
                List<Place> places = null;
                try {
                    /* radius: 5000m
                    *  #results: 30
                    *  types: grocery or supermarket
                    */
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
                }
            }
        }.execute();
    }


    public LatLng getLastPosition(){
        LocationFinderHelper lastLocation = new LocationFinderHelper(context);
        return lastLocation.getLatLng();
    }


}
