package de.fau.cs.mad.kwikshop.android.viewmodel;


import java.util.Collections;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;
import de.fau.cs.mad.kwikshop.android.model.messages.ReminderTimeIsOverEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ShoppingListChangedEvent;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.greenrobot.event.EventBus;

public class ListOfShoppingListsViewModel extends ListOfListsViewModel<ShoppingList> {

    //needs to be static because it is accessed indirectly in the constructor of the super-class
    private static final ListLastModifiedDateComparator<ShoppingList> listComparator = new ListLastModifiedDateComparator<>();

    // backing fields for properties

    private final Command addShoppingListCommand = new Command<Object>() {
        @Override
        public void execute(Object parameter) {
            viewLauncher.showAddShoppingListView();
        }
    };
    private final Command<Integer> selectShoppingListCommand = new Command<Integer>() {
        @Override
        public void execute(Integer shoppingListId) {
            viewLauncher.showShoppingListWithSupermarketDialog(shoppingListId);
        }
    };
    private final Command<Integer> selectShoppingListDetailsCommand = new Command<Integer>() {
        @Override
        public void execute(Integer shoppingListId) {
            viewLauncher.showShoppingListDetailsView(shoppingListId);
        }
    };


    @Inject
    public ListOfShoppingListsViewModel(ViewLauncher viewLauncher, ListManager<ShoppingList> listManager) {

        super(viewLauncher, listManager);
    }


    // Getters / Setters

    public Command getAddShoppingListCommand() {
        return this.addShoppingListCommand;
    }

    public Command<Integer> getSelectShoppingListCommand() {
        return this.selectShoppingListCommand;
    }

    public Command<Integer> getSelectShoppingListDetailsCommand() {
        return this.selectShoppingListDetailsCommand;
    }

    @Override
    protected void setLists(final ObservableArrayList<ShoppingList, Integer> value) {

        super.setLists(value);
        Collections.sort(value, listComparator);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ShoppingListChangedEvent ev) {

        switch (ev.getChangeType()) {

            case Deleted:
                getLists().removeById(ev.getListId());
                break;

            case PropertiesModified:
            case Added:
                reloadList(ev.getListId());
                break;

            default:
                break;
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(ReminderTimeIsOverEvent e) {

        EventBus.getDefault().cancelEventDelivery(e);

        int itemId = e.getItemId();
        int listId = e.getListId();
        viewLauncher.showReminderView(listId, itemId);
    }


    @Override
    protected ListType getListType() {
        return ListType.ShoppingList;
    }

    @Override
    protected void reloadList(int listId) {
        super.reloadList(listId);
        Collections.sort(getLists(), listComparator);
    }


}
