package de.cs.fau.mad.kwikshop.android.model;

import android.content.Context;

import cs.fau.mad.kwikshop_android.R;
import de.cs.fau.mad.kwikshop.android.common.Group;
import de.cs.fau.mad.kwikshop.android.common.Unit;

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
                    newUnit("litre", R.string.unit_litre, R.string.unit_litre_short)
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
                    newGroup("Coffee & Tea", R.string.group_CoffeeAndTea),
                    newGroup("Health & Hygiene", R.string.group_healthAndHygiene),
                    newGroup("Pet Supplies", R.string.group_petSupplies),
                    newGroup("Household", R.string.group_household),
                    newGroup("Bread and Pastries", R.string.group_breakPastries),
                    newGroup("Beverages", R.string.group_beverages),
                    newGroup("Sweets & Snacks", R.string.group_sweetsAndSnacks),
                    newGroup("Baby Foods", R.string.group_babyFoods),
                    newGroup("Pasta", R.string.group_pasta),
                    newGroup("Milk & Cheese", R.string.group_dairy),
                    newGroup("Fruits & Vegetables", R.string.group_fruitsAndVegetables),
                    newGroup("Meat & Fish", R.string.group_meatAndFish),
                    newGroup("Ingredients & Spices", R.string.group_ingredientsAndSpices),
                    newGroup("Frozen & Convenience", R.string.group_frozenAndConvenience),
                    newGroup("Tobacco", R.string.group_tobacco),
                    newGroup(OTHER, R.string.group_Other)
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

    private Group newGroup(String name, int displayNameResourceId) {

        String displayNameResourceName = context.getResources().getResourceName(displayNameResourceId);

        Group g = new Group();
        g.setName(name);
        g.setDisplayNameResourceName(displayNameResourceName);
        return g;
    }

}
