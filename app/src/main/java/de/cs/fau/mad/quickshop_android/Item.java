package de.cs.fau.mad.quickshop_android;

public class Item {

  private Integer id;

  private Boolean bought;

  private String name;

  private Integer amount;

  private Boolean highlight;

  private String comment;

  private Group group;

  private Unit unit;

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

}
