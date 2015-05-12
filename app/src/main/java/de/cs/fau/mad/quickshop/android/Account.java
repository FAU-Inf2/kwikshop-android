package de.cs.fau.mad.quickshop.android;

public abstract class Account {

  private int id;

  private String name;

  private Boolean loggedIn;

  public abstract Boolean delete();

  public abstract Boolean login();

  public abstract String create();

}
