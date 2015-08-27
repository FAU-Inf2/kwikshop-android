package de.fau.cs.mad.kwikshop.android.model;

import android.content.Context;

import java.sql.SQLException;
import java.util.Date;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.common.CalendarEventDate;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.PredefinedId;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;

public class ListStorageFragment  {

    private static LocalListStorage m_LocalListStorage;
    private static SimpleStorage<Group> m_GroupStorage;
    private static SimpleStorage<Unit> m_UnitStorage;
    private static SimpleStorage<CalendarEventDate> m_CalendarEventStorage;
    private static SimpleStorage<LastLocation> m_LastLocationStorage;
    private static SimpleStorage<DeletedList> m_DeletedListStorage;
    private static SimpleStorage<DeletedItem> m_DeletedItemStorage;
    private static ListStorageFragment m_ListStorageFragment;
    private static DatabaseHelper m_DatabaseHelper;
    private static ListStorage<Recipe> m_RecipeStorage;





    public static SimpleStorage<Group> getGroupStorage() {
        return m_GroupStorage;
    }

    public static SimpleStorage<Unit> getUnitStorage() {
        return m_UnitStorage;
    }

    public static SimpleStorage<CalendarEventDate> getCalendarEventStorage() {
        return m_CalendarEventStorage;
    }

    public static SimpleStorage<LastLocation> getLastLocationStorage(){
        return m_LastLocationStorage;
    }

    public static SimpleStorage<DeletedList> getDeletedListStorage() {
        return m_DeletedListStorage;
    }

    public static SimpleStorage<DeletedItem> getDeletedItemStorage() {
        return m_DeletedItemStorage;
    }


    public static LocalListStorage getLocalListStorage() {
        return m_LocalListStorage;
    }

    public static DatabaseHelper getDatabaseHelper() {
        return m_DatabaseHelper;
    }


    public static ListStorage<Recipe> getRecipeStorage(){ return m_RecipeStorage;}

