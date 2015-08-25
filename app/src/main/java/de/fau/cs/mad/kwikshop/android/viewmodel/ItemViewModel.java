package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.content.Context;

/**
 * Created by oq22eval on 8/24/15.
 */
public class ItemViewModel {
    private Context context;
    private boolean isVisible = false;
    private de.fau.cs.mad.kwikshop.common.ItemViewModel item;


    public boolean isVisible(){
        return isVisible;
    }

    public void setVisible(boolean isVisible){
        this.isVisible = isVisible;
    }
}
