package de.cs.fau.mad.quickshop.android;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "item")
public class Item {

  @DatabaseField(generatedId = true)
  private Integer id;

  @DatabaseField
  private Boolean bought;

  @DatabaseField(canBeNull = false)
  private String name;

  @DatabaseField
  private Integer amount;

  @DatabaseField
  private Boolean highlight;

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

  public Integer getId() {
    return id;
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

  public Integer getAmount() {
    return amount;
  }

  public void setAmount(Integer amount) {
    this.amount = amount;
  }

  public Boolean isHighlight() {
    return highlight;
  }

  public void setHighlight(Boolean highlight) {
    this.highlight = highlight;
  }

  public String getComment() {
    return comment;
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

    public void setShoppingList(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
    }

    public ShoppingList getShoppingList() {
        return shoppingList;
    }
}
