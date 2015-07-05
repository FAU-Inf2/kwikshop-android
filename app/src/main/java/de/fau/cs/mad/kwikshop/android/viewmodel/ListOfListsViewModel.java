package de.fau.cs.mad.kwikshop.android.viewmodel;

import de.fau.cs.mad.kwikshop.android.common.Recipe;
import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.common.interfaces.DomainListObject;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ListIdExtractor;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewModelBase;
import de.greenrobot.event.EventBus;

public abstract class ListOfListsViewModel<TList extends DomainListObject> extends ViewModelBase{

    // listener interface
    public interface Listener<TList> extends ViewModelBase.Listener {

        void onListsChanged(final ObservableArrayList<TList, Integer> oldValue,
                            final ObservableArrayList<TList, Integer> newValue);
    }

    protected final ViewLauncher viewLauncher;
    protected final ListManager<TList> listManager;

    protected Listener<TList> listener;
    private ObservableArrayList<TList, Integer> lists;


    public ListOfListsViewModel(ViewLauncher viewLauncher, ListManager<TList> listManager) {

        if(viewLauncher == null) {
            throw new IllegalArgumentException("'viewLauncher' must not be null");
        }

        if (listManager == null) {
            throw new IllegalArgumentException("'listManager' must not be null");
        }

        this.listManager = listManager;
        this.viewLauncher = viewLauncher;

        setLists(new ObservableArrayList<>(new ListIdExtractor<TList>(), listManager.getLists()));
        EventBus.getDefault().register(this);
    }


    public ObservableArrayList<TList, Integer> getLists() {
        return this.lists;
    }

    private void setLists(final ObservableArrayList<TList, Integer> value) {
        if (value != lists) {
            ObservableArrayList<TList, Integer> oldValue = this.lists;
            this.lists = value;
            if (listener != null) {
                listener.onListsChanged(oldValue, value);
            }
        }
    }

    public void setListener(final Listener<TList> listener) {
        this.listener = listener;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ItemChangedEvent ev) {

        if (ev.getListType() == getListType()) {
            switch (ev.getChangeType()) {

                case Deleted:
                case Added:
                    reloadList(ev.getListId());
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    protected Listener getListener() {
        return listener;
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
    }


    protected void reloadList(int listId) {
        TList list = listManager.getList(listId);
        synchronized (this) {

            int index = lists.indexOfById(list.getId());
            if (index >= 0) {
                lists.set(index, list);
            } else {
                lists.add(list);
            }
        }
    }

    protected abstract ListType getListType();
}
