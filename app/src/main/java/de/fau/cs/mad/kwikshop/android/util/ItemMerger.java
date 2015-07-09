package de.fau.cs.mad.kwikshop.android.util;

import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.common.interfaces.DomainListObject;
import de.fau.cs.mad.kwikshop.android.model.DatabaseHelper;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;

public class ItemMerger<TList extends DomainListObject> {

    final private ListManager<TList> listManager;

    public ItemMerger(ListManager<TList> listManager){
        if (listManager == null) {
            throw new IllegalArgumentException("'listManager' must not be null");
        }

        this.listManager = listManager;
    }


    //returns true if item was merged, false if not
    public boolean mergeItem(int listId, Item item){
        DomainListObject list = listManager.getList(listId);
        for(Item items : list.getItems()){
            //merges if name, brand, comment, unit and group are the same
            if(items.getName().equals(item.getName()) && items.getBrand().equals(item.getBrand())
                    && items.getComment().equals(item.getComment()) && items.getUnit().equals(item.getUnit())
                    && items.getGroup().equals(item.getGroup())){

                items.setAmount(items.getAmount() + item.getAmount());
                listManager.saveListItem(listId, items);
                return true;
            }
        }
        return false;
    }

}
