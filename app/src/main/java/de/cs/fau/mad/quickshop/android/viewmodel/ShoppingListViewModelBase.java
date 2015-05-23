package de.cs.fau.mad.quickshop.android.viewmodel;

/**
 * Base class for all view models used to display a shopping list.
 * Offers to get the name of the shopping list
 */
public abstract class ShoppingListViewModelBase {

    private String name;

    public interface Listener {

        void onNameChanged(String value);

    }


    public String getName() {
        return name;
    }

    protected void setName(final String value) {
        if ((name != null && !name.equals(value)) || value == null) {
            this.name = value;
            Listener listener = getListener();
            if (listener != null) {
                listener.onNameChanged(value);
            }
        }
    }


    protected abstract Listener getListener();
}
