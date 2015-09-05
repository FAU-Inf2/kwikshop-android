package de.fau.cs.mad.kwikshop.android.model;

import android.content.Context;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
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
    private static SimpleStorage<Unit> m_SingularUnitStorage;
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

    public static SimpleStorage<Unit> getSingularUnitStorage() { return m_SingularUnitStorage;}

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
            m_SingularUnitStorage = new UnitStorage(m_DatabaseHelper.getUnitDao());
            createUnitsInDatabase(context, m_UnitStorage);
            createSingularUnits(context, m_SingularUnitStorage);

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

    private static void createSingularUnits(Context context, SimpleStorage<Unit> singularUnitStorage) throws  SQLException{
        int count = singularUnitStorage.getItems().size();
        if (count > 0) {
            return;
        }
        Unit[] singularUnits = new DefaultDataProvider().getSingularPredefinedUnits();
        Arrays.sort(singularUnits);
        for (Unit u : singularUnits) {
            singularUnitStorage.addItem(u);
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
