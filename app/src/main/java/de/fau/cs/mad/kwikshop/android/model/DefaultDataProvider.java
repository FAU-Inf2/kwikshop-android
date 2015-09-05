package de.fau.cs.mad.kwikshop.android.model;

import android.content.Context;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.PredefinedId;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.localization.ResourceId;

public class DefaultDataProvider {

    public static class GroupNames {

        public static final String OTHER = "Other";
        public static final String COFFEE_AND_TEA = "Coffee & Tea";
        public static final String HEALTH_AND_HYGIENE = "Health & Hygiene";
        public static final String PET_SUPPLIES = "Pet Supplies";
        public static final String HOUSEHOLD ="Household";
        public static final String BREAD_AND_PASTRIES = "Bread and Pastries";
        public static final String BEVERAGES = "Beverages";
        public static final String SWEETS_AND_SNACKS = "Sweets & Snacks";
        public static final String BABY_FOODS = "Baby Foods";
        public static final String PASTA ="Pasta";
        public static final String MILK_AND_CHEESE = "Milk & Cheese";
        public static final String FRUITS_AND_VEGETABLES = "Fruits & Vegetables";
        public static final String MEAT_AND_FISH = "Meat & Fish";
        public static final String INGREDIENTS_AND_SPICES = "Ingredients & Spices";
        public static final String FROZEN_AND_CONVENIENCE = "Frozen & Convenience";
        public static final String TOBACCO = "Tobacco";
    }

    public static class UnitNames {

        public static final String PIECE = "piece";
        public static final String BAG = "bag";
        public static final String BOTTLE = "bottle";
        public static final String BOX = "box";
        public static final String PACK = "pack";
        public static final String DOZEN = "dozen";
        public static final String GRAM = "gram";
        public static final String KILOGRAM = "kilogram";
        public static final String MILLILITRE = "millilitre";
        public static final String LITRE = "litre";
        public static final String CUP = "cup";
        public static final String TABLESPOON = "tablespoon";
        public static final String TEASPOON = "teaspoon";
        public static final String CAN = "can";

    }


    static Unit[] defaultUnits;
    static Unit[] defaultSingleUnits;
    static Group[] defaultGroups;


    @Inject
    public DefaultDataProvider() {

    }


    public static String getDefaultUnitName() {
        return UnitNames.PIECE;
    }

    public Unit[] getPredefinedUnits() {

        if (defaultUnits == null) {
            defaultUnits = new Unit[]{
                    new Unit(UnitNames.PIECE, ResourceId.Unit_Piece, ResourceId.Unit_short_Piece, PredefinedId.Unit_Piece),
                    new Unit(UnitNames.BAG, ResourceId.Unit_Bag, PredefinedId.Unit_Bag),
                    new Unit(UnitNames.BOTTLE, ResourceId.Unit_Bottle, PredefinedId.Unit_Bottle),
                    new Unit(UnitNames.BOX, ResourceId.Unit_Box, PredefinedId.Unit_Box),
                    new Unit(UnitNames.PACK, ResourceId.Unit_Pack, PredefinedId.Unit_Pack),
                    new Unit(UnitNames.DOZEN, ResourceId.Unit_Dozen, PredefinedId.Unit_Dozen),
                    new Unit(UnitNames.GRAM, ResourceId.Unit_Gram, ResourceId.Unit_short_Gram, PredefinedId.Unit_Gram),
                    new Unit(UnitNames.KILOGRAM, ResourceId.Unit_Kilogram, ResourceId.Unit_short_Kilogram, PredefinedId.Unit_Kilogram),
                    new Unit(UnitNames.MILLILITRE, ResourceId.Unit_Millilitre, ResourceId.Unit_short_Millilitre, PredefinedId.Unit_Millilitre),
                    new Unit(UnitNames.LITRE, ResourceId.Unit_Litre, ResourceId.Unit_short_Litre, PredefinedId.Unit_Litre),
                    new Unit(UnitNames.CUP, ResourceId.Unit_Cup, PredefinedId.Unit_Cup),
                    new Unit(UnitNames.TABLESPOON, ResourceId.Unit_Tablespoon, ResourceId.Unit_short_Tablespoon, PredefinedId.Unit_Tablespoon),
                    new Unit(UnitNames.CAN, ResourceId.Unit_Can, PredefinedId.Unit_Can),
                    new Unit(UnitNames.TEASPOON, ResourceId.Unit_Teaspoon, ResourceId.Unit_short_Teaspoon, PredefinedId.Unit_Teaspoon)
            };
        }

        return defaultUnits;

    }

