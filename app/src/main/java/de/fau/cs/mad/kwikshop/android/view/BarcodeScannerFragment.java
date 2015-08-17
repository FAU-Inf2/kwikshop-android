package de.fau.cs.mad.kwikshop.android.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.viewmodel.BarcodeScannerViewModel;
import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class BarcodeScannerFragment extends Fragment{

    BarcodeScannerViewModel viewModel;
    private ZXingScannerView scannerView;

    public static BarcodeScannerFragment newInstance(){
        return new BarcodeScannerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(getActivity()));
        viewModel = objectGraph.get(BarcodeScannerViewModel.class);
        objectGraph.inject(this);

        viewModel.setContext(getActivity());
        viewModel.hideActionBar();

        scannerView = viewModel.getScannerView();

        return scannerView;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        viewModel.onPause();

    }


}
