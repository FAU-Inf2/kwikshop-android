package de.cs.fau.mad.kwikshop.android.util;

import android.view.Display;

import java.util.Comparator;

import de.cs.fau.mad.kwikshop.android.common.Group;
import de.cs.fau.mad.kwikshop.android.common.Item;
import de.cs.fau.mad.kwikshop.android.common.ShoppingList;
import de.cs.fau.mad.kwikshop.android.view.DisplayHelper;
import de.cs.fau.mad.kwikshop.android.view.ItemSortType;

public class ItemComparatorHelper implements Comparator<Integer> {

    private final ShoppingList shoppingList;
    private final ItemSortType comparatorType;
    private final DisplayHelper displayHelper;


    /*
        possible comparatorTypes:
        MANUAL
        GROUP
     */
    public ItemComparatorHelper(ShoppingList shoppingList, DisplayHelper displayHelper, ItemSortType comparatorType) {

        if (shoppingList == null) {
            throw new IllegalArgumentException("'shoppingList' must not be null");
        }

        if (displayHelper == null) {
            throw new IllegalArgumentException("'displayHelper' must not be null");
        }

        this.shoppingList = shoppingList;
        this.comparatorType = comparatorType;
        this.displayHelper = displayHelper;
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

                int groupOrder = displayHelper.getDisplayName(item1.getGroup())
                        .compareTo(displayHelper.getDisplayName(item2.getGroup()));

                //if groups are identical, sort alphabetically
                res = groupOrder;
                if(res != 0) break;


            case ALPHABETICALLY:

                int alphabetOrder = item1.getName().compareTo(item2.getName());

                //if the items have the same name, sort by item order
                res = alphabetOrder;
                if(res != 0) break;

            default:
                res = item1.getOrder() - item2.getOrder();
        }

        return res;
    }


}