    public Unit[] getSingularPredefinedUnits() {
        if (defaultSingleUnits == null){
            defaultSingleUnits = new Unit[]{
                    new Unit(UnitNames.PIECE, ResourceId.Unit_Piece_singular, ResourceId.Unit_short_Piece, PredefinedId.Unit_Piece),
                    new Unit(UnitNames.BAG, ResourceId.Unit_Bag_singular, PredefinedId.Unit_Bag),
                    new Unit(UnitNames.BOTTLE, ResourceId.Unit_Bottle_singular, PredefinedId.Unit_Bottle),
                    new Unit(UnitNames.BOX, ResourceId.Unit_Box_singular, PredefinedId.Unit_Box),
                    new Unit(UnitNames.PACK, ResourceId.Unit_Pack_singular, PredefinedId.Unit_Pack),
                    new Unit(UnitNames.DOZEN, ResourceId.Unit_Dozen_singular, PredefinedId.Unit_Dozen),
                    new Unit(UnitNames.GRAM, ResourceId.Unit_Gram_singular, ResourceId.Unit_short_Gram, PredefinedId.Unit_Gram),
                    new Unit(UnitNames.KILOGRAM, ResourceId.Unit_Kilogram_singular, ResourceId.Unit_short_Kilogram, PredefinedId.Unit_Kilogram),
                    new Unit(UnitNames.MILLILITRE, ResourceId.Unit_Millilitre_singular, ResourceId.Unit_short_Millilitre, PredefinedId.Unit_Millilitre),
                    new Unit(UnitNames.LITRE, ResourceId.Unit_Litre_singular, ResourceId.Unit_short_Litre, PredefinedId.Unit_Litre),
                    new Unit(UnitNames.CUP, ResourceId.Unit_Cup_singular, PredefinedId.Unit_Cup),
                    new Unit(UnitNames.TABLESPOON, ResourceId.Unit_Tablespoon_singular, ResourceId.Unit_short_Tablespoon, PredefinedId.Unit_Tablespoon),
                    new Unit(UnitNames.CAN, ResourceId.Unit_Can_singular, PredefinedId.Unit_Can),
                    new Unit(UnitNames.TEASPOON, ResourceId.Unit_Teaspoon_singular, ResourceId.Unit_short_Teaspoon, PredefinedId.Unit_Teaspoon)
            };
        }
        return defaultSingleUnits;
    }

    public static String getDefaultGroupName() {
        return GroupNames.OTHER;
    }

    public Group[] getPredefinedGroups() {

        if (defaultGroups == null) {
            defaultGroups = new Group[]{
                    new Group(GroupNames.COFFEE_AND_TEA, ResourceId.Group_CoffeeAndTea, PredefinedId.Group_CoffeeAndTea),
                    new Group(GroupNames.HEALTH_AND_HYGIENE, ResourceId.Group_HealthAndHygiene,PredefinedId.Group_HealthAndHygiene),
                    new Group(GroupNames.PET_SUPPLIES, ResourceId.Group_PetSupplies,PredefinedId.Group_PetSupplies),
                    new Group(GroupNames.HOUSEHOLD, ResourceId.Group_Household,PredefinedId.Group_Household),
                    new Group(GroupNames.BREAD_AND_PASTRIES, ResourceId.Group_BreadAndPastries,PredefinedId.Group_BreadAndPastries),
                    new Group(GroupNames.BEVERAGES, ResourceId.Group_Beverages,PredefinedId.Group_Beverages),
                    new Group(GroupNames.SWEETS_AND_SNACKS, ResourceId.Group_SweetsAndSnacks, PredefinedId.Group_SweetsAndSnacks),
                    new Group(GroupNames.BABY_FOODS, ResourceId.Group_BabyFoods, PredefinedId.Group_BabyFoods),
                    new Group(GroupNames.PASTA, ResourceId.Group_Pasta, PredefinedId.Group_Pasta),
                    new Group(GroupNames.MILK_AND_CHEESE, ResourceId.Group_Dairy, PredefinedId.Group_Dairy),
                    new Group(GroupNames.FRUITS_AND_VEGETABLES, ResourceId.Group_FruitsAndVegetables, PredefinedId.Group_FruitsAndVegetables),
                    new Group(GroupNames.MEAT_AND_FISH, ResourceId.Group_MeatAndFish, PredefinedId.Group_MeatAndFish),
                    new Group(GroupNames.INGREDIENTS_AND_SPICES, ResourceId.Group_IngredientsAndSpices, PredefinedId.Group_IngredientsAndSpices),
                    new Group(GroupNames.FROZEN_AND_CONVENIENCE, ResourceId.Group_FrozenAndConvenience, PredefinedId.Group_FrozenAndConvenience),
                    new Group(GroupNames.TOBACCO, ResourceId.Group_Tobacco, PredefinedId.Group_Tobacco),
                    new Group(GroupNames.OTHER, ResourceId.Group_Other, PredefinedId.Group_Other)
            };
        }

        return defaultGroups;
    }




}
