package de.cs.fau.mad.quickshop.android.model;

import android.content.Context;

import java.util.List;

import cs.fau.mad.quickshop_android.R;
import de.cs.fau.mad.quickshop.android.common.Group;
import de.cs.fau.mad.quickshop.android.common.Unit;

public class DefaultDataProvider {

    final Context context;
    static Unit[] defaultUnits;
    static Group[] defaultGroups;


    public DefaultDataProvider(Context context) {

        if (context == null) {
            throw new IllegalArgumentException("'context' must not be null");
        }

        this.context = context;
    }


    public Unit[] getDefaultUnits() {

        if (defaultUnits == null) {
            defaultUnits = new Unit[]{
                    getUnit("piece", R.string.unit_piece, R.string.unit_piece_short),
                    getUnit("bag", R.string.unit_bag),
                    getUnit("bottle", R.string.unit_bottle),
                    getUnit("box", R.string.unit_box),
                    getUnit("pack", R.string.unit_pack),
                    getUnit("dozen", R.string.unit_dozen),
                    getUnit("gram", R.string.unit_gram, R.string.unit_gram_short),
                    getUnit("kilogram", R.string.unit_kilogram, R.string.unit_kilogram_short),
                    getUnit("millilitre", R.string.unit_millilitre, R.string.unit_millilitre_short),
                    getUnit("litre", R.string.unit_litre, R.string.unit_litre_short)
            };
        }

        return defaultUnits;

    }

    public Group[] getDefaultGroups() {

        if (defaultGroups == null) {
            defaultGroups = new Group[]{
                    getGroup("Coffee & Tea", R.string.group_CoffeeAndTea),
                    getGroup("Health & Hygiene", R.string.group_healthAndHygiene),
                    getGroup("Pet Supplies", R.string.group_petSupplies),
                    getGroup("Household", R.string.group_household),
                    getGroup("Bread and Pastries", R.string.group_breakPastries),
                    getGroup("Beverages", R.string.group_beverages),
                    getGroup("Sweets & Snacks", R.string.group_sweetsAndSnacks),
                    getGroup("Baby Foods", R.string.group_babyFoods),
                    getGroup("Pasta", R.string.group_pasta),
                    getGroup("Milk & Cheese", R.string.group_dairy),
                    getGroup("Fruits & Vegetables", R.string.group_fruitsAndVegetables),
                    getGroup("Meat & Fish", R.string.group_meatAndFish),
                    getGroup("Ingredients & Spices", R.string.group_ingredientsAndSpices),
                    getGroup("Frozen & Convenience", R.string.group_frozenAndConvenience),
                    getGroup("Tobacco", R.string.group_tobacco),
                    getGroup("Other", R.string.group_Other)
            };
        }

        return defaultGroups;
    }


    private Unit getUnit(String name, int displayNameResourceId) {
        return getUnit(name, displayNameResourceId, -1);
    }

    private Unit getUnit(String name, int displayNameResourceId, int shortDisplayNameResourceId) {

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

    private Group getGroup(String name, int displayNameResourceId) {

        String displayNameResourceName = context.getResources().getResourceName(displayNameResourceId);

        Group g = new Group();
        g.setName(name);
        g.setDisplayNameResourceName(displayNameResourceName);
        return g;
    }

}
