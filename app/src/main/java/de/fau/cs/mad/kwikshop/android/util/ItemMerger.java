package de.fau.cs.mad.kwikshop.android.util;

import de.fau.cs.mad.kwikshop.common.ItemViewModel;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;
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
    public boolean mergeItem(int listId, ItemViewModel item){
        DomainListObject list = listManager.getList(listId);
        for(ItemViewModel items : list.getItems()){
            //merges if name, brand, comment, unit and group are the same
            if(!items.equals(item) && items.getName().equals(item.getName()) && items.getBrand().equals(item.getBrand())
                    && items.getComment().equals(item.getComment()) &&
                    ((items.getName() != null && items.getUnit().equals(item.getUnit())) || items.getUnit() == item.getUnit()) &&
                    ((items.getGroup() != null && items.getGroup().equals(item.getGroup())) || items.getGroup() == item.getGroup())){

                items.setAmount(items.getAmount() + item.getAmount());
                listManager.saveListItem(listId, items);
                return true;
            }
        }
        return false;
    }

}
