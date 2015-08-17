package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;
import com.google.zxing.Result;
import javax.inject.Inject;
import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class BarcodeScannerViewModel implements ZXingScannerView.ResultHandler  {

    private Context context;
    //private final ViewLauncher viewLauncher;
    //private final ResourceProvider resourceProvider;
    private ZXingScannerView scannerView;


    @Inject
    public BarcodeScannerViewModel(){
        /*
        if(resourceProvider == null) {throw new ArgumentNullException("resourceProvider");}

        this.resourceProvider = resourceProvider;
        this.viewLauncher = viewLauncher;
        */
    }

    public ZXingScannerView getScannerView(){
        scannerView = new ZXingScannerView(context);
        return scannerView ;
    }

    public void setContext(Context context){
        this.context = context;
    }

    public void hideActionBar(){
        ((ActionBarActivity)context).getSupportActionBar().hide();
    }

    public void onPause(){
        scannerView.stopCamera();
    }

    public void onResume(){
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }


    @Override
    public void handleResult(Result result) {
        Toast.makeText(context, "Contents = " + result.getText() + ", Format = " + result.getBarcodeFormat().toString(), Toast.LENGTH_LONG).show();
        scannerView.startCamera();
    }



}
