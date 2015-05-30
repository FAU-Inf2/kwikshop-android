package de.cs.fau.mad.quickshop.android.model;

import java.util.List;

import de.cs.fau.mad.quickshop.android.common.Group;

public interface SimpleStorage<T> {

    List<T> getItems();

    void addItem(T group);

}
