package de.fau.cs.mad.kwikshop.android.view;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.viewmodel.BarcodeScannerViewModel;
import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class BarcodeScannerFragment extends Fragment{

    private static final String ARG_LISTID = "list_id";
    BarcodeScannerViewModel viewModel;

    public static BarcodeScannerFragment newInstance(int listID){
        BarcodeScannerFragment fragment = new BarcodeScannerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LISTID, listID);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ZXingScannerView scannerView = null;
        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(getActivity()));
        viewModel = objectGraph.get(BarcodeScannerViewModel.class);
        objectGraph.inject(this);

        viewModel.setContext(getActivity());
        viewModel.hideActionBar();
        viewModel.setListId(getArguments().getInt(ARG_LISTID));
        scannerView = viewModel.getScannerView();
        return scannerView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        viewModel.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        viewModel.onPause();

    }


}
