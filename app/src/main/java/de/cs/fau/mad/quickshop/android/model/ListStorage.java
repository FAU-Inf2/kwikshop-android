package de.cs.fau.mad.quickshop.android.model;

import java.util.List;

import de.cs.fau.mad.quickshop.android.common.ShoppingList;

public abstract class ListStorage {

  /** 
   *  returns a new unique id for the created list
   */
  public abstract int createList();

  /** 
   *  returns a Vector of list ids
   */
  public abstract List<ShoppingList> getAllLists();

  /** 
   *  returns the list identified by id
   */
  public abstract ShoppingList loadList(Integer listId);

  /** 
   *  saves a list in the storage
   */
  public abstract Boolean saveList(ShoppingList list);

  /** 
   *  deletes a list
   */
  public abstract Boolean deleteList(Integer id);

}
