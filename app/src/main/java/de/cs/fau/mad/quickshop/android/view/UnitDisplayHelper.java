package de.cs.fau.mad.quickshop.android.view;

import android.content.Context;

import de.cs.fau.mad.quickshop.android.common.Unit;
import de.cs.fau.mad.quickshop.android.util.StringHelper;

public class UnitDisplayHelper {

    private final Context context;


    public UnitDisplayHelper(Context context) {

        if (context == null) {
            throw new IllegalArgumentException("'context' must not be null");
        }

        this.context = context;
    }


    /**
     * Gets the (localized) name for the specified unit.
     * If no resource for localized name has been specified, the language-neutral name is returned
     */
    public String getDisplayName(Unit unit) {

        if (StringHelper.isNullOrWhiteSpace(unit.getDisplayNameResourceName())) {
            return unit.getName();
        } else {
            return getStringByName(unit.getDisplayNameResourceName());
        }

    }

    /**
     * Gets the (localized) short name for specified unit for display in the UI
     * If no resource for a localized short name has been specified,
     * the regular display name is returned
     */
    public String getShortDisplayName(Unit unit) {

        if (StringHelper.isNullOrWhiteSpace(unit.getShortDisplayNameResourceName())) {
            return getDisplayName(unit);
        } else {
            return getStringByName(unit.getShortDisplayNameResourceName());
        }

    }


    private String getStringByName(String name) {
        int id = context.getResources().getIdentifier(name, "string", context.getPackageName());
        return context.getResources().getString(id);
    }

}
