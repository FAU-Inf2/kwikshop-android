package de.fau.cs.mad.kwikshop.android.model;

import android.os.AsyncTask;

import java.util.*;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.common.*;
import de.fau.cs.mad.kwikshop.android.model.exceptions.*;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.messages.*;
import de.fau.cs.mad.kwikshop.android.model.tasks.SaveShoppingListTask;
import de.greenrobot.event.EventBus;

public class ShoppingListManager implements ListManager<ShoppingList> {


    private final ListStorage<ShoppingList> listStorage;
    private final EventBus eventBus = EventBus.getDefault();

    private final Object loadLock = new Object();
    private final CountDownLatch loadLatch = new CountDownLatch(1);
    private AsyncTask<Void, Void, Void> loadTask;

    private final Object listLock = new Object();
    private Map<Integer, ShoppingList> shoppingLists;
    private Map<Integer, Map<Integer, Item>> shoppingListItems;


    @Inject
    public ShoppingListManager(ListStorage<ShoppingList> listStorage) {
        if (listStorage == null) {
            throw new IllegalArgumentException("'listStorage' must not be null");
        }

        this.listStorage = listStorage;
        loadLists();
    }

    @Override
    public Collection<ShoppingList> getLists() {
        loadLists();

        synchronized (listLock) {
            return shoppingLists.values();
        }
    }

    @Override
    public ShoppingList getList(int listId) {
        loadLists();
        synchronized (listLock) {
            if (!shoppingLists.containsKey(listId)) {
                throw listNotFound(listId);
            }
            return shoppingLists.get(listId);
        }
    }

    @Override
    public Collection<Item> getListItems(int listId) {

        loadLists();
        synchronized (listLock) {
            if (!shoppingLists.containsKey(listId)) {
                throw listNotFound(listId);
            }

            return shoppingLists.get(listId).getItems();
        }
    }

    @Override
    public Item getListItem(int listId, int itemId) {
        loadLists();

        synchronized (listLock) {
            if (!shoppingLists.containsKey(listId)) {
                throw listNotFound(listId);
            }

            if (!shoppingListItems.get(listId).containsKey(itemId)) {
                throw itemNotFound(listId, itemId);
            }

            return shoppingListItems.get(listId).get(itemId);
        }
    }


    @Override
    public int createList() {

        //currently realized as blocking operation so we can be sure the list's id has been set

        int id = listStorage.createList();
        ShoppingList shoppingList = listStorage.loadList(id);
        listStorage.saveList(shoppingList);

        int listId = shoppingList.getId();
        synchronized (listLock) {
            shoppingLists.put(listId, shoppingList);
            shoppingListItems.put(listId, new HashMap<Integer, Item>());
        }

        eventBus.post(new ShoppingListChangedEvent(ShoppingListChangeType.Added, shoppingList.getId()));


        return id;
    }

    @Override
    public ShoppingList saveList(int listId) {

        ShoppingList shoppingList = saveListWithoutEvents(listId);
        eventBus.post(new ShoppingListChangedEvent(ShoppingListChangeType.PropertiesModified, shoppingList.getId()));
        return shoppingList;
    }

    @Override
    public Item addListItem(int listId, Item item) {
        ShoppingList list;
        synchronized (listLock) {
            if (!shoppingLists.containsKey(listId)) {
                throw listNotFound(listId);
            }

            list = shoppingLists.get(listId);
        }

        list.addItem(item);
        listStorage.saveList(list);

        synchronized (listLock) {
            shoppingListItems.get(listId).put(item.getId(), item);
        }

        eventBus.post(new ItemChangedEvent(ItemChangeType.Added, listId, item.getId()));

        return item;
    }

    @Override
    public Item saveListItem(int listId, Item item) {

        synchronized (listLock) {
            if (!shoppingLists.containsKey(listId)) {
                throw listNotFound(listId);
            }

            if (!shoppingListItems.get(listId).containsKey(item.getId())) {
                throw itemNotFound(listId, item.getId());
            }
        }
        saveListWithoutEvents(listId);

        eventBus.post(new ItemChangedEvent(ItemChangeType.PropertiesModified, listId, item.getId()));

        return item;
    }

    @Override
    public boolean deleteItem(int listId, int itemId) {
        ShoppingList list;
        synchronized (listLock) {

            if (!shoppingLists.containsKey(listId)) {
                return false;
            }

            if (!shoppingListItems.get(listId).containsKey(itemId)) {
                return false;
            }

            shoppingListItems.get(listId).remove(itemId);
            list = shoppingLists.get(listId);
        }

        if (list.removeItem(itemId)) {
            saveListWithoutEvents(listId);

            eventBus.post(new ItemChangedEvent(ItemChangeType.Deleted, listId, itemId));
            return true;

        } else {
            return false;
        }
    }

    @Override
    public boolean deleteList(int listId) {
        synchronized (listLock) {
            if (!shoppingLists.containsKey(listId)) {
                return false;
            }
            shoppingLists.remove(listId);
            shoppingListItems.remove(listId);
        }

        listStorage.deleteList(listId);

        eventBus.post(new ShoppingListChangedEvent(ShoppingListChangeType.Deleted, listId));

        return true;
    }


    private void loadLists() {

        synchronized (loadLock) {

            if (loadTask == null) {

                loadTask = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {

                        shoppingLists = new HashMap<>();
                        shoppingListItems = new HashMap<>();

                        List<ShoppingList> lists = listStorage.getAllLists();

                        for (ShoppingList list : lists) {

                            Map<Integer, Item> items = new HashMap<>();
                            for (Item item : list.getItems()) {
                                items.put(item.getId(), item);
                            }

                            int listId = list.getId();
                            synchronized (listLock) {
                                shoppingLists.put(listId, list);
                                shoppingListItems.put(listId, items);
                            }
                        }

                        loadLatch.countDown();
                        return null;
                    }
                };
                loadTask.execute();
            }
        }

        try {
            loadLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private ListNotFoundException listNotFound(int listId) {
        return new ListNotFoundException(String.format("Shopping list (Id %s) not found", listId));
    }

    private ItemNotFoundException itemNotFound(int listId, int itemId) {
        return new ItemNotFoundException(String.format("Could not find item with id %s in shopping list %s", listId, itemId));
    }

    private ShoppingList saveListWithoutEvents(int listId) {

        ShoppingList shoppingList;
        synchronized (listLock) {
            if (!shoppingLists.containsKey(listId)) {

                throw listNotFound(listId);
            }
            shoppingList = shoppingLists.get(listId);
        }
        new SaveShoppingListTask(listStorage).execute(shoppingList);

        return shoppingList;
    }

}
