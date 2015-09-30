package de.fau.cs.mad.kwikshop.android.util;

import android.content.Context;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.DefaultDataProvider;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.PredefinedId;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.Unit;

public class DefaultRecipesHelper {

    ListManager<Recipe> recipeManager;
    Context context;

    @Inject
    public DefaultRecipesHelper(ListManager<Recipe> recipeManager, Context context){

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
        Unit tablespoon = ListStorageFragment.getUnitStorage().getByName(DefaultDataProvider.UnitNames.TABLESPOON);
        Unit ml = ListStorageFragment.getUnitStorage().getByName(DefaultDataProvider.UnitNames.MILLILITRE);

        Group meat = ListStorageFragment.getGroupStorage().getByName(DefaultDataProvider.GroupNames.MEAT_AND_FISH);
        Group vegetable = ListStorageFragment.getGroupStorage().getByName(DefaultDataProvider.GroupNames.FRUITS_AND_VEGETABLES);
        Group milk = ListStorageFragment.getGroupStorage().getByName(DefaultDataProvider.GroupNames.MILK_AND_CHEESE);
        Group other = ListStorageFragment.getGroupStorage().getByName(DefaultDataProvider.GroupNames.OTHER);
        Group pasta = ListStorageFragment.getGroupStorage().getByName(DefaultDataProvider.GroupNames.PASTA);
        Group spices =  ListStorageFragment.getGroupStorage().getByName(DefaultDataProvider.GroupNames.INGREDIENTS_AND_SPICES);
        Group fruit = ListStorageFragment.getGroupStorage().getByName(DefaultDataProvider.GroupNames.FRUITS_AND_VEGETABLES);
        Group pastries = ListStorageFragment.getGroupStorage().getByName(DefaultDataProvider.GroupNames.BREAD_AND_PASTRIES);

        /**
         * Default Recipes:
         *
         * - chili con carne
         * - carrot cake
         * - rhubarb tart
         * - madeira cake
         * - spaghetti bolognese
         * - cherry cake
         **/

        //region Recipe: Spaghetti Bolognese
        {
            int recipeId = recipeManager.createList();
            Recipe spaghetti = recipeManager.getList(recipeId);
            spaghetti.setName(context.getString(R.string.recipe_name_spaghetti_bolognese));
            spaghetti.setScaleFactor(4);
            spaghetti.setScaleName(context.getString(R.string.recipe_scaleName_person));
            spaghetti.setPredefinedId(PredefinedId.Recipe_Spaghetti_Bolognese.toInt());

            {
                Item item1 = new Item();
                item1.setName(context.getString(R.string.recipe_mince));
                item1.setAmount(500);
                item1.setUnit(gram);
                item1.setGroup(meat);
                item1.setPredefinedId(PredefinedId.Recipe_Spaghetti_Bolognese_Item1.toInt());
                recipeManager.addListItem(recipeId, item1);
            }

            {
                Item item2 = new Item();
                item2.setName(context.getString(R.string.recipe_tomatoes));
                item2.setAmount(400);
                item2.setUnit(gram);
                item2.setGroup(vegetable);
                item2.setPredefinedId(PredefinedId.Recipe_Spaghetti_Bolognese_Item2.toInt());
                recipeManager.addListItem(recipeId, item2);
            }

            {
                Item item3 = new Item();
                item3.setName(context.getString(R.string.recipe_pasta));
                item3.setAmount(500);
                item3.setUnit(gram);
                item3.setGroup(pasta);
                item3.setPredefinedId(PredefinedId.Recipe_Spaghetti_Bolognese_Item3.toInt());
                recipeManager.addListItem(recipeId, item3);
            }

            {
                Item item4 = new Item();
                item4.setName(context.getString(R.string.recipe_onion));
                item4.setAmount(1);
                item4.setUnit(piece);
                item4.setGroup(vegetable);
                item4.setPredefinedId(PredefinedId.Recipe_Spaghetti_Bolognese_Item4.toInt());
                recipeManager.addListItem(recipeId, item4);
            }

            {
                Item item5 = new Item();
                item5.setName(context.getString(R.string.recipe_clove_of_garlic));
                item5.setAmount(1);
                item5.setUnit(piece);
                item5.setGroup(vegetable);
                item5.setPredefinedId(PredefinedId.Recipe_Spaghetti_Bolognese_Item5.toInt());
                recipeManager.addListItem(recipeId, item5);
            }

            {
                Item item6 = new Item();
                item6.setName(context.getString(R.string.recipe_carrot));
                item6.setAmount(1);
                item6.setUnit(piece);
                item6.setGroup(vegetable);
                item6.setPredefinedId(PredefinedId.Recipe_Spaghetti_Bolognese_Item6.toInt());
                recipeManager.addListItem(recipeId, item6);

            }
            {
                Item item7 = new Item();
                item7.setName(context.getString(R.string.recipe_ketchup));
                item7.setAmount(2);
                item7.setUnit(tbsp);
                item7.setGroup(other);
                item7.setPredefinedId(PredefinedId.Recipe_Spaghetti_Bolognese_Item7.toInt());
                recipeManager.addListItem(recipeId, item7);
            }
            {
                Item item8 = new Item();
                item8.setName(context.getString(R.string.recipe_oregano));
                item8.setAmount(2);
                item8.setUnit(teaspoon);
                item8.setGroup(spices);
                item8.setPredefinedId(PredefinedId.Recipe_Spaghetti_Bolognese_Item8.toInt());
                recipeManager.addListItem(recipeId, item8);
            }
            {
                Item item9 = new Item();
                item9.setName(context.getString(R.string.recipe_ketchup));
                item9.setAmount(1);
                item9.setUnit(piece);
                item9.setGroup(vegetable);
                item9.setPredefinedId(PredefinedId.Recipe_Spaghetti_Bolognese_Item6.toInt());
                recipeManager.addListItem(recipeId, item9);
            }
            {
                Item item10 = new Item();
                item10.setName(context.getString(R.string.recipe_tomato_paste));
                item10.setAmount(1);
                item10.setUnit(cans);
                item10.setGroup(vegetable);
                item10.setPredefinedId(PredefinedId.Recipe_Spaghetti_Bolognese_Item10.toInt());
                recipeManager.addListItem(recipeId, item10);
            }
            {
                Item item11 = new Item();
                item11.setName(context.getString(R.string.recipe_vegetable_broth));
                item11.setAmount(200);
                item11.setUnit(ml);
                item11.setGroup(vegetable);
                item11.setPredefinedId(PredefinedId.Recipe_Spaghetti_Bolognese_Item11.toInt());
                recipeManager.addListItem(recipeId, item11);

            }


            recipeManager.saveList(recipeId);
        }

        //endregion

        //region Recipe: Chili con Carne
        {
            int recipeId = recipeManager.createList();
            Recipe recipe1 = recipeManager.getList(recipeId);
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
                recipeManager.addListItem(recipeId, item1);
            }

            {
                Item item4 = new Item();
                item4.setName(context.getString(R.string.recipe_tomatoes));
                item4.setAmount(500);
                item4.setUnit(gram);
                item4.setGroup(vegetable);
                item4.setPredefinedId(PredefinedId.Recipe_ChiliConCarne_Item4.toInt());
                recipeManager.addListItem(recipeId, item4);
            }

            {
                Item item2 = new Item();
                item2.setName(context.getString(R.string.recipe_kidney_beans));
                item2.setAmount(200);
                item2.setUnit(gram);
                item2.setGroup(vegetable);
                item2.setPredefinedId(PredefinedId.Recipe_ChiliConCarne_Item2.toInt());
                recipeManager.addListItem(recipeId, item2);
            }

            {
                Item item5 = new Item();
                item5.setName(context.getString(R.string.recipe_corn));
                item5.setAmount(120);
                item5.setUnit(gram);
                item5.setGroup(vegetable);
                item5.setPredefinedId(PredefinedId.Recipe_ChiliConCarne_Item5.toInt());
                recipeManager.addListItem(recipeId, item5);
            }

            {
                Item item3 = new Item();
                item3.setName(context.getString(R.string.recipe_potatoes));
                item3.setAmount(4);
                item3.setUnit(piece);
                item3.setGroup(vegetable);
                item3.setPredefinedId(PredefinedId.Recipe_ChiliConCarne_Item3.toInt());
                recipeManager.addListItem(recipeId, item3);
            }

            {
                Item item6 = new Item();
                item6.setName(context.getString(R.string.recipe_onion));
                item6.setAmount(2);
                item6.setUnit(piece);
                item6.setGroup(vegetable);
                item6.setPredefinedId(PredefinedId.Recipe_ChiliConCarne_Item6.toInt());
                recipeManager.addListItem(recipeId, item6);
            }

            recipeManager.saveList(recipeId);
        }

