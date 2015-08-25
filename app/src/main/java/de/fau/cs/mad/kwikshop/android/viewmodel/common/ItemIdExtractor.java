package de.fau.cs.mad.kwikshop.android.viewmodel.common;

import de.fau.cs.mad.kwikshop.common.ItemViewModel;

/**
 * IdExtractor implementation for use in ObservableArrayList
 */
public class ItemIdExtractor implements ObservableArrayList.IdExtractor<ItemViewModel, Integer> {

    @Override
    public Integer getId(ItemViewModel object) {
        return object.getId();
    }

}
