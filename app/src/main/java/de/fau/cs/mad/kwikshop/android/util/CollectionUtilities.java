package de.fau.cs.mad.kwikshop.android.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.fau.cs.mad.kwikshop.android.model.DeletedItem;
import de.fau.cs.mad.kwikshop.android.model.DeletedList;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainObject;

public class CollectionUtilities {


    public static <T extends DomainObject>Map<Integer, T> toMap(Collection<T> list) {

        Map<Integer, T> result = new HashMap<>();
        for(T item : list) {
            result.put(item.getId(), item);
        }
        return result;
    }


    public static Map<Integer, Item> toItemMapByServerId(Collection<Item> items) {

        Map<Integer, Item> result = new HashMap<>();
        for(Item item : items) {
            result.put(item.getServerId(), item);
        }
        return result;
    }


    public static Map<Integer, Map<Integer, DeletedItem>> toDeletedItemMapByClientId(Collection<DeletedItem> deltedItems) {

        Map<Integer, Map<Integer, DeletedItem>> result = new HashMap<>();

        for(DeletedItem item : deltedItems) {

            if(!result.containsKey(item.getListId())) {
                result.put(item.getListId(), new HashMap<Integer, DeletedItem>());
            }

            result.get(item.getListId()).put(item.getItemId(), item);
        }

        return result;
    }

    public static Map<Integer, Map<Integer, DeletedItem>> toDeletedItemMapByServerId(Collection<DeletedItem> deltedItems) {

        Map<Integer, Map<Integer, DeletedItem>> result = new HashMap<>();

        for(DeletedItem item : deltedItems) {

            if(!result.containsKey(item.getListIdServer())) {
                result.put(item.getListIdServer(), new HashMap<Integer, DeletedItem>());
            }

            result.get(item.getListIdServer()).put(item.getItemIdServer(), item);
        }

        return result;
    }

    public static Map<Integer, DeletedList> toDeletedListMapByClientId(Collection<DeletedList> deletedLists) {
        Map<Integer, DeletedList> result = new HashMap<>();
        for(DeletedList item : deletedLists) {
            result.put(item.getListId(), item);
        }
        return result;
    }

    public static Map<Integer, DeletedList> toDeletedListMapByServerId(Collection<DeletedList> deletedLists) {
        Map<Integer, DeletedList> result = new HashMap<>();
        for(DeletedList item : deletedLists) {
            result.put(item.getListIdServer(), item);
        }
        return result;
    }

}
