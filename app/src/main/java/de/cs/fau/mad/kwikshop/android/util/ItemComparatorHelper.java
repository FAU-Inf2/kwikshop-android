package de.cs.fau.mad.kwikshop.android.util;

import java.util.Comparator;

import de.cs.fau.mad.kwikshop.android.common.Item;
import de.cs.fau.mad.kwikshop.android.common.ShoppingList;

public class ItemComparatorHelper implements Comparator<Integer> {

    public enum SortType {
        MANUAL, // Manual ordering, edited by the user via drag and drop
        GROUP   // Order by Group
    }

    ShoppingList shoppingList;
    SortType comparatorType;

    public ItemComparatorHelper(ShoppingList shoppingList, SortType comparatorType) {
        this.shoppingList = shoppingList;
        this.comparatorType = comparatorType;
    }

    @Override
    public int compare(Integer i1, Integer i2)
    {
        Item item1 = shoppingList.getItem(i1);
        Item item2 = shoppingList.getItem(i2);

        int res = 0;

        switch(comparatorType) {
            case MANUAL:
                res = item1.getOrder() - item2.getOrder();
                break;
            case GROUP:
                // TODO
                break;
            default:
                res = item1.getOrder() - item2.getOrder();
        }

        return res;
    }
}
