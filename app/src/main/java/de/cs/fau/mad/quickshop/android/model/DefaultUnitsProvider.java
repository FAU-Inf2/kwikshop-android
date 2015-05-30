package de.cs.fau.mad.quickshop.android.model;

import android.content.Context;

import java.util.List;

import cs.fau.mad.quickshop_android.R;
import de.cs.fau.mad.quickshop.android.common.Unit;

public class DefaultUnitsProvider {

    final Context context;
    static Unit[] defaultUnits;


    public DefaultUnitsProvider(Context context) {

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


    private Unit getUnit(String name, int nameResourceId) {
        return getUnit(name, nameResourceId, -1);
    }

    private Unit getUnit(String name, int nameResourceId, int shortNameResourceId) {

        String nameResourceName = context.getResources().getResourceName(nameResourceId);

        String shortNameResourceName = shortNameResourceId != -1
                ? context.getResources().getResourceName(shortNameResourceId)
                : null;

        Unit unit = new Unit();
        unit.setName(name);
        unit.setDisplayNameResourceName(nameResourceName);
        unit.setShortDisplayNameResourceName(shortNameResourceName);

        return unit;
    }

}
