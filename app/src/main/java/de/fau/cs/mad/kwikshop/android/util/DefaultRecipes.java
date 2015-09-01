package de.fau.cs.mad.kwikshop.android.util;

import android.content.Context;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.DefaultDataProvider;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.PredefinedId;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.Unit;

public class DefaultRecipes {

    ListManager<Recipe> recipeManager;
    Context context;

    public DefaultRecipes(ListManager<Recipe> recipeManager, Context context){

        if(recipeManager == null) throw new ArgumentNullException("recipeManager");

        if(context == null) throw new ArgumentNullException("context");

        this.recipeManager = recipeManager;

        this.context = context;
    }



    public void addDefaultRecipes(){
        Unit gram = ListStorageFragment.getUnitStorage().getByName(DefaultDataProvider.UnitNames.GRAM);
        Unit cups = ListStorageFragment.getUnitStorage().getByName(DefaultDataProvider.UnitNames.CUP);
        Unit tbsp = ListStorageFragment.getUnitStorage().getByName(DefaultDataProvider.UnitNames.TABLESPOON);
        Unit cans = ListStorageFragment.getUnitStorage().getByName(DefaultDataProvider.UnitNames.CAN);
        Unit piece = ListStorageFragment.getUnitStorage().getByName(DefaultDataProvider.UnitNames.PIECE);
        Unit pack = ListStorageFragment.getUnitStorage().getByName(DefaultDataProvider.UnitNames.PACK);
        Unit teaspoon = ListStorageFragment.getUnitStorage().getByName(DefaultDataProvider.UnitNames.TEASPOON);

        Group meat = ListStorageFragment.getGroupStorage().getByName(DefaultDataProvider.GroupNames.MEAT_AND_FISH);
        Group vegetable = ListStorageFragment.getGroupStorage().getByName(DefaultDataProvider.GroupNames.FRUITS_AND_VEGETABLES);
        Group milk = ListStorageFragment.getGroupStorage().getByName(DefaultDataProvider.GroupNames.MILK_AND_CHEESE);
        Group other = ListStorageFragment.getGroupStorage().getByName(DefaultDataProvider.GroupNames.OTHER);

        {
            int id = recipeManager.createList();
            Recipe recipe1 = recipeManager.getList(id);
            recipe1.setName(context.getString(R.string.recipe_name_chili_con_carne));
            recipe1.setScaleFactor(4);
            recipe1.setScaleName(context.getString(R.string.recipe_scaleName_person));
            recipe1.setPredefinedId(PredefinedId.Recipe_ChiliConCarne.toInt());

            {
                Item item1 = new Item();
                item1.setName(context.getString(R.string.recipe_mince));
                item1.setAmount(600);
                item1.setUnit(gram);
                item1.setGroup(meat);
                item1.setPredefinedId(PredefinedId.Recipe_ChiliConCarne_Item1.toInt());
                recipe1.addItem(item1);
            }

            {
                Item item4 = new Item();
                item4.setName(context.getString(R.string.recipe_tomatoes));
                item4.setAmount(500);
                item4.setUnit(gram);
                item4.setGroup(vegetable);
                item4.setPredefinedId(PredefinedId.Recipe_ChiliConCarne_Item4.toInt());
                recipe1.addItem(item4);
            }

            {
                Item item2 = new Item();
                item2.setName(context.getString(R.string.recipe_kidney_beans));
                item2.setAmount(200);
                item2.setUnit(gram);
                item2.setGroup(vegetable);
                item2.setPredefinedId(PredefinedId.Recipe_ChiliConCarne_Item2.toInt());
                recipe1.addItem(item2);
            }

            {
                Item item5 = new Item();
                item5.setName(context.getString(R.string.recipe_corn));
                item5.setAmount(120);
                item5.setUnit(gram);
                item5.setGroup(vegetable);
                item5.setPredefinedId(PredefinedId.Recipe_ChiliConCarne_Item5.toInt());
                recipe1.addItem(item5);
            }

            {
                Item item3 = new Item();
                item3.setName(context.getString(R.string.recipe_potatoes));
                item3.setAmount(4);
                item3.setUnit(piece);
                item3.setGroup(vegetable);
                item3.setPredefinedId(PredefinedId.Recipe_ChiliConCarne_Item3.toInt());
                recipe1.addItem(item3);
            }

            {
                Item item6 = new Item();
                item6.setName(context.getString(R.string.recipe_onion));
                item6.setAmount(2);
                item6.setUnit(piece);
                item6.setGroup(vegetable);
                item6.setPredefinedId(PredefinedId.Recipe_ChiliConCarne_Item6.toInt());
                recipe1.addItem(item6);
            }


            recipeManager.saveList(id);
        }

        {
            int id = recipeManager.createList();
            Recipe recipe = recipeManager.getList(id);
            recipe.setName(context.getString(R.string.recipe_carrotCake));
            recipe.setScaleFactor(8);
            recipe.setScaleName(context.getString(R.string.recipe_scaleName_piece));
            recipe.setPredefinedId(PredefinedId.Recipe_CarrotCake.toInt());

            {
                Item item1 = new Item();
                item1.setName(context.getString(R.string.recipe_oil));
                item1.setAmount(0.5);
                item1.setUnit(cups);
                item1.setGroup(other);
                item1.setPredefinedId(PredefinedId.Recipe_CarrotCake_Item1.toInt());
                recipe.addItem(item1);
            }
            {
                Item item2 = new Item();
                item2.setName(context.getString(R.string.recipe_carrot));
                item2.setAmount(3);
                item2.setUnit(piece);
                item2.setGroup(vegetable);
                item2.setPredefinedId(PredefinedId.Recipe_CarrotCake_Item2.toInt());
                recipe.addItem(item2);
            }

            {
                Item item3 = new Item();
                item3.setName(context.getString(R.string.recipe_eggs));
                item3.setAmount(4);
                item3.setUnit(piece);
                item3.setGroup(other);
                item3.setPredefinedId(PredefinedId.Recipe_CarrotCake_Item3.toInt());
                recipe.addItem(item3);
            }
            {
                Item item4 = new Item();
                item4.setName(context.getString(R.string.recipe_sugar));
                item4.setAmount(2);
                item4.setUnit(cups);
                item4.setGroup(other);
                item4.setPredefinedId(PredefinedId.Recipe_CarrotCake_Item4.toInt());
                recipe.addItem(item4);
            }
            {
                Item item5 = new Item();
                item5.setName(context.getString(R.string.recipe_bakingPowder));
                item5.setAmount(1);
                item5.setUnit(tbsp);
                item5.setGroup(other);
                item5.setPredefinedId(PredefinedId.Recipe_CarrotCake_Item5.toInt());
                recipe.addItem(item5);
            }
            {
                Item item6 = new Item();
                item6.setName(context.getString(R.string.recipe_sweetenedCondensedMilk));
                item6.setAmount(1);
                item6.setUnit(cans);
                item6.setGroup(other);
                item6.setPredefinedId(PredefinedId.Recipe_CarrotCake_Item6.toInt());
                recipe.addItem(item6);
            }
            {
                Item item7 = new Item();
                item7.setName(context.getString(R.string.recipe_coconutFlakes));
                item7.setAmount(50);
                item7.setUnit(gram);
                item7.setGroup(other);
                item7.setPredefinedId(PredefinedId.Recipe_CarrotCake_Item7.toInt());
                recipe.addItem(item7);
            }

            recipeManager.saveList(id);
        }

        {

            int id = recipeManager.createList();
            Recipe recipe = recipeManager.getList(id);
            recipe.setName(context.getString(R.string.recipe_rhubarb_tart));
            recipe.setScaleFactor(1);
            recipe.setScaleName(context.getString(R.string.recipe_scaleName_piece));
            recipe.setPredefinedId(PredefinedId.Recipe_RhubarbTart.toInt());

            {
                Item item1 = new Item();
                item1.setName(context.getString(R.string.recipe_rhubarb));
                item1.setAmount(1500);
                item1.setUnit(gram);
                item1.setGroup(vegetable);
                item1.setPredefinedId(PredefinedId.Recipe_RhubarbTart_Item1.toInt());
                recipe.addItem(item1);
            }

            {
                Item item2 = new Item();
                item2.setName(context.getString(R.string.recipe_lowfat_quark));
                item2.setAmount(750);
                item2.setUnit(gram);
                item2.setGroup(other);
                item2.setPredefinedId(PredefinedId.Recipe_RhubarbTart_Item2.toInt());
                recipe.addItem(item2);
            }

            {
                //TODO: group
                Item item3 = new Item();
                item3.setName(context.getString(R.string.recipe_eggs));
                item3.setAmount(4);
                item3.setUnit(piece);
                item3.setGroup(other);
                item3.setPredefinedId(PredefinedId.Recipe_RhubarbTart_Item3.toInt());
                recipe.addItem(item3);
            }
            {
                //Todo: group
                Item item4 = new Item();
                item4.setName(context.getString(R.string.recipe_vanilla_sugar));
                item4.setAmount(2);
                item4.setUnit(pack);
                item4.setGroup(other);
                item4.setPredefinedId(PredefinedId.Recipe_RhubarbTart_Item4.toInt());
                recipe.addItem(item4);
            }
            {
                Item item5 = new Item();
                item5.setName(context.getString(R.string.recipe_butter));
                item5.setAmount(275);
                item5.setUnit(gram);
                item5.setGroup(milk);
                item5.setPredefinedId(PredefinedId.Recipe_RhubarbTart_Item5.toInt());
                recipe.addItem(item5);
            }

            {
                //Todo: group
                Item item6 = new Item();
                item6.setName(context.getString(R.string.recipe_sugar));
                item6.setAmount(275);
                item6.setUnit(gram);
                item6.setPredefinedId(PredefinedId.Recipe_RhubarbTart_Item6.toInt());
                recipe.addItem(item6);
            }
            {
                Item item7 = new Item();
                item7.setName(context.getString(R.string.recipe_plainFlour));
                item7.setAmount(175);
                item7.setUnit(gram);
                item7.setGroup(other);
                item7.setPredefinedId(PredefinedId.Recipe_RhubarbTart_Item7.toInt());
                recipe.addItem(item7);
            }
            {
                Item item8 = new Item();
                item8.setName(context.getString(R.string.recipe_starch));
                item8.setAmount(170);
                item8.setGroup(other);
                item8.setPredefinedId(PredefinedId.Recipe_RhubarbTart_Item8.toInt());
                item8.setUnit(gram);
                recipe.addItem(item8);
            }
            {
                Item item9 = new Item();
                item9.setName(context.getString(R.string.recipe_bakingPowder));
                item9.setAmount(3);
                item9.setUnit(teaspoon);
                item9.setGroup(other);
                item9.setPredefinedId(PredefinedId.Recipe_RhubarbTart_Item9.toInt());
                recipe.addItem(item9);
            }

            recipeManager.saveList(id);
        }

        {
            int id = recipeManager.createList();
            Recipe recipe = recipeManager.getList(id);
            recipe.setName(context.getString(R.string.recipe_madeira_cake));
            recipe.setScaleFactor(1);
            recipe.setScaleName(context.getString(R.string.recipe_scaleName_piece));
            recipe.setPredefinedId(PredefinedId.Recipe_MadeiraCake.toInt());

            {
                Item item1 = new Item();
                item1.setName(context.getString(R.string.recipe_butter));
                item1.setAmount(250);
                item1.setUnit(gram);
                item1.setGroup(milk);
                item1.setPredefinedId(PredefinedId.Recipe_MadeiraCake_Item1.toInt());
                recipe.addItem(item1);
            }
            {
                Item item2 = new Item();
                item2.setName(context.getString(R.string.recipe_sugar));
                item2.setAmount(200);
                item2.setUnit(gram);
                item2.setGroup(other);
                item2.setPredefinedId(PredefinedId.Recipe_MadeiraCake_Item2.toInt());
                recipe.addItem(item2);
            }
            {
                Item item3 = new Item();
                item3.setName(context.getString(R.string.recipe_plainFlour));
                item3.setAmount(125);
                item3.setUnit(gram);
                item3.setGroup(other);
                item3.setPredefinedId(PredefinedId.Recipe_MadeiraCake_Item3.toInt());
                recipe.addItem(item3);
            }
            {
                Item item4 = new Item();
                item4.setName(context.getString(R.string.recipe_starch));
                item4.setAmount(125);
                item4.setUnit(gram);
                item4.setGroup(other);
                item4.setPredefinedId(PredefinedId.Recipe_MadeiraCake_Item4.toInt());
                recipe.addItem(item4);
            }
            {
                Item item5 = new Item();
                item5.setName(context.getString(R.string.recipe_vanilla_sugar));
                item5.setAmount(1);
                item5.setUnit(pack);
                item5.setGroup(other);
                item5.setPredefinedId(PredefinedId.Recipe_MadeiraCake_Item5.toInt());
                recipe.addItem(item5);
            }
            {
                Item item6 = new Item();
                item6.setName(context.getString(R.string.recipe_bakingPowder));
                item6.setAmount(0.5);
                item6.setUnit(teaspoon);
                item6.setGroup(other);
                item6.setPredefinedId(PredefinedId.Recipe_MadeiraCake_Item6.toInt());
                recipe.addItem(item6);
            }
            {
                Item item7 = new Item();
                item7.setName(context.getString(R.string.recipe_salt));
                item7.setAmount(0.5);
                item7.setUnit(teaspoon);
                item7.setGroup(other);
                item7.setPredefinedId(PredefinedId.Recipe_MadeiraCake_Item7.toInt());
                recipe.addItem(item7);
            }

            recipeManager.saveList(id);
        }
    }

}