        //endregion

        //region Recipe: Carrot Cake
        {
            int recipeId = recipeManager.createList();
            Recipe recipe = recipeManager.getList(recipeId);
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
                recipeManager.addListItem(recipeId, item1);
            }
            {
                Item item2 = new Item();
                item2.setName(context.getString(R.string.recipe_carrot));
                item2.setAmount(3);
                item2.setUnit(piece);
                item2.setGroup(vegetable);
                item2.setPredefinedId(PredefinedId.Recipe_CarrotCake_Item2.toInt());
                recipeManager.addListItem(recipeId, item2);
            }

            {
                Item item3 = new Item();
                item3.setName(context.getString(R.string.recipe_eggs));
                item3.setAmount(4);
                item3.setUnit(piece);
                item3.setGroup(other);
                item3.setPredefinedId(PredefinedId.Recipe_CarrotCake_Item3.toInt());
                recipeManager.addListItem(recipeId, item3);
            }
            {
                Item item4 = new Item();
                item4.setName(context.getString(R.string.recipe_sugar));
                item4.setAmount(2);
                item4.setUnit(cups);
                item4.setGroup(other);
                item4.setPredefinedId(PredefinedId.Recipe_CarrotCake_Item4.toInt());
                recipeManager.addListItem(recipeId, item4);
            }
            {
                Item item5 = new Item();
                item5.setName(context.getString(R.string.recipe_bakingPowder));
                item5.setAmount(1);
                item5.setUnit(tbsp);
                item5.setGroup(other);
                item5.setPredefinedId(PredefinedId.Recipe_CarrotCake_Item5.toInt());
                recipeManager.addListItem(recipeId, item5);
            }
            {
                Item item6 = new Item();
                item6.setName(context.getString(R.string.recipe_sweetenedCondensedMilk));
                item6.setAmount(1);
                item6.setUnit(cans);
                item6.setGroup(other);
                item6.setPredefinedId(PredefinedId.Recipe_CarrotCake_Item6.toInt());
                recipeManager.addListItem(recipeId, item6);
            }
            {
                Item item7 = new Item();
                item7.setName(context.getString(R.string.recipe_coconutFlakes));
                item7.setAmount(50);
                item7.setUnit(gram);
                item7.setGroup(other);
                item7.setPredefinedId(PredefinedId.Recipe_CarrotCake_Item7.toInt());
                recipeManager.addListItem(recipeId, item7);
            }