    public static void SetupLocalListStorageFragment(Context context) {

        // ListStorage is already created? -> Nothing to do
        if(m_LocalListStorage != null) {
            return;
        }

        // Our ListStorage needs a DatabaseHelper
        if(m_DatabaseHelper == null) {
            m_DatabaseHelper = new DatabaseHelper(context);
        }

        if(m_RecipeStorage == null)
            m_RecipeStorage = new RecipeStorage();

        // Create ListStorage
        m_LocalListStorage = new LocalListStorage();


        try {

            //create local group storage and local unit storage
            m_GroupStorage = new GroupStorage(m_DatabaseHelper.getGroupDao());
            createGroupsInDatabase(context, m_GroupStorage);

            m_UnitStorage = new UnitStorage(m_DatabaseHelper.getUnitDao());
            createUnitsInDatabase(context, m_UnitStorage);

            m_CalendarEventStorage = new SimpleStorageBase<>(m_DatabaseHelper.getCalendarDao());

            m_LastLocationStorage = new SimpleStorageBase<>(m_DatabaseHelper.getLocationDao());

            m_DeletedListStorage = new SimpleStorageBase<>(m_DatabaseHelper.getDeletedListDao());

            m_DeletedItemStorage = new SimpleStorageBase<>(m_DatabaseHelper.getDeletedItemDao());

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(m_LocalListStorage.getAllLists().size() == 0) {

            createPredefinedShoppingLists(context);
        }

        if(m_RecipeStorage.getAllLists().size() == 0){

            createPredefinedRecipes(context);
        }

    }

    private static void createPredefinedRecipes(Context context) {

        Unit gram = m_UnitStorage.getByName(DefaultDataProvider.UnitNames.GRAM);
        Unit cups = m_UnitStorage.getByName(DefaultDataProvider.UnitNames.CUP);
        Unit tbsp = m_UnitStorage.getByName(DefaultDataProvider.UnitNames.TABLESPOON);
        Unit cans = m_UnitStorage.getByName(DefaultDataProvider.UnitNames.CAN);
        Unit piece = m_UnitStorage.getByName(DefaultDataProvider.UnitNames.PIECE);
        Unit pack = m_UnitStorage.getByName(DefaultDataProvider.UnitNames.PACK);
        Unit teaspoon = m_UnitStorage.getByName(DefaultDataProvider.UnitNames.TEASPOON);

        Group meat = m_GroupStorage.getByName(DefaultDataProvider.GroupNames.MEAT_AND_FISH);
        Group vegetable = m_GroupStorage.getByName(DefaultDataProvider.GroupNames.FRUITS_AND_VEGETABLES);
        Group milk = m_GroupStorage.getByName(DefaultDataProvider.GroupNames.MILK_AND_CHEESE);
        Group other = m_GroupStorage.getByName(DefaultDataProvider.GroupNames.OTHER);

        {
            int id = m_RecipeStorage.createList();
            Recipe recipe1 = m_RecipeStorage.loadList(id);
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

            m_RecipeStorage.saveList(recipe1);
        }

        {
            int id = m_RecipeStorage.createList();

            Recipe recipe = m_RecipeStorage.loadList(id);
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

            m_RecipeStorage.saveList(recipe);
        }

        {

            int id = m_RecipeStorage.createList();
            Recipe recipe = m_RecipeStorage.loadList(id);
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

            m_RecipeStorage.saveList(recipe);
        }

        {
            int id = m_RecipeStorage.createList();
            Recipe recipe = m_RecipeStorage.loadList(id);
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

            m_RecipeStorage.saveList(recipe);
        }
    }

    private static void createPredefinedShoppingLists(Context context) {
        int m = m_LocalListStorage.createList();
        ShoppingList list = m_LocalListStorage.loadList(m);
        list.setName(context.getString(R.string.descr_my_first_shopping_list));
        list.setLastModifiedDate(new Date());
        list.setPredefinedId(PredefinedId.ShoppingList_MyFirstShoppingList.toInt());

        Group defaultGroup = m_GroupStorage.getDefaultValue();

        {
            Item i1 = new Item();
            i1.setName(context.getString(R.string.descr_apple));
            i1.setComment(context.getString(R.string.descr_quickadd));
            i1.setGroup(defaultGroup);
            i1.setPredefinedId(PredefinedId.ShoppingList_MyFirstShoppingList_Item1.toInt());
            list.addItem(i1);
        }
        {
            Item i5 = new Item();
            i5.setName(context.getString(R.string.descr_sweets));
            i5.setComment(context.getString(R.string.descr_drag_and_drop));
            i5.setGroup(defaultGroup);
            i5.setPredefinedId(PredefinedId.ShoppingList_MyFirstShoppingList_Item5.toInt());
            list.addItem(i5);
        }
        {
            Item i6 = new Item();
            i6.setName(context.getString(R.string.descr_coke));
            i6.setComment(context.getString(R.string.descr_swipe));
            i6.setAmount(5);
            i6.setGroup(defaultGroup);
            i6.setPredefinedId(PredefinedId.ShoppingList_MyFirstShoppingList_Item6.toInt());
            list.addItem(i6);
        }
        {
            Item i3 = new Item();
            i3.setName(context.getString(R.string.descr_spaghettis));
            i3.setComment(context.getString(R.string.descr_fab));
            i3.setAmount(3);
            i3.setGroup(defaultGroup);
            i3.setPredefinedId(PredefinedId.ShoppingList_MyFirstShoppingList_Item3.toInt());
            list.addItem(i3);
        }
        {
            Item i4 = new Item();
            i4.setName(context.getString(R.string.descr_toilet_paper));
            i4.setComment(context.getString(R.string.descr_details));
            i4.setHighlight(true);
            i4.setGroup(defaultGroup);
            i4.setPredefinedId(PredefinedId.ShoppingList_MyFirstShoppingList_Item4.toInt());
            list.addItem(i4);
        }
        {
            Item i2 = new Item();
            i2.setName(context.getString(R.string.descr_already_bought));
            i2.setBought(true);
            i2.setGroup(defaultGroup);
            i2.setPredefinedId(PredefinedId.ShoppingList_MyFirstShoppingList_Item2.toInt());
            list.addItem(i2);
        }

        getLocalListStorage().saveList(list);
    }



    private static void createUnitsInDatabase(Context context, SimpleStorage<Unit> unitStorage) throws SQLException {

        int count = unitStorage.getItems().size();
        if (count > 0) {
            return;
        }

        Unit[] units = new DefaultDataProvider().getPredefinedUnits();
        for (Unit u : units) {
            unitStorage.addItem(u);
        }
    }

    private static void createGroupsInDatabase(Context context, SimpleStorage<Group> groupStorage) {

        int count = groupStorage.getItems().size();
        if (count > 0) {
            return;
        }

        Group[] defaultGroups = new DefaultDataProvider().getPredefinedGroups();
        for (Group g : defaultGroups) {
            groupStorage.addItem(g);
        }

    }

}
