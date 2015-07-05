package de.fau.cs.mad.kwikshop.android.viewmodel;

import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewModelBase;

/**
 * Base class for all view models used to display a shopping list.
 * Offers to get the name of the shopping list
 */
public abstract class ListViewModelBase extends ViewModelBase {

    private String name;

    public interface Listener extends ViewModelBase.Listener {

        void onNameChanged(String value);

    }


    public String getName() {
        return name;
    }

    protected void setName(final String value) {

        boolean changed = false;
        if(name == null) {
            changed = name != value;
        } else {
            changed = !name.equals(value);
        }

        if (changed) {
            this.name = value;
            Listener listener = getListener();
            if (listener != null) {
                listener.onNameChanged(value);
            }
        }
    }

    @Override
    protected abstract Listener getListener();




}
