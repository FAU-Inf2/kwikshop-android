package de.fau.cs.mad.kwikshop.android.model;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.messages.FindSupermarketsResult;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.greenrobot.event.EventBus;
import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Param;
import se.walkercrou.places.Place;

import static de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper.*;

public class SupermarketPlace {

    public interface AsyncPlaceRequestListener {
        void postResult(List<Place> places);
    }

    List<Place> places = null;

    private Context context;

    public SupermarketPlace(Context context) {
        this.context = context;
    }

    public static void initiateSupermarketPlaceRequest(Context context, final Object instance, int radius, int resultCount, String query) {
        new SupermarketPlace(context).performAsyncPlaceRequest(instance, radius, resultCount, query);
    }

    public void performAsyncPlaceRequest(final Object instance, final int radius, final int resultCount, final String query) {

        final LatLng latlng = getLastPosition();


        new AsyncTask<Void, Void, List<Place>>() {

            private final String googleBrowserApiKey = context.getResources().getString(R.string.google_browser_api_key);
            public AsyncPlaceRequestListener listener = (AsyncPlaceRequestListener) instance;

            @Override
            protected List<Place> doInBackground(Void... params) {
                GooglePlaces client = new GooglePlaces(googleBrowserApiKey);

                String[] storeTypes = new String[]{"grocery_or_supermarket", "bakery", "gas_station", "liquor_store", "pharmacy", "shopping_mall", "store"};

                // get nearby supermarkets
                if (query == null) {

                    try {
                        places = client.getNearbyPlaces(
                                latlng.latitude,
                                latlng.longitude,
                                radius, resultCount,
                                Param.name("types").value(loadBoolean(STORE_TYPE_SUPERMARKET, true, context) ? storeTypes[0] : ""),
                                Param.name("types").value(loadBoolean(STORE_TYPE_BAKERY, false, context) ? storeTypes[1] : ""),
                                Param.name("types").value(loadBoolean(STORE_TYPE_GAS_STATION, false, context) ? storeTypes[2] : ""),
                                Param.name("types").value(loadBoolean(STORE_TYPE_LIQUOR_STORE, false, context) ? storeTypes[3] : ""),
                                Param.name("types").value(loadBoolean(STORE_TYPE_PHARMACY, false, context) ? storeTypes[4] : ""),
                                Param.name("types").value(loadBoolean(STORE_TYPE_SHOPPING_MALL, false, context) ? storeTypes[5] : ""),
                                Param.name("types").value(loadBoolean(STORE_TYPE_STORE, false, context) ? storeTypes[6] : "")
                        );


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                // get places by query string
                } else {

                    try {
                        places = client.getPlacesByQuery(query);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                return places;
            }

            @Override
            protected void onPostExecute(List<Place> places) {
                super.onPostExecute(places);
                if (listener != null) {
                    listener.postResult(places);
                }
                EventBus.getDefault().post(new FindSupermarketsResult(places, latlng));
            }
        }.execute();
    }

    public LatLng getLastPosition() {
        LocationFinderHelper locationFinderHelper = new LocationFinderHelper(context);
        Location lastLocation = locationFinderHelper.getLocation();
        if (lastLocation == null)
            return null;
        return new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
    }


}
