package de.fau.cs.mad.kwikshop.android.common.interfaces;

import java.util.Collection;
import java.util.Date;

import de.fau.cs.mad.kwikshop.android.common.Item;

public interface DomainListObject extends DomainObject {

    Collection<Item> getItems();

    void addItem(Item item);

    boolean removeItem(int itemId);

    Date getLastModifiedDate();

    void setLastModifiedDate(Date value);
}
