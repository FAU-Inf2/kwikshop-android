package de.fau.cs.mad.kwikshop.android.model.interfaces;


import java.util.List;

public interface SimpleStorage<T>  {

    List<T> getItems() ;

    void addItem(T item) ;

    void updateItem(T item) ;

    void deleteSingleItem(T item) ;

    void deleteAll() ;

    T getDefaultValue();

}
