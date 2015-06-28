package de.fau.cs.mad.kwikshop.android.viewmodel.common;

import de.fau.cs.mad.kwikshop.android.common.ShoppingList;

public class ShoppingListIdExtractor implements ObservableArrayList.IdExtractor<ShoppingList, Integer> {

    @Override
    public Integer getId(ShoppingList object) {
        return object.getId();
    }

}
