package de.cs.fau.mad.quickshop.android.viewmodel.common;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Wrapper for lists that can be monitored for modifications ot the list
 *
 * @param <T> The type of items the list holds
 */
public class ObservableList<T> implements List<T> {


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

    }


    Listener<T> listener;           //current listener
    final List<T> wrappedList;      //the underlying list instance being wrapped


    /**
     * Initializes a new instance of ObservableList wrapping a new (empty) list
     */
    public ObservableList() {
        this(new ArrayList<T>());
    }

    /**
     * Initializes a new instance of ObservableList wrapping the specified list
     *
     * @param wrappedList The list to be wrapped
     */
    public ObservableList(List<T> wrappedList) {
        this.wrappedList = wrappedList;
    }


    public void setListener(final Listener<T> value) {
        this.listener = value;
    }

    @Override
    public void add(int location, T object) {
        wrappedList.add(location, object);
        if (listener != null) {
            listener.onItemAdded(object);
        }
    }

    @Override
    public boolean add(T object) {
        boolean success = wrappedList.add(object);
        if (success && listener != null) {
            listener.onItemAdded(object);
        }
        return success;
    }

    @Override
    public boolean addAll(int location, Collection<? extends T> collection) {
        boolean success = wrappedList.addAll(location, collection);
        if (success && listener != null) {
            for (T item : collection) {
                listener.onItemAdded(item);
            }
        }
        return success;
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        boolean success = wrappedList.addAll(collection);
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
            List<T> allItems = new ArrayList<>(wrappedList);
            wrappedList.clear();
            for (T item : allItems) {
                listener.onItemRemoved(item);
            }
        } else {
            wrappedList.clear();
        }
    }

    @Override
    public boolean contains(Object object) {
        return wrappedList.contains(object);
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> collection) {
        return wrappedList.containsAll(collection);
    }

    @Override
    public T get(int location) {
        return wrappedList.get(location);
    }

    @Override
    public int indexOf(Object object) {
        return wrappedList.indexOf(object);
    }

    @Override
    public boolean isEmpty() {
        return wrappedList.isEmpty();
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {

        final Iterator<T> baseIterator = wrappedList.iterator();
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

    @Override
    public int lastIndexOf(Object object) {
        return wrappedList.lastIndexOf(object);
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator() {

        return new ReadonlyListIteratorWrapper(wrappedList.listIterator());
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator(int location) {
        return new ReadonlyListIteratorWrapper(wrappedList.listIterator(location));
    }

    @Override
    public T remove(int location) {
        T removedItem = wrappedList.remove(location);
        if (listener != null) {
            listener.onItemRemoved(removedItem);
        }
        return removedItem;
    }

    @Override
    public boolean remove(Object object) {
        boolean success = wrappedList.remove(object);
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
        T oldItem = wrappedList.set(location, object);
        if (listener != null) {
            listener.onItemRemoved(oldItem);
            listener.onItemAdded(object);
        }
        return oldItem;
    }

    @Override
    public int size() {
        return wrappedList.size();
    }

    @NonNull
    @Override
    public List<T> subList(int start, int end) {
        return wrappedList.subList(start, end);
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return wrappedList.toArray();
    }

    @NonNull
    @Override
    public <T1> T1[] toArray(T1[] array) {
        return wrappedList.toArray(array);
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
