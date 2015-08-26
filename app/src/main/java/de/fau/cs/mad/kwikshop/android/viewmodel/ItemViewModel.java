package de.fau.cs.mad.kwikshop.android.viewmodel;


import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.common.Item;

/**
 * Created by oq22eval on 8/26/15.
 */
public class ItemViewModel {


    private final Item item;
    private boolean Visible = false;

    @Inject
    public ItemViewModel(Item item){

        this.item = item;
    }

    public boolean isVisible() {
        return Visible;
    }

    public void setVisible(boolean visible) {
        this.Visible = visible;
    }

    public Item getItem() {
        return item;
    }

}
