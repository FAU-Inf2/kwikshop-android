package de.cs.fau.mad.quickshop.android.interfaces;

import android.view.View;

/**
 * Interface for activities offering activity-wide save/cancel buttons
 */
public interface ISaveCancelActivity {


    void setOnSaveClickListener(View.OnClickListener listener);

    void setOnCancelClickListener(View.OnClickListener listener);


}
