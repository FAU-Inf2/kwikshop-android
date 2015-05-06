package de.cs.fau.mad.quickshop_android;

import java.util.Vector;

public abstract class ListStorage {

  /** 
   *  returns a new unique id for the created list
   */
  public abstract int createList();

  /** 
   *  returns a Vector of list ids
   */
  public abstract Vector getAllLists();

  /** 
   *  returns the list identified by id
   */
  public abstract List loadList(Integer listId);

  /** 
   *  saves a list in the storage
   */
  public abstract Boolean saveList(List list);

  /** 
   *  deletes a list
   */
  public abstract Boolean deleteList(Integer id);

}
