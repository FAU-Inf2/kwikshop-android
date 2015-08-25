package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.content.Context;

import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.Unit;

/**
 * Created by oq22eval on 8/24/15.
 */
public class ItemViewModel {
    private Context context;/**/
    private boolean isVisible = false;


    public boolean isVisible(){
        return isVisible;
    }

    public void setVisible(boolean isVisible){
        this.isVisible = isVisible;
    }
}
