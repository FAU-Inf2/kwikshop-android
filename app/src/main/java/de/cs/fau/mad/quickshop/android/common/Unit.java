package de.cs.fau.mad.quickshop.android.common;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "unit")
public class Unit {

  @DatabaseField(generatedId = true)
  private int id;

  @DatabaseField(canBeNull = false)
  //language-neutral name of the unit (= english)
  private String name;

  @DatabaseField(canBeNull = true)
  // the name of the string resource for the localized name of the unit
  private String displayNameResourceName;

  @DatabaseField(canBeNull = true)
  //the name of the string resource for the localized short name of the unit
  private String shortDisplayNameResourceName;

  public Unit(){
      // Default no-arg constructor for generating Units, required for ORMLite
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }


  public String getName() {
    return this.name;
  }

  public void setName(String value) {
    this.name = value;
  }

  public String getDisplayNameResourceName() {
    return displayNameResourceName;
  }

  public void setDisplayNameResourceName(String value) {
    this.displayNameResourceName = value;
  }

  public String getShortDisplayNameResourceName() {
    return shortDisplayNameResourceName;
  }

  public void setShortDisplayNameResourceName(String value) {
    this.shortDisplayNameResourceName = value;
  }


  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(Object other) {

    if (other == null) {
      return false;
    }

    if (!(other instanceof Unit)) {
      return false;
    }

    return equals((Unit) other);

  }


  public boolean equals(Unit other) {
    return other.getId() == this.getId() && other.getName().equals(this.getName());
  }


}
