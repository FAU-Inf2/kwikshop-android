package de.cs.fau.mad.quickshop.android.common;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "unit")
public class Unit {

  @DatabaseField(generatedId = true)
  private int id;

  @DatabaseField(canBeNull = false)
  private String name;

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
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

    Unit otherUnit = (Unit) other;
    return otherUnit.getId() == this.getId() &&
            otherUnit.getName().equals(this.getName());
  }

}
