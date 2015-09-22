package de.fau.cs.mad.kwikshop.android.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.SupportMapFragment;

import javax.inject.Inject;
import butterknife.ButterKnife;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.viewmodel.LocationViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;

public class LocationFragment  extends FragmentWithViewModel{

    private LocationViewModel viewModel;

    @Inject
    ViewLauncher viewLauncher;

    public static LocationFragment newInstance() {
        return new LocationFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(getActivity()));
        viewModel = objectGraph.get(LocationViewModel.class);
        objectGraph.inject(this);
        viewModel.setContext(getActivity().getApplicationContext());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_location, container, false);
        ButterKnife.inject(this, rootView);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapView);
        viewModel.setMapFragment(mapFragment);
        viewModel.setView(rootView);

        Command<Void> placeRequestCommand = viewModel.getStartPlaceRequestCommand();
        if(placeRequestCommand.getCanExecute())
            placeRequestCommand.execute(null);

        return rootView;
    }


    void dismissDialog(){
        viewLauncher.dismissDialog();}

    @Override
    public void onDestroy() {
        super.onDestroy();
        dismissDialog();
    }

    @Override
    public void onPause() {
        super.onPause();
        dismissDialog();
    }


}

