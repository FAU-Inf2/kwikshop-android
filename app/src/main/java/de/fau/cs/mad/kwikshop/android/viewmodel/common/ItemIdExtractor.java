package de.fau.cs.mad.kwikshop.android.viewmodel.common;

import de.fau.cs.mad.kwikshop.common.Item;

/**
 * IdExtractor implementation for use in ObservableArrayList
 */
public class ItemIdExtractor implements ObservableArrayList.IdExtractor<Item, Integer> {

    @Override
    public Integer getId(Item object) {
        return object.getId();
    }

}
