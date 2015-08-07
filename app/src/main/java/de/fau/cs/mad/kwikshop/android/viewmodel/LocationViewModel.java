package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.LocationFinderHelper;
import de.fau.cs.mad.kwikshop.android.model.SupermarketPlace;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.view.ShoppingListActivity;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import se.walkercrou.places.Place;
import se.walkercrou.places.Status;

public class LocationViewModel {

    private Activity activity;
    private Context context;

    private Boolean canceled = false;

    private final ViewLauncher viewLauncher;
    private final ListManager<ShoppingList> shoppingListManager;
    private final ResourceProvider resourceProvider;

    public static String SHOPPINGMODEPLACEREQUEST_CANCEL = "ShoppingModePlaceRequest_cancel";


    @Inject
    public LocationViewModel(ViewLauncher viewLauncher, ListManager<ShoppingList> shoppingListManager, ResourceProvider resourceProvider){

        if(viewLauncher == null) throw new ArgumentNullException("viewLauncher");
        if(shoppingListManager == null) throw new ArgumentNullException("shoppingListManager");
        if(resourceProvider == null) {throw new ArgumentNullException("resourceProvider");}

        this.resourceProvider = resourceProvider;
        this.viewLauncher = viewLauncher;
        this.shoppingListManager = shoppingListManager;
    }

    public void setContext(Context context){ this.context = context; }

    public void setActivity(Activity activity){ this.activity = activity; }

    public Boolean isCanceld(){ return canceled; }

    public Command<Void> getCancelProgressDialogCommand(){ return cancelProgressDialogCommand; }

    public Command<Void> getFinishActivityCommand(){ return finishCommand; }

    public Command<String> getRouteIntentCommand(){ return routeIntentCommand; }

    public Command<Void> getRestartActivityCommand(){ return restartActivityCommand; }

    public Command<Void> getSavePlaceToShoppingListCommand(){ return savePlaceToShoppingListCommand; }

    public Command<Void> getAcceptDialogCommand() {return acceptDialogCommand;  }

    public Command<Integer> getStartShoppingListFragmemtWithoutPlaceRequestCommand(){ return startShoppingListFragmemtWithoutPlaceRequestCommand; }

    final Command cancelProgressDialogCommand =  new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            canceled = true;
            viewLauncher.showListOfShoppingListsActivity();
        }
    };

    final Command finishCommand = new Command<Void>(){
        @Override
        public void execute(Void parameter) {
            viewLauncher.finishActivity();
        }
    };

    final Command routeIntentCommand = new Command<String>(){
        @Override
        public void execute(String targetAddress) {
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr=" + targetAddress));
            viewLauncher.startActivity(intent);
        }
    };

    final Command restartActivityCommand = new Command<Void>(){
        @Override
        public void execute(Void parameter) {
            viewLauncher.startActivity(activity.getIntent());
        }
    };

    final Command savePlaceToShoppingListCommand = new Command<Void>(){
        @Override
        public void execute(Void parameter) {

        }
    };

    final Command acceptDialogCommand = new Command<Void>(){
        @Override
        public void execute(Void parameter) {

        }
    };

    final Command startShoppingListFragmemtWithoutPlaceRequestCommand = new Command<Integer>(){
        @Override
        public void execute(Integer listId) {
            startShoppingListFragmentWithoutPlaceRequest(listId.intValue());
        }
    };

    public void getNearbySupermarketPlaces(Object instance, int radius, int resultCount){
        SupermarketPlace.initiateSupermarketPlaceRequest(context, instance, radius, resultCount);
    }

    public LatLng getLastLatLng(){
        LastLocation lastLocation = LocationFinderHelper.initiateLocationFinderHelper(context).getLastLocation();
        return new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
    }


    public GoogleMap setupGoogleMap(GoogleMap map){

        map.setMyLocationEnabled(true);
        map.moveCamera( CameraUpdateFactory.newLatLngZoom(getLastLatLng(), 15.0f) );
        UiSettings settings = map.getUiSettings();
        settings.setAllGesturesEnabled(true);
        settings.setMapToolbarEnabled(false);

        return map;
    }

    public void showPlacesInGoogleMap(List<Place> places, GoogleMap map){
        if(places == null){
            return;
        }
        for(Place place : places){
            IconGenerator iconFactory = new IconGenerator(activity.getApplicationContext());
            MarkerOptions markerOptions = new MarkerOptions().
                    icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(place.getName()))).
                    position(new LatLng(place.getLatitude(), place.getLongitude())).
                    anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
            map.addMarker(markerOptions);
        }
    }

    public Place findClickedPlace(Marker marker, List<Place> places){
        return LocationFinderHelper.findClickedPlace(marker, places);
    }

    public String findClickedAddress(Place clickedPlace){
        if(clickedPlace != null)
            return LocationFinderHelper.getAddressConverted(new LatLng(clickedPlace.getLatitude(), clickedPlace.getLongitude()), context);
        else
            return null;
    }

    public String converStatus(Status status){
       return LocationFinderHelper.convertStatus(status, context);
    }

    public String getDistanceBetweenLastLocationAndPlace(Place place, LatLng latLng){
        return LocationFinderHelper.getDistanceBetweenLastLocationAndPlace(place, latLng);
    }

    public void notificationOfNoConnection(){

        viewLauncher.showMessageDialog(
                resourceProvider.getString(R.string.alert_dialog_connection_label),
                resourceProvider.getString(R.string.alert_dialog_connection_message),
                resourceProvider.getString(R.string.alert_dialog_connection_try),
                new Command<Void>() {
                    @Override
                    public void execute(Void parameter) {
                        if(viewLauncher.checkInternetConnection())
                            viewLauncher.showLocationActivity();
                        else {
                            notificationOfNoConnection();
                        }
                    }
                },
                resourceProvider.getString(R.string.alert_dialog_connection_cancel),
                getFinishActivityCommand()
        );

    }

    public CharSequence[] getNamesFromPlaces(List<Place> places){
        return LocationFinderHelper.getNamesFromPlaces(places);
    }


    public boolean checkPlaces(List<Place> places){
        return LocationFinderHelper.checkPlaces(places);
    }

    public void startShoppingListFragmentWithoutPlaceRequest(int listId){
        Intent intent = ShoppingListActivity.getIntent(context, listId);
        intent.putExtra(SHOPPINGMODEPLACEREQUEST_CANCEL, true);
        viewLauncher.startActivity(intent);
    }



}