            recipeManager.saveList(recipeId);
        }

        //endregion

        //region Recipe: Rhubard Tart
        {
            int recipeId = recipeManager.createList();
            Recipe recipe = recipeManager.getList(recipeId);
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
                recipeManager.addListItem(recipeId, item1);
            }

            {
                Item item2 = new Item();
                item2.setName(context.getString(R.string.recipe_lowfat_quark));
                item2.setAmount(750);
                item2.setUnit(gram);
                item2.setGroup(other);
                item2.setPredefinedId(PredefinedId.Recipe_RhubarbTart_Item2.toInt());
                recipeManager.addListItem(recipeId, item2);
            }

            {
                //TODO: group
                Item item3 = new Item();
                item3.setName(context.getString(R.string.recipe_eggs));
                item3.setAmount(4);
                item3.setUnit(piece);
                item3.setGroup(other);
                item3.setPredefinedId(PredefinedId.Recipe_RhubarbTart_Item3.toInt());
                recipeManager.addListItem(recipeId, item3);
            }
            {
                //Todo: group
                Item item4 = new Item();
                item4.setName(context.getString(R.string.recipe_vanilla_sugar));
                item4.setAmount(2);
                item4.setUnit(pack);
                item4.setGroup(other);
                item4.setPredefinedId(PredefinedId.Recipe_RhubarbTart_Item4.toInt());
                recipeManager.addListItem(recipeId, item4);
            }
            {
                Item item5 = new Item();
                item5.setName(context.getString(R.string.recipe_butter));
                item5.setAmount(275);
                item5.setUnit(gram);
                item5.setGroup(milk);
                item5.setPredefinedId(PredefinedId.Recipe_RhubarbTart_Item5.toInt());
                recipeManager.addListItem(recipeId, item5);
            }

            {
                //Todo: group
                Item item6 = new Item();
                item6.setName(context.getString(R.string.recipe_sugar));
                item6.setAmount(275);
                item6.setUnit(gram);
                item6.setPredefinedId(PredefinedId.Recipe_RhubarbTart_Item6.toInt());
                recipeManager.addListItem(recipeId, item6);
            }
            {
                Item item7 = new Item();
                item7.setName(context.getString(R.string.recipe_plainFlour));
                item7.setAmount(175);
                item7.setUnit(gram);
                item7.setGroup(other);
                item7.setPredefinedId(PredefinedId.Recipe_RhubarbTart_Item7.toInt());
                recipeManager.addListItem(recipeId, item7);
            }
            {
                Item item8 = new Item();
                item8.setName(context.getString(R.string.recipe_starch));
                item8.setAmount(170);
                item8.setGroup(other);
                item8.setPredefinedId(PredefinedId.Recipe_RhubarbTart_Item8.toInt());
                item8.setUnit(gram);
                recipeManager.addListItem(recipeId, item8);
            }
            {
                Item item9 = new Item();
                item9.setName(context.getString(R.string.recipe_bakingPowder));
                item9.setAmount(3);
                item9.setUnit(teaspoon);
                item9.setGroup(other);
                item9.setPredefinedId(PredefinedId.Recipe_RhubarbTart_Item9.toInt());
                recipeManager.addListItem(recipeId, item9);
            }

            recipeManager.saveList(recipeId);
        }

        //endregion

        //region Recipe: Madeira Cake
        {
            int recipeId = recipeManager.createList();
            Recipe recipe = recipeManager.getList(recipeId);
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
                recipeManager.addListItem(recipeId, item1);
            }
            {
                Item item2 = new Item();
                item2.setName(context.getString(R.string.recipe_sugar));
                item2.setAmount(200);
                item2.setUnit(gram);
                item2.setGroup(other);
                item2.setPredefinedId(PredefinedId.Recipe_MadeiraCake_Item2.toInt());
                recipeManager.addListItem(recipeId, item2);
            }
            {
                Item item3 = new Item();
                item3.setName(context.getString(R.string.recipe_plainFlour));
                item3.setAmount(125);
                item3.setUnit(gram);
                item3.setGroup(other);
                item3.setPredefinedId(PredefinedId.Recipe_MadeiraCake_Item3.toInt());
                recipeManager.addListItem(recipeId, item3);
            }
            {
                Item item4 = new Item();
                item4.setName(context.getString(R.string.recipe_starch));
                item4.setAmount(125);
                item4.setUnit(gram);
                item4.setGroup(other);
                item4.setPredefinedId(PredefinedId.Recipe_MadeiraCake_Item4.toInt());
                recipeManager.addListItem(recipeId, item4);
            }
            {
                Item item5 = new Item();
                item5.setName(context.getString(R.string.recipe_vanilla_sugar));
                item5.setAmount(1);
                item5.setUnit(pack);
                item5.setGroup(other);
                item5.setPredefinedId(PredefinedId.Recipe_MadeiraCake_Item5.toInt());
                recipeManager.addListItem(recipeId, item5);
            }
            {
                Item item6 = new Item();
                item6.setName(context.getString(R.string.recipe_bakingPowder));
                item6.setAmount(0.5);
                item6.setUnit(teaspoon);
                item6.setGroup(other);
                item6.setPredefinedId(PredefinedId.Recipe_MadeiraCake_Item6.toInt());
                recipeManager.addListItem(recipeId, item6);
            }
            {
                Item item7 = new Item();
                item7.setName(context.getString(R.string.recipe_salt));
                item7.setAmount(0.5);
                item7.setUnit(teaspoon);
                item7.setGroup(other);
                item7.setPredefinedId(PredefinedId.Recipe_MadeiraCake_Item7.toInt());
                recipeManager.addListItem(recipeId, item7);
            }

            recipeManager.saveList(recipeId);
        }

        //endregion

        //region Recipe: Cherry Cake
        {
            int recipeId = recipeManager.createList();
            Recipe recipe = recipeManager.getList(recipeId);
            recipe.setName(context.getString(R.string.recipe_cherry_cake));
            recipe.setScaleFactor(1);
            recipe.setScaleName(context.getString(R.string.recipe_scaleName_piece));
            recipe.setPredefinedId(PredefinedId.Recipe_CherryCake.toInt());

            {
                Item item = new Item();
                item.setName(context.getString(R.string.recipe_butter));
                item.setAmount(200);
                item.setUnit(gram);
                item.setGroup(other);
                item.setPredefinedId(PredefinedId.Recipe_CherryCake_Item1.toInt());
                recipeManager.addListItem(recipeId, item);
            }
            {
                Item item = new Item();
                item.setName(context.getString(R.string.recipe_sugar));
                item.setAmount(175);
                item.setUnit(gram);
                item.setGroup(other);
                item.setPredefinedId(PredefinedId.Recipe_CherryCake_Item2.toInt());
                recipeManager.addListItem(recipeId, item);
            }
            {
                Item item = new Item();
                item.setName(context.getString(R.string.recipe_vanilla_sugar));
                item.setAmount(1);
                item.setUnit(pack);
                item.setGroup(other);
                item.setPredefinedId(PredefinedId.Recipe_CherryCake_Item3.toInt());
                recipeManager.addListItem(recipeId, item);
            }
            {
                Item item = new Item();
                item.setName(context.getString(R.string.recipe_eggs));
                item.setAmount(3);
                item.setUnit(piece);
                item.setGroup(other);
                item.setPredefinedId(PredefinedId.Recipe_CherryCake_Item4.toInt());
                recipeManager.addListItem(recipeId, item);
            }
            {
                Item item = new Item();
                item.setName(context.getString(R.string.recipe_plainFlour));
                item.setAmount(200);
                item.setUnit(gram);
                item.setGroup(other);
                item.setPredefinedId(PredefinedId.Recipe_CherryCake_Item5.toInt());
                recipeManager.addListItem(recipeId, item);
            }
            {
                Item item = new Item();
                item.setName(context.getString(R.string.recipe_bakingPowder));
                item.setAmount(2);
                item.setUnit(teaspoon);
                item.setGroup(other);
                item.setPredefinedId(PredefinedId.Recipe_CherryCake_Item6.toInt());
                recipeManager.addListItem(recipeId, item);
            }
            {
                Item item = new Item();
                item.setName(context.getString(R.string.recipe_milk));
                item.setAmount(2);
                item.setUnit(tablespoon);
                item.setGroup(other);
                item.setPredefinedId(PredefinedId.Recipe_CherryCake_Item7.toInt());
                recipeManager.addListItem(recipeId, item);
            }

            recipeManager.saveList(recipeId);
        }

        //endregion Recipe: Cherry Cake

        //region Recipe: Strawberry Cake
        {
            int recipeId = recipeManager.createList();
            Recipe recipe = recipeManager.getList(recipeId);
            recipe.setName(context.getString(R.string.recipe_strawberry_cake));
            recipe.setScaleFactor(1);
            recipe.setScaleName(context.getString(R.string.recipe_scaleName_piece));
            recipe.setPredefinedId(PredefinedId.Recipe_StrawberryCake.toInt());

            {
                Item item = new Item();
                item.setName(context.getString(R.string.recipe_strawberrys));
                item.setAmount(500);
                item.setUnit(gram);
                item.setGroup(fruit);
                item.setPredefinedId(PredefinedId.Recipe_StrawberryCake_Item1.toInt());
                recipeManager.addListItem(recipeId, item);
            }
            {
                Item item = new Item();
                item.setName(context.getString(R.string.recipe_baked_pastry_case));
                item.setAmount(1);
                item.setUnit(piece);
                item.setGroup(pastries);
                item.setPredefinedId(PredefinedId.Recipe_StrawberryCake_Item2.toInt());
                recipeManager.addListItem(recipeId, item);
            }
            {
                Item item = new Item();
                item.setName(context.getString(R.string.recipe_cake_glaze));
                item.setAmount(1);
                item.setUnit(pack);
                item.setGroup(other);
                item.setPredefinedId(PredefinedId.Recipe_StrawberryCake_Item3.toInt());
                recipeManager.addListItem(recipeId, item);
            }

            recipeManager.saveList(recipeId);
        }
        //endregion Recipe: Strawberry Cake


        //region Recipe: Pineapple Cake
        {
            int recipeId = recipeManager.createList();
            Recipe recipe = recipeManager.getList(recipeId);
            recipe.setName(context.getString(R.string.recipe_pineapple_cake));
            recipe.setScaleFactor(1);
            recipe.setScaleName(context.getString(R.string.recipe_scaleName_piece));
            recipe.setPredefinedId(PredefinedId.Recipe_PineappleCake.toInt());

            {
                Item item = new Item();
                item.setName(context.getString(R.string.recipe_pineapples));
                item.setAmount(800);
                item.setUnit(gram);
                item.setGroup(fruit);
                item.setPredefinedId(PredefinedId.Recipe_PineappleCake_Item1.toInt());
                recipeManager.addListItem(recipeId, item);
            }
            {
                Item item = new Item();
                item.setName(context.getString(R.string.recipe_baked_pastry_case));
                item.setAmount(1);
                item.setUnit(piece);
                item.setGroup(pastries);
                item.setPredefinedId(PredefinedId.Recipe_PineappleCake_Item2.toInt());
                recipeManager.addListItem(recipeId, item);
            }
            {
                Item item = new Item();
                item.setName(context.getString(R.string.recipe_cake_glaze));
                item.setAmount(1);
                item.setUnit(pack);
                item.setGroup(other);
                item.setPredefinedId(PredefinedId.Recipe_PineappleCake_Item3.toInt());
                recipeManager.addListItem(recipeId, item);
            }

            recipeManager.saveList(recipeId);
        }
        //endregion Recipe: Pineapple Cake


        //region Recipe: Apple Pie
        {
            int recipeId = recipeManager.createList();
            Recipe recipe = recipeManager.getList(recipeId);
            recipe.setName(context.getString(R.string.recipe_apple_pie));
            recipe.setScaleFactor(1);
            recipe.setScaleName(context.getString(R.string.recipe_scaleName_piece));
            recipe.setPredefinedId(PredefinedId.Recipe_Apple_Pie.toInt());

            {
                Item item = new Item();
                item.setName(context.getString(R.string.recipe_butter));
                item.setAmount(350);
                item.setUnit(gram);
                item.setGroup(other);
                item.setPredefinedId(PredefinedId.Recipe_Apple_Pie_Item1.toInt());
                recipeManager.addListItem(recipeId, item);
            }
            {
                Item item = new Item();
                item.setName(context.getString(R.string.recipe_sugar));
                item.setAmount(350);
                item.setUnit(gram);
                item.setGroup(other);
                item.setPredefinedId(PredefinedId.Recipe_Apple_Pie_Item2.toInt());
                recipeManager.addListItem(recipeId, item);
            }
            {
                Item item = new Item();
                item.setName(context.getString(R.string.recipe_eggs));
                item.setAmount(3);
                item.setUnit(piece);
                item.setGroup(other);
                item.setPredefinedId(PredefinedId.Recipe_Apple_Pie_Item3.toInt());
                recipeManager.addListItem(recipeId, item);
            }
            {
                Item item = new Item();
                item.setName(context.getString(R.string.recipe_plainFlour));
                item.setAmount(650);
                item.setUnit(gram);
                item.setGroup(other);
                item.setPredefinedId(PredefinedId.Recipe_Apple_Pie_Item4.toInt());
                recipeManager.addListItem(recipeId, item);
            }
            {
                Item item = new Item();
                item.setName(context.getString(R.string.recipe_bakingPowder));
                item.setAmount(2);
                item.setUnit(teaspoon);
                item.setGroup(other);
                item.setPredefinedId(PredefinedId.Recipe_Apple_Pie_Item5.toInt());
                recipeManager.addListItem(recipeId, item);
            }
            {
                Item item = new Item();
                item.setName(context.getString(R.string.recipe_apples));
                item.setAmount(6);
                item.setUnit(piece);
                item.setGroup(fruit);
                item.setPredefinedId(PredefinedId.Recipe_Apple_Pie_Item6.toInt());
                recipeManager.addListItem(recipeId, item);
            }

            recipeManager.saveList(recipeId);
        }
        //endregion Recipe: Apple Pie

    }

}
