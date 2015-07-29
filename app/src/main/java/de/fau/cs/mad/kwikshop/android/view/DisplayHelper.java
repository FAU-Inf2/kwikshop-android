package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.android.util.StringHelper;
import de.fau.cs.mad.kwikshop.common.localization.ResourceId;

//TODO: Might make sense to move this to the viewmodel package
public class DisplayHelper {

    private final Context context;
    private final ResourceProvider resourceProvider;

    @Inject
    public DisplayHelper(Context context, ResourceProvider resourceProvider) {

        if (context == null) {
            throw new ArgumentNullException("context");
        }

        if(resourceProvider == null) {
            throw new ArgumentNullException("resourceProvider");
        }

        this.context = context;
        this.resourceProvider = resourceProvider;
    }


    /**
     * Gets the (localized) name for the specified group.
     * If no resource for localized name has been specified, the language-neutral name is returned
     */
    public String getDisplayName(Group group) {

        if (group == null) {
            return context.getResources().getString(R.string.other);
        } else {

            Integer id = getAndroidId(group.getResourceId());

            if(id == null) {
                return group.getName();
            } else {
                return resourceProvider.getString(id);
            }
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


    private Integer getAndroidId(ResourceId id) {

        if(id == null) {
            return null;
        }

        switch (id) {

            case Group_CoffeeAndTea:
                return R.string.group_CoffeeAndTea;
            case Group_HealthAndHygiene:
                return R.string.group_healthAndHygiene;
            case Group_PetSupplies:
                return R.string.group_petSupplies;
            case Group_Household:
                return R.string.group_household;
            case Group_BreadAndPastries:
                return R.string.group_breakPastries;
            case Group_Beverages:
                return R.string.group_beverages;
            case Group_SweetsAndSnacks:
                return R.string.group_sweetsAndSnacks;
            case Group_BabyFoods:
                return R.string.group_babyFoods;
            case Group_Pasta:
                return R.string.group_pasta;
            case Group_Dairy:
                return R.string.group_dairy;
            case Group_FruitsAndVegetables:
                return R.string.group_fruitsAndVegetables;
            case Group_MeatAndFish:
                return R.string.group_meatAndFish;
            case Group_IngredientsAndSpices:
                return R.string.group_ingredientsAndSpices;
            case Group_FrozenAndConvenience:
                return R.string.group_frozenAndConvenience;
            case Group_Tobacco:
                return R.string.group_tobacco;
            case Group_Other:
                return R.string.group_Other;
            default:
                return null;
        }

    }

}
