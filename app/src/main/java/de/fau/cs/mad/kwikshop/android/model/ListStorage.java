package de.fau.cs.mad.kwikshop.android.model;

import java.util.List;

import de.fau.cs.mad.kwikshop.android.common.ShoppingList;

public abstract class ListStorage<TList> {

  /** 
   *  returns a new unique id for the created list
   */
  public abstract int createList();

  /** 
   *  returns a Vector of list ids
   */
  public abstract List<TList> getAllLists();

  /** 
   *  returns the list identified by id
   */
  public abstract TList loadList(Integer listId);

  /** 
   *  saves a list in the storage
   */
  public abstract Boolean saveList(TList list);

  /** 
   *  deletes a list
   */
  public abstract Boolean deleteList(Integer id);

}
