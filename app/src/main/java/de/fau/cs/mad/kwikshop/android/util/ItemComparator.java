package de.fau.cs.mad.kwikshop.android.util;

import java.util.Comparator;

import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
import de.fau.cs.mad.kwikshop.android.view.ItemSortType;

public class ItemComparator implements Comparator<Item> {

    private final ItemSortType comparatorType;
    private final DisplayHelper displayHelper;


    public ItemComparator(DisplayHelper displayHelper, ItemSortType comparatorType) {

        if (displayHelper == null) {
            throw new IllegalArgumentException("'displayHelper' must not be null");
        }

        this.comparatorType = comparatorType;
        this.displayHelper = displayHelper;
    }

    @Override
    public int compare(Item item1, Item item2)
    {
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