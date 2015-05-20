package de.cs.fau.mad.quickshop.android.model.mock;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import de.cs.fau.mad.quickshop.android.ListStorage;
import de.cs.fau.mad.quickshop.android.Item;
import de.cs.fau.mad.quickshop.android.ShoppingList;

public class ListStorageMock extends ListStorage {

    private static int nextId = 0;
    private static ArrayList<ShoppingList> m_Lists = new ArrayList<>();




    public ListStorageMock() {

        if (m_Lists.size() == 0) {

            for (int i = 0; i < 10; i++) {

                int newListId = createList();
                ShoppingList newList = loadList(newListId);

                int itemCount = new Random().nextInt(30);

                for (int j = 0; j < itemCount; j++) {
                    Item newItem = new Item();
                    newItem.setID(j);
                    newItem.setName("Item number "+ j);

                    newList.addItem(newItem);
                }
            }
        }

    }

    @Override
    public int createList() {

        int id = nextId++;
        ShoppingList newList = new ShoppingList(id);
        newList.setName("Shopping list " + id);

        m_Lists.add(newList);

        return id;
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

        throw new IllegalArgumentException("Unknown list, id = " + listId);
    }

    @Override
    public Boolean saveList(ShoppingList list) {

        deleteList(list.getId());
        m_Lists.add(list);
        return true;

    }

    @Override
    public Boolean deleteList(Integer id) {

        ShoppingList listToRemove = null;
        for (ShoppingList existingList : m_Lists) {
            if (existingList.getId() == id) {
                listToRemove = existingList;
                break;
            }
        }

        if (listToRemove != null) {
            return m_Lists.remove(listToRemove);
        } else {
            return false;
        }
    }
}
