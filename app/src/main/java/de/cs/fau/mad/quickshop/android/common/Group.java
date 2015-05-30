package de.cs.fau.mad.quickshop.android.common;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "group")
public class Group {
  @DatabaseField(generatedId = true)
  private int id;

  @DatabaseField(canBeNull = false)
  private String name;

  @DatabaseField(canBeNull = true)
  private String displayNameResourceName;

  public Group() {
      // Default no-arg constructor for generating Groups, required for ORMLite
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

  public String getDisplayNameResourceName() {
    return displayNameResourceName;
  }

  public void setDisplayNameResourceName(String value) {
    this.displayNameResourceName = value;
  }

}
