package de.fau.cs.mad.kwikshop.android.util;

import de.fau.cs.mad.kwikshop.common.Item;
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
    public boolean mergeItem(int listId, Item item){
        DomainListObject list = listManager.getList(listId);

        item.setName(item.getName().trim());

        for(Item existingItem : list.getItems()){

            //skip item if item is bought (do not merge new item into bought item)
            if(existingItem.isBought()){
                continue;
            }

            //merges if name, brand, comment, unit and group are the same
            if(!existingItem.equals(item) && existingItem.getName().equals(item.getName()) && existingItem.getBrand().equals(item.getBrand())
                    && existingItem.getComment().equals(item.getComment()) &&
                    ((existingItem.getUnit() != null && existingItem.getUnit().equals(item.getUnit())) || existingItem.getUnit() == item.getUnit()) &&
                    ((existingItem.getGroup() != null && existingItem.getGroup().equals(item.getGroup())) || existingItem.getGroup() == item.getGroup())){

                //TODO: This might be a good place to use ObjectHelper.compare()

                existingItem.setAmount(existingItem.getAmount() + item.getAmount());
                listManager.saveListItem(listId, existingItem);
                return true;
            }
        }
        return false;
    }

}
