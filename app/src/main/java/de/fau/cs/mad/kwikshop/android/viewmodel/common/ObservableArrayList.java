package de.fau.cs.mad.kwikshop.android.viewmodel.common;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Arraylist that can be monitored for changes
 *
 * @param <T> The type of items the list holds
 */
public class ObservableArrayList<T, K> extends ArrayList<T> {


    public interface IdExtractor<T, K> {

        public K getId(T object);

    }

    /**
     * Listener interface for observers to implement
     *
     * @param <T> The type of items the list being observed contains
     */
    public interface Listener<T> {

        /**
         * Called after an item has been added to the list
         *
         * @param newItem The item that was added to the list
         */
        void onItemAdded(T newItem);

        /**
         * Called after an item has been removed from the list
         *
         * @param removedItem The item that was removed from the list
         */
        void onItemRemoved(T removedItem);

        void onItemModified(T modifiedItem);

    }


    private final IdExtractor<T, K> idExtractor;
    private Listener<T> listener;           //current listener


    public ObservableArrayList(IdExtractor<T, K> idExtractor) {
        super();

        if (idExtractor == null) {
            throw new IllegalArgumentException("'idExtractor' must not be null");
        }

        this.idExtractor = idExtractor;
    }

    public ObservableArrayList(IdExtractor<T, K> idExtractor, Collection<? extends T> c) {
        super(c);

        if (idExtractor == null) {
            throw new IllegalArgumentException("'idExtractor' must not be null");
        }

        this.idExtractor = idExtractor;
    }


    public void setListener(final Listener<T> value) {
        this.listener = value;
    }

    public void replaceListener(final Listener<T> toReplace, final Listener<T> newListener) {
        if (this.listener == toReplace) {
            this.listener = newListener;
        }
    }

    @Override
    public void add(int location, T object) {
        super.add(location, object);
        if (listener != null) {
            listener.onItemAdded(object);
        }
    }

    @Override
    public boolean add(T object) {
        boolean success = super.add(object);
        if (success && listener != null) {
            listener.onItemAdded(object);
        }
        return success;
    }

    @Override
    public boolean addAll(int location, Collection<? extends T> collection) {
        boolean success = super.addAll(location, collection);
        if (success && listener != null) {
            for (T item : collection) {
                listener.onItemAdded(item);
            }
        }
        return success;
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        boolean success = super.addAll(collection);
        if (success && listener != null) {
            for (T item : collection) {
                listener.onItemAdded(item);
            }
        }
        return success;
    }

    @Override
    public void clear() {
        if (listener != null) {
            List<T> allItems = new ArrayList<>(this);
            super.clear();
            for (T item : allItems) {
                listener.onItemRemoved(item);
            }
        } else {
            super.clear();
        }
    }


    @NonNull
    @Override
    public Iterator<T> iterator() {

        final Iterator<T> baseIterator = super.iterator();
        return new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return baseIterator.hasNext();
            }

            @Override
            public T next() {
                return baseIterator.next();
            }

            @Override
            public void remove() {
                //no supported as we're not able to fire the right event without a specialized
                // iterator implementation
                throw new UnsupportedOperationException();
            }
        };
    }


    @NonNull
    @Override
    public ListIterator<T> listIterator() {

        return new ReadonlyListIteratorWrapper(super.listIterator());
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator(int location) {
        return new ReadonlyListIteratorWrapper(super.listIterator(location));
    }

    @Override
    public T remove(int location) {
        T removedItem = super.remove(location);
        if (listener != null) {
            listener.onItemRemoved(removedItem);
        }
        return removedItem;
    }

    @Override
    public boolean remove(Object object) {
        boolean success = super.remove(object);
        if (success && listener != null) {
            try {
                listener.onItemRemoved((T) object);
            } catch (ClassCastException ex) {
                //do not fire event if the object we removed from the list can't be cast to T
                // (it should have been there in the first place)
                //no idea why java offers remove(Object) instead of remove(T)
            }
        }
        return success;
    }


    @Override
    public boolean removeAll(@NonNull Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T set(int location, T object) {
        T oldItem = super.set(location, object);
        if (listener != null) {
            listener.onItemRemoved(oldItem);
            listener.onItemAdded(object);
        }
        return oldItem;
    }


    public void notifyItemModifiedById(K id) {
        T item = getById(id);
        if (item != null) {
            notifyItemModified(item);
        }
    }

    public void notifyItemModified(T modifiedItem) {
        if (listener != null) {
            listener.onItemModified(modifiedItem);
        }
    }

    public boolean removeById(K id) {
        T toRemove = getById(id);

        if (toRemove == null) {
            return false;
        } else {
            return remove(toRemove);
        }
    }


    public T getById(K id) {

        //TODO: optimize this (perhaps using a HashMap of Ids)

        for (T item : this) {
            if (idExtractor.getId(item).equals(id)) {
                return item;
            }
        }

        return null;
    }

    public int indexOfById(K id) {
        T item = getById(id);
        if (item == null) {
            return -1;
        } else {
            return indexOf(item);
        }
    }

    public boolean containsById(K id) {
        return getById(id) != null;
    }

    public T setOrAddById(T item) {
        K id = idExtractor.getId(item);
        if(containsById(id)) {
            int index = indexOfById(id);
            return set(index, item);
        } else {
            add(item);
            return item;
        }

    }

    private class ReadonlyListIteratorWrapper implements ListIterator<T> {

        private ListIterator<T> wrappedIterator;

        public ReadonlyListIteratorWrapper(ListIterator<T> wrappedIterator) {
            this.wrappedIterator = wrappedIterator;
        }

        @Override
        public void add(T object) {
            wrappedIterator.add(object);
            if (listener != null) {
                listener.onItemAdded(object);
            }
        }

        @Override
        public boolean hasNext() {
            return wrappedIterator.hasNext();
        }

        @Override
        public boolean hasPrevious() {
            return wrappedIterator.hasPrevious();
        }

        @Override
        public T next() {
            return wrappedIterator.next();
        }

        @Override
        public int nextIndex() {
            return wrappedIterator.nextIndex();
        }

        @Override
        public T previous() {
            return wrappedIterator.previous();
        }

        @Override
        public int previousIndex() {
            return wrappedIterator.previousIndex();
        }

        @Override
        public void remove() {
            //no supported as we're not able to fire the right event without a specialized
            // iterator implementation
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(T object) {
            //no supported as we're not able to fire the right event without a specialized
            // iterator implementation
            throw new UnsupportedOperationException();
        }
    }

}
