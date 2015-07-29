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
                    newUnit(PIECE, R.string.unit_piece, R.string.unit_piece_short),
                    newUnit("bag", R.string.unit_bag),
                    newUnit("bottle", R.string.unit_bottle),
                    newUnit("box", R.string.unit_box),
                    newUnit("pack", R.string.unit_pack),
                    newUnit("dozen", R.string.unit_dozen),
                    newUnit("gram", R.string.unit_gram, R.string.unit_gram_short),
                    newUnit("kilogram", R.string.unit_kilogram, R.string.unit_kilogram_short),
                    newUnit("millilitre", R.string.unit_millilitre, R.string.unit_millilitre_short),
                    newUnit("litre", R.string.unit_litre, R.string.unit_litre_short),
                    newUnit("cup", R.string.unit_cup),
                    newUnit("tablespoon", R.string.unit_tablespoon, R.string.unit_tablespoon_short),
                    newUnit("can", R.string.unit_can)
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


    private Unit newUnit(String name, int displayNameResourceId) {
        return newUnit(name, displayNameResourceId, -1);
    }

    private Unit newUnit(String name, int displayNameResourceId, int shortDisplayNameResourceId) {

        String displayNameResourceName = context.getResources().getResourceName(displayNameResourceId);

        String shortDisplayNameResourceName = shortDisplayNameResourceId != -1
                ? context.getResources().getResourceName(shortDisplayNameResourceId)
                : null;

        Unit unit = new Unit();
        unit.setName(name);
        unit.setDisplayNameResourceName(displayNameResourceName);
        unit.setShortDisplayNameResourceName(shortDisplayNameResourceName);

        return unit;
    }



}
