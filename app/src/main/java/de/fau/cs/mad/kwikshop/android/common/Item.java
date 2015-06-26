package de.fau.cs.mad.kwikshop.android.common;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable(tableName = "item")
public class Item {

    public static final String FOREIGN_SHOPPINGLIST_FIELD_NAME = "shoppingList";

    public static final String FOREIGN_RECIPE_FIELD_NAME = "recipe";

    @DatabaseField(generatedId = true)
    private int id;

    // Order of this Item in the ShoppingList
    @DatabaseField
    private int order = -1;

    @DatabaseField
    private Boolean bought = false;

    @DatabaseField(canBeNull = false)
    private String name = "";

    @DatabaseField
    private int amount = 1;

    @DatabaseField
    private Boolean highlight = false;

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
    @DatabaseField(foreign = true, columnName = FOREIGN_SHOPPINGLIST_FIELD_NAME)
    private ShoppingList shoppingList;

    @DatabaseField(foreign = true, columnName = FOREIGN_RECIPE_FIELD_NAME)
    private Recipe recipe;

    @DatabaseField(canBeNull = true)
    private Date lastBought;

    @DatabaseField
    private boolean regularlyRepeatItem;

    @DatabaseField
    private TimePeriodsEnum periodType;

    @DatabaseField
    private int selectedRepeatTime;

    @DatabaseField
    private boolean remindFromNextPurchaseOn;

    @DatabaseField(canBeNull = true)
    private Date remindAtDate;

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

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
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

    public void setLastBought(Date lastBought) {
        this.lastBought = lastBought;
    }

    public Date getLastBought() {
        return lastBought;
    }

    public void setRegularlyRepeatItem(boolean regularlyRepeatItem) {
        this.regularlyRepeatItem = regularlyRepeatItem;
    }

    public boolean isRegularlyRepeatItem() {
        return regularlyRepeatItem;
    }

    public void setPeriodType(TimePeriodsEnum periodType) {
        this.periodType = periodType;
    }

    public TimePeriodsEnum getPeriodType() {
        return periodType;
    }

    public void setSelectedRepeatTime(int selectedRepeatTime) {
        this.selectedRepeatTime = selectedRepeatTime;
    }

    public int getSelectedRepeatTime() {
        return selectedRepeatTime;
    }

    public Date getRemindAtDate() {
        return remindAtDate;
    }

    public void setRemindAtDate(Date remindAtDate) {
        this.remindAtDate = remindAtDate;
    }

    public boolean isRemindFromNextPurchaseOn() {
        return remindFromNextPurchaseOn;
    }

    public boolean isRemindFromNowOn() {
        return !remindFromNextPurchaseOn;
    }

    public void setRemindFromNextPurchaseOn(boolean remindFromNextPurchaseOn) {
        this.remindFromNextPurchaseOn = remindFromNextPurchaseOn;
    }

    public void setRemindFromNowOn(boolean remindFromNowOn) {
        this.remindFromNextPurchaseOn = !remindFromNowOn;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (this == o)
            return true;
        if (o.getClass() != this.getClass())
            return false;
        Item item = (Item) o;
        return item.id == this.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
