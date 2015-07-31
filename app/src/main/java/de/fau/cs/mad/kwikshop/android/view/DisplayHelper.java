package de.fau.cs.mad.kwikshop.android.view;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.localization.ResourceId;

//TODO: Might make sense to move this to the viewmodel package
public class DisplayHelper {

    private final ResourceProvider resourceProvider;

    @Inject
    public DisplayHelper(ResourceProvider resourceProvider) {

        if(resourceProvider == null) {
            throw new ArgumentNullException("resourceProvider");
        }

        this.resourceProvider = resourceProvider;
    }


    /**
     * Gets the (localized) name for the specified group.
     * If no resource for localized name has been specified, the language-neutral name is returned
     */
    public String getDisplayName(Group group) {

        if (group == null) {
            return resourceProvider.getString(R.string.other);
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
        } else {

            Integer id = getAndroidId(unit.getResourceId());
            if(id == null) {
                return unit.getName();
            } else {
                return resourceProvider.getString(id);
            }
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
        } else {
            Integer id = getAndroidId(unit.getShortNameResourceId());
            if(id == null) {
                return getDisplayName(unit);
            } else {
                return resourceProvider.getString(id);
            }
        }

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
            case Unit_Piece:
                return R.string.unit_piece;
            case Unit_short_Piece:
                return R.string.unit_piece_short;
            case Unit_Bag:
                return R.string.unit_bag;
            case Unit_Bottle:
                return R.string.unit_bottle;
            case Unit_Box:
                return R.string.unit_box;
            case Unit_Pack:
                return R.string.unit_pack;
            case Unit_Dozen:
                return R.string.unit_dozen;
            case Unit_Gram:
                return R.string.unit_gram;
            case Unit_short_Gram:
                return R.string.unit_gram_short;
            case Unit_Kilogram:
                return R.string.unit_kilogram;
            case Unit_short_Kilogram:
                return R.string.unit_kilogram_short;
            case Unit_Millilitre:
                return R.string.unit_millilitre;
            case Unit_short_Millilitre:
                return R.string.unit_millilitre_short;
            case Unit_Litre :
                return R.string.unit_litre;
            case Unit_short_Litre:
                return R.string.unit_litre_short;
            case Unit_Cup:
                return R.string.unit_cup;
            case Unit_Tablespoon:
                return R.string.unit_tablespoon;
            case Unit_short_Tablespoon:
                return R.string.unit_tablespoon_short;
            case Unit_Can:
                return R.string.unit_can;
            default:
                return null;
        }

    }

}
