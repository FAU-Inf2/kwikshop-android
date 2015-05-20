package de.cs.fau.mad.quickshop.android;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "item")
public class Item {

  // TODO: we might want to change the annotation to generatedId = true
  @DatabaseField(id = true)
  private int id;

  @DatabaseField
  private Boolean bought;

  @DatabaseField(canBeNull = false)
  private String name;

  @DatabaseField
  private int amount = 1;

  @DatabaseField
  private Boolean highlight;

  @DatabaseField(canBeNull = true)
  private String brand;

  @DatabaseField(canBeNull = true)
  private String comment;

  @DatabaseField(foreign = true)
  private Group group;

  @DatabaseField(foreign = true)
  private Unit unit;

   /**
   * the ShoppingList, that contains this Item.
    * (Required for ORMLite)
   */
  @DatabaseField(foreign = true)
  private ShoppingList shoppingList;

  public Item() {
   // Default no-arg constructor for generating Items, required for ORMLite
  }

  public int getId() {
    return id;
  }

  // TODO: REMOVE THIS, only for testing. IDs should be read only
  public void setID(int id) {
    this.id = id;
  }

  public Boolean isBought() {
    return bought;
  }

  public void setBought(Boolean bought) {
    this.bought = bought;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public Boolean isHighlight() {
    return highlight;
  }

  public void setHighlight(Boolean highlight) {
    this.highlight = highlight;
  }

  public String getBrand() {
    return (brand == null ? "" : brand);
  }

  public void setBrand(String brand) {
    this.brand = brand;
  }

  public String getComment() {
    return (comment == null ? "" : comment);
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public Group getGroup() {
    return group;
  }

  public void setGroup(Group group) {
    this.group = group;
  }

  public Unit getUnit() {
    return unit;
  }

  public void setUnit(Unit unit) {
    this.unit = unit;
  }
  
  //TODO: REMOVE THIS, only for testing. The shoppingList should only be changed by ORM
  public void setShoppingList(ShoppingList shoppingList) {
      this.shoppingList = shoppingList;
  }

  public ShoppingList getShoppingList() {
      return shoppingList;
  }
}
