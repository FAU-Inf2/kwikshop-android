package de.cs.fau.mad.quickshop_android;

import java.util.Vector;

public class LocalListStorage extends ListStorage {
    @Override
    public int createList() {
        return 0;
    }

    @Override
    public Vector getAllLists() {
        return null;
    }

    @Override
    public ShoppingList loadList(Integer listID) {
        return null;
    }

    @Override
    public Boolean saveList(ShoppingList list) {
        return null;
    }

    @Override
    public Boolean deleteList(Integer id) {
        return null;
    }
}
