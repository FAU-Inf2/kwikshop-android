package de.fau.cs.mad.kwikshop.android.model;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.messages.FindSupermarketsResult;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.greenrobot.event.EventBus;
import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Param;
import se.walkercrou.places.Place;
import se.walkercrou.places.Status;

public class SupermarketPlace {

    public interface AsyncPlaceRequestListener {
        void postResult(List<Place> places);
    }

    List<Place> places = null;
    private Context context;

    public SupermarketPlace(Context context) {
        this.context = context;
    }

    public static void initiateSupermarketPlaceRequest(Context context, final Object instance, int radius, int resultCount) {
        new SupermarketPlace(context).performAsyncPlaceRequest(instance, radius, resultCount);
    }

    public void performAsyncPlaceRequest(final Object instance, final int radius, final int resultCount) {

        final LatLng latlng = getLastPosition();
        new AsyncTask<Void, Void, List<Place>>() {

            private final String googleBrowserApiKey = context.getResources().getString(R.string.google_browser_api_key);
            public AsyncPlaceRequestListener listener = (AsyncPlaceRequestListener) instance;

            @Override
            protected List<Place> doInBackground(Void... params) {
                GooglePlaces client = new GooglePlaces(googleBrowserApiKey);

                String[] storeTypes = new String[]{
                        "grocery_or_supermarket",
                        "bakery",
                        "gas_station",
                        "liquor_store",
                        "pharmacy",
                        "shopping_mall",
                        "florist"
                };

                String[] storeTypesSettingName = new String[]{
                        SharedPreferencesHelper.STORE_TYPE_SUPERMARKET,
                        SharedPreferencesHelper.STORE_TYPE_BAKERY,
                        SharedPreferencesHelper.STORE_TYPE_GAS_STATION,
                        SharedPreferencesHelper.STORE_TYPE_LIQUOR_STORE,
                        SharedPreferencesHelper.STORE_TYPE_PHARMACY,
                        SharedPreferencesHelper.STORE_TYPE_SHOPPING_MALL,
                        SharedPreferencesHelper.STORE_TYPE_FLORIST
                };

                try {
                    Log.e("SupermarketPlace", "Request: all");
                    places = client.getNearbyPlaces(
                            latlng.latitude,
                            latlng.longitude,
                            radius, 30,
                            Param.name("types").value("store"),
                            Param.name("types").value(SharedPreferencesHelper.loadBoolean(storeTypesSettingName[0], false, context) ? storeTypes[0] : ""),
                            Param.name("types").value(SharedPreferencesHelper.loadBoolean(storeTypesSettingName[1], false, context) ? storeTypes[1] : ""),
                            Param.name("types").value(SharedPreferencesHelper.loadBoolean(storeTypesSettingName[2], false, context) ? storeTypes[2] : ""),
                            Param.name("types").value(SharedPreferencesHelper.loadBoolean(storeTypesSettingName[3], false, context) ? storeTypes[3] : ""),
                            Param.name("types").value(SharedPreferencesHelper.loadBoolean(storeTypesSettingName[4], false, context) ? storeTypes[4] : ""),
                            Param.name("types").value(SharedPreferencesHelper.loadBoolean(storeTypesSettingName[5], false, context) ? storeTypes[5] : ""),
                            Param.name("types").value(SharedPreferencesHelper.loadBoolean(storeTypesSettingName[6], false, context) ? storeTypes[5] : "")
                    );
                } catch (Exception e) {
                    Log.e("SupermarketPlace", "Error: " + e.getMessage());
                    e.printStackTrace();
                }


                return places;
            }

            @Override
            protected void onPostExecute(List<Place> places) {
                super.onPostExecute(places);
                if (listener != null) {
                    listener.postResult(places);
                }

                EventBus.getDefault().post(new FindSupermarketsResult(places));
            }
        }.execute();
    }

    private List<Place> addPlaces(List<Place> placesToAdd) {
        if (places != null) {
            if (placesToAdd != null) {
                places.addAll(placesToAdd);
            }
        }
        return places;
    }


    public LatLng getLastPosition() {
        LocationFinderHelper lastLocation = new LocationFinderHelper(context);
        return lastLocation.getLatLng();
    }


}
