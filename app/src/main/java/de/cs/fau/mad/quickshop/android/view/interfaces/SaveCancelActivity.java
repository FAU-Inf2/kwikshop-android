package de.cs.fau.mad.quickshop.android.view.interfaces;

import android.view.View;

/**
 * Interface for activities offering activity-wide save/cancel buttons
 */
public interface SaveCancelActivity {


    void setOnSaveClickListener(View.OnClickListener listener);

    void setOnCancelClickListener(View.OnClickListener listener);


}
