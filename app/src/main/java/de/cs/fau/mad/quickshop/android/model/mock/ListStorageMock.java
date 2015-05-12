package de.cs.fau.mad.quickshop.android.model.mock;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import de.cs.fau.mad.quickshop.android.ListStorage;
import de.cs.fau.mad.quickshop.android.Item;
import de.cs.fau.mad.quickshop.android.ShoppingList;

public class ListStorageMock extends ListStorage {

    private ArrayList<ShoppingList> m_Lists = new ArrayList<>();



    public ListStorageMock() {

        for(int i = 0; i < 10; i++) {

            ShoppingList newList = new ShoppingList(i);
            newList.setName("Shopping list " + (i + 1));

            int itemCount = new Random().nextInt(30);

            for(int j = 0; j < itemCount; j++) {
                Item newItem = new Item();

                newList.addItem(newItem);
            }

            m_Lists.add(newList);
        }

    }

    @Override
    public int createList() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Vector<ShoppingList> getAllLists() {
        return new Vector<>(m_Lists);
    }

    @Override
    public ShoppingList loadList(Integer listId) {
        for(ShoppingList list : m_Lists) {
            if(list.getId() == listId) {
                return  list;
            }
        }

        throw new IllegalArgumentException("Unknown list");
    }

    @Override
    public Boolean saveList(ShoppingList list) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean deleteList(Integer id) {
        throw new UnsupportedOperationException();
    }
}
