package de.fau.cs.mad.kwikshop.android.model;

import android.os.AsyncTask;
import android.util.SparseArray;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;
import de.fau.cs.mad.kwikshop.android.model.exceptions.ItemNotFoundException;
import de.fau.cs.mad.kwikshop.android.model.exceptions.ListNotFoundException;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangeType;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;
import de.fau.cs.mad.kwikshop.android.model.tasks.SaveListTask;
import de.fau.cs.mad.kwikshop.common.util.EqualityComparer;
import de.greenrobot.event.EventBus;

public abstract class AbstractListManager<TList extends DomainListObject> implements ListManager<TList> {

    private final ListStorage<TList> listStorage;
    private final EqualityComparer equalityComparer;
    private final SimpleStorage<DeletedList> deletedListStorage;
    private final SimpleStorage<DeletedItem> deletedItemStorage;

    private final EventBus eventBus = EventBus.getDefault();

    private final Object loadLock = new Object();
    private final CountDownLatch loadLatch = new CountDownLatch(1);
    private AsyncTask<Void, Void, Void> loadTask;

    private final Object listLock = new Object();
    private Map<Integer, TList> lists;
    private SparseArray<SparseArray<Item>> listItems;



    public AbstractListManager(ListStorage<TList> listStorage, EqualityComparer equalityComparer,
                               SimpleStorage<DeletedList> deletedListStorage,
                               SimpleStorage<DeletedItem> deletedItemStorage) {

        if (listStorage == null) {
            throw new ArgumentNullException("listStorage");
        }

        if(equalityComparer == null) {
            throw new ArgumentNullException("equalityComparer");
        }

        if(deletedListStorage == null) {
            throw new ArgumentNullException("deletedListStorage");
        }

        if(deletedItemStorage == null) {
            throw new ArgumentNullException("deletedItemStorage");
        }


        this.listStorage = listStorage;
        this.equalityComparer = equalityComparer;
        this.deletedListStorage = deletedListStorage;
        this.deletedItemStorage = deletedItemStorage;

        loadLists();
    }

    @Override
    public Collection<TList> getLists() {
        loadLists();

        synchronized (listLock) {
            return lists.values();
        }
    }

    @Override
    public TList getList(int listId) {
        loadLists();
        synchronized (listLock) {
            if (!lists.containsKey(listId)) {
                throw listNotFound(listId);
            }
            return lists.get(listId);
        }
    }

    @Override
    public Collection<Item> getListItems(int listId) {

        loadLists();
        synchronized (listLock) {
            if (!lists.containsKey(listId)) {
                throw listNotFound(listId);
            }

            return lists.get(listId).getItems();
        }
    }

    @Override
    public Item getListItem(int listId, int itemId) {
        loadLists();

        synchronized (listLock) {
            if (!lists.containsKey(listId)) {
                throw listNotFound(listId);
            }

            if (listItems.get(listId).get(itemId) == null) {
                throw itemNotFound(listId, itemId);
            }

            return listItems.get(listId).get(itemId);
        }
    }


    @Override
    public int createList() {

        //currently realized as blocking operation so we can be sure the list's id has been set

        int id = listStorage.createList();
        TList list = listStorage.loadList(id);
        list.setLastModifiedDate(new Date());
        list.setModifiedSinceLastSync(true);
        listStorage.saveList(list);

        int listId = list.getId();
        synchronized (listLock) {
            lists.put(listId, list);
            listItems.put(listId, new SparseArray<Item>());
        }

        eventBus.post(getAddedListChangedEvent(list.getId()));


        return id;
    }

    @Override
    public TList saveList(int listId) {

        TList shoppingList = saveListWithoutEvents(listId);
        shoppingList.setModifiedSinceLastSync(true);
        eventBus.post(getPropertiesModifiedListChangedEvent(shoppingList.getId()));
        return shoppingList;
    }


    @Override
    public Item addListItem(int listId, Item item) {
        TList list;
        synchronized (listLock) {
            if (!lists.containsKey(listId)) {
                throw listNotFound(listId);
            }

            list = lists.get(listId);
        }

        list.addItem(item);
        list.setLastModifiedDate(new Date());
        list.setModifiedSinceLastSync(true);
        listStorage.saveList(list);

        synchronized (listLock) {
            listItems.get(listId).put(item.getId(), item);
        }

        eventBus.post(new ItemChangedEvent(getListType(), ItemChangeType.Added, listId, item.getId()));

        return item;
    }

    @Override
    public Item saveListItem(int listId, Item item) {

        TList list;
        synchronized (listLock) {
            if (!lists.containsKey(listId)) {
                throw listNotFound(listId);
            }

            list = lists.get(listId);

            if (listItems.get(listId).get(item.getId()) == null) {
                throw itemNotFound(listId, item.getId());
            }
        }

        list.setModifiedSinceLastSync(true);
        item.setModifiedSinceLastSync(true);

        saveListWithoutEvents(listId);

        eventBus.post(new ItemChangedEvent(getListType(), ItemChangeType.PropertiesModified, listId, item.getId()));

        return item;
    }

    @Override
    public boolean deleteItem(int listId, int itemId) {

        TList list;
        Item item;

        synchronized (listLock) {

            if (!lists.containsKey(listId)) {
                return false;
            }

            item = listItems.get(listId).get(itemId);
            if (item == null) {
                return false;
            }

            listItems.get(listId).remove(itemId);
            list = lists.get(listId);
        }


        deletedItemStorage.addItem(new DeletedItem(getListType(), listId, itemId, item.getVersion()));

        list.setModifiedSinceLastSync(true);
        saveListWithoutEvents(listId);

        eventBus.post(new ItemChangedEvent(getListType(),ItemChangeType.Deleted, listId, itemId));
        return true;

    }

    @Override
    public boolean deleteList(int listId) {

        TList list;
        synchronized (listLock) {
            if (!lists.containsKey(listId)) {
                return false;
            }
            list = lists.get(listId);
            lists.remove(listId);
            listItems.remove(listId);
        }

        listStorage.deleteList(listId);
        deletedListStorage.addItem(new DeletedList(getListType(), listId, list.getServerVersion()));

        eventBus.post(getDeletedListChangedEvent(listId));

        return true;
    }


    private void loadLists() {

        synchronized (loadLock) {

            if (loadTask == null) {

                loadTask = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {

                        lists = new HashMap<>();
                        listItems = new SparseArray<>();

                        List<TList> lists = listStorage.getAllLists();

                        for (TList list : lists) {

                            SparseArray<Item> items = new SparseArray<>();
                            for (Item item : list.getItems()) {
                                items.put(item.getId(), item);
                            }

                            int listId = list.getId();
                            synchronized (listLock) {
                                AbstractListManager.this.lists.put(listId, list);
                                listItems.put(listId, items);
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

    private TList saveListWithoutEvents(int listId) {

        TList list;
        synchronized (listLock) {
            if (!lists.containsKey(listId)) {

                throw listNotFound(listId);
            }
            list = lists.get(listId);
        }
        list.setLastModifiedDate(new Date());
        new SaveListTask<>(listStorage).execute(list);

        return list;
    }



    protected abstract ListNotFoundException listNotFound(int listId);

    protected abstract ItemNotFoundException itemNotFound(int listId, int itemId);

    protected abstract Object getAddedListChangedEvent(int id);

    protected abstract Object getDeletedListChangedEvent(int id);

    protected abstract Object getPropertiesModifiedListChangedEvent(int id);

    protected abstract ListType getListType();
}
