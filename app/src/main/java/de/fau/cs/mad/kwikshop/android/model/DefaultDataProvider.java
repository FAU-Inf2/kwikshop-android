package de.fau.cs.mad.kwikshop.android.model;

import android.content.Context;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.localization.ResourceId;

public class DefaultDataProvider {

    private static final String PIECE = "piece";
    private static final String OTHER = "Other";

    final Context context;
    static Unit[] defaultUnits;
    static Group[] defaultGroups;


    public DefaultDataProvider(Context context) {

        if (context == null) {
            throw new IllegalArgumentException("'context' must not be null");
        }

        this.context = context;
    }


    public static String getDefaultUnitName() {
        return PIECE;
    }

    public Unit[] getPredefinedUnits() {

        if (defaultUnits == null) {
            defaultUnits = new Unit[]{
                    new Unit(PIECE, ResourceId.Unit_Piece, ResourceId.Unit_short_Piece),
                    new Unit("bag", ResourceId.Unit_Bag),
                    new Unit("bottle", ResourceId.Unit_Bottle),
                    new Unit("box", ResourceId.Unit_Box),
                    new Unit("pack", ResourceId.Unit_Pack),
                    new Unit("dozen", ResourceId.Unit_Dozen),
                    new Unit("gram", ResourceId.Unit_Gram, ResourceId.Unit_short_Gram),
                    new Unit("kilogram", ResourceId.Unit_Kilogram, ResourceId.Unit_short_Kilogram),
                    new Unit("millilitre", ResourceId.Unit_Millilitre, ResourceId.Unit_short_Millilitre),
                    new Unit("litre", ResourceId.Unit_Litre, ResourceId.Unit_short_Litre),
                    new Unit("cup", ResourceId.Unit_Cup),
                    new Unit("tablespoon", ResourceId.Unit_Tablespoon, ResourceId.Unit_short_Tablespoon),
                    new Unit("can", ResourceId.Unit_Can)
            };
        }

        return defaultUnits;

    }

    public static String getDefaultGroupName() {
        return OTHER;
    }

    public Group[] getPredefinedGroups() {

        if (defaultGroups == null) {
            defaultGroups = new Group[]{
                    new Group("Coffee & Tea", ResourceId.Group_CoffeeAndTea),
                    new Group("Health & Hygiene", ResourceId.Group_HealthAndHygiene),
                    new Group("Pet Supplies", ResourceId.Group_PetSupplies),
                    new Group("Household", ResourceId.Group_Household),
                    new Group("Bread and Pastries", ResourceId.Group_BreadAndPastries),
                    new Group("Beverages", ResourceId.Group_Beverages),
                    new Group("Sweets & Snacks", ResourceId.Group_SweetsAndSnacks),
                    new Group("Baby Foods", ResourceId.Group_BabyFoods),
                    new Group("Pasta", ResourceId.Group_Pasta),
                    new Group("Milk & Cheese", ResourceId.Group_Dairy),
                    new Group("Fruits & Vegetables", ResourceId.Group_FruitsAndVegetables),
                    new Group("Meat & Fish", ResourceId.Group_MeatAndFish),
                    new Group("Ingredients & Spices", ResourceId.Group_IngredientsAndSpices),
                    new Group("Frozen & Convenience", ResourceId.Group_FrozenAndConvenience),
                    new Group("Tobacco", ResourceId.Group_Tobacco),
                    new Group(OTHER, ResourceId.Group_Other)
            };
        }

        return defaultGroups;
    }




}
