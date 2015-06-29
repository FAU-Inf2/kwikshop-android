package de.fau.cs.mad.kwikshop.android.model.interfaces;

import java.util.List;

import de.fau.cs.mad.kwikshop.android.common.ShoppingList;

public interface ListStorage<TList> {

  /** 
   *  returns a new unique id for the created list
   */
  int createList();

  /**
   *  Returns a list of all lists
   */
  List<TList> getAllLists();

  /** 
   *  returns the list identified by id
   */
  TList loadList(int listId);

  /** 
   *  saves a list in the storage
   */
  boolean saveList(TList list);

  /** 
   *  deletes a list
   */
  boolean deleteList(int id);

}
