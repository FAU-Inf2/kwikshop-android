package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;

import fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.common.Group;
import de.fau.cs.mad.kwikshop.android.common.Unit;
import de.fau.cs.mad.kwikshop.android.util.StringHelper;

public class DisplayHelper {

    private final Context context;


    public DisplayHelper(Context context) {

        if (context == null) {
            throw new IllegalArgumentException("'context' must not be null");
        }

        this.context = context;
    }


    /**
     * Gets the (localized) name for the specified group.
     * If no resource for localized name has been specified, the language-neutral name is returned
     */
    public String getDisplayName(Group group) {

        if (group == null) {
            return context.getResources().getString(R.string.other);
        } else if (StringHelper.isNullOrWhiteSpace(group.getDisplayNameResourceName())) {
            return group.getName();
        } else {
            return getStringByName(group.getDisplayNameResourceName());
        }
    }

    /**
     * Gets the (localized) name for the specified unit.
     * If no resource for localized name has been specified, the language-neutral name is returned
     */
    public String getDisplayName(Unit unit) {

        if (unit == null) {
            return "";
        } else if (StringHelper.isNullOrWhiteSpace(unit.getDisplayNameResourceName())) {
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

        if (unit == null) {
            return "";
        } else if (StringHelper.isNullOrWhiteSpace(unit.getShortDisplayNameResourceName())) {
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
