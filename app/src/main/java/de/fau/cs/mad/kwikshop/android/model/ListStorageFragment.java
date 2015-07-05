package de.fau.cs.mad.kwikshop.android.model;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import java.sql.SQLException;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.common.CalendarEventDate;
import de.fau.cs.mad.kwikshop.android.common.Group;
import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.common.LastLocation;
import de.fau.cs.mad.kwikshop.android.common.Recipe;
import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.common.Unit;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;

public class ListStorageFragment extends Fragment {

    //region Constants

    public static final String TAG_LISTSTORAGE = "tag_ListStorage";

    //endregion


    //region Fields

    private static LocalListStorage m_LocalListStorage;
    private static SimpleStorage<Group> m_GroupStorage;
    private static SimpleStorage<Unit> m_UnitStorage;
    private static SimpleStorage<CalendarEventDate> m_CalendarEventStorage;
    private static SimpleStorage<LastLocation> m_LastLocationStorage;
    private static ListStorageFragment m_ListStorageFragment;
    private static DatabaseHelper m_DatabaseHelper;
    private static ListStorage<Recipe> m_RecipeStorage;

    //endregion


    //region Overrides

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    //endregion


    //region Public Methods

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


    public static LocalListStorage getLocalListStorage() {
        return m_LocalListStorage;
    }

    public static DatabaseHelper getDatabaseHelper() {
        return m_DatabaseHelper;
    }

    public static ListStorageFragment getListStorageFragment() {
        return m_ListStorageFragment;
    }

    public static ListStorage<Recipe> getRecipeStorage(){ return m_RecipeStorage;}

    public void SetupLocalListStorageFragment(FragmentActivity activity) {
        FragmentManager fm = activity.getSupportFragmentManager();
        Context context = activity.getBaseContext();
        // ListStorage is already created? -> Nothing to do
        if(m_LocalListStorage != null)
            return;

        // Our ListStorage needs a DatabaseHelper
        if(m_DatabaseHelper == null)
            m_DatabaseHelper = new DatabaseHelper(context);

        // Find / create the ListStorageFragment
        m_ListStorageFragment = (ListStorageFragment) fm.findFragmentByTag(ListStorageFragment.TAG_LISTSTORAGE);
        if (m_ListStorageFragment == null) {
            m_ListStorageFragment = new ListStorageFragment();

            fm.beginTransaction().add(
                    m_ListStorageFragment, ListStorageFragment.TAG_LISTSTORAGE)
                    .commit();
        }

        if(m_RecipeStorage == null)
            m_RecipeStorage = new RecipeStorage();

        // Create ListStorage
        m_LocalListStorage = new LocalListStorage();


        try {

            //create local group storage and local unit storage
            m_GroupStorage = new GroupStorage(m_DatabaseHelper.getGroupDao());
            createGroupsInDatabase(activity, m_GroupStorage);

            m_UnitStorage = new UnitStorage(m_DatabaseHelper.getUnitDao());
            createUnitsInDatabase(activity, m_UnitStorage);

            m_CalendarEventStorage = new SimpleStorageBase<>(m_DatabaseHelper.getCalendarDao());

            m_LastLocationStorage = new SimpleStorageBase<>(m_DatabaseHelper.getLocationDao());

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(m_LocalListStorage.getAllLists().size() == 0) {
            int m = m_LocalListStorage.createList();
            ShoppingList list = m_LocalListStorage.loadList(m);
            list.setName(context.getString(R.string.descr_my_first_shopping_list));

            Group defaultGroup = m_GroupStorage.getDefaultValue();


            Item i1 = new Item();
            i1.setName(context.getString(R.string.descr_apple));
            i1.setComment(context.getString(R.string.descr_quickadd));
            i1.setGroup(defaultGroup);
            list.addItem(i1);

            Item i5 = new Item();
            i5.setName(context.getString(R.string.descr_sweets));
            i5.setComment(context.getString(R.string.descr_drag_and_drop));
            i5.setGroup(defaultGroup);
            list.addItem(i5);

            Item i6 = new Item();
            i6.setName(context.getString(R.string.descr_coke));
            i6.setComment(context.getString(R.string.descr_swipe));
            i6.setAmount(5);
            i6.setGroup(defaultGroup);
            list.addItem(i6);


            Item i3 = new Item();
            i3.setName(context.getString(R.string.descr_spaghettis));
            i3.setComment(context.getString(R.string.descr_fab));
            i3.setAmount(3);
            i3.setGroup(defaultGroup);
            list.addItem(i3);

            Item i4 = new Item();
            i4.setName(context.getString(R.string.descr_toilet_paper));
            i4.setComment(context.getString(R.string.descr_details));
            i4.setHighlight(true);
            i4.setGroup(defaultGroup);
            list.addItem(i4);

            Item i2 = new Item();
            i2.setName(context.getString(R.string.descr_already_bought));
            i2.setBought(true);
            i2.setGroup(defaultGroup);
            list.addItem(i2);

            getLocalListStorage().saveList(list);
        }

        if(m_RecipeStorage.getAllLists().size() == 0){


            //I couldn't find a better way
            //see defaultDataProvider for order
            Unit gram = m_UnitStorage.getItems().get(6);

            Group meat = m_GroupStorage.getItems().get(11);
            Group vegetable = m_GroupStorage.getItems().get(10);

            int id = m_RecipeStorage.createList();
            Recipe recipe1 = m_RecipeStorage.loadList(id);
            recipe1.setName(context.getString(R.string.recipe_name_chili_con_carne));
            recipe1.setScaleFactor(4);
            recipe1.setScaleName(context.getString(R.string.recipe_scaleName_person));

            Item item1 = new Item();
            item1.setName(context.getString(R.string.recipe_mince));
            item1.setAmount(600);
            item1.setUnit(gram);
            item1.setGroup(meat);
            recipe1.addItem(item1);

            Item item4 = new Item();
            item4.setName(context.getString(R.string.recipe_tomatoes));
            item4.setAmount(500);
            item4.setUnit(gram);
            item4.setGroup(vegetable);
            recipe1.addItem(item4);

            Item item2 = new Item();
            item2.setName(context.getString(R.string.recipe_kidney_beans));
            item2.setAmount(200);
            item2.setUnit(gram);
            item2.setGroup(vegetable);
            recipe1.addItem(item2);

            Item item5 = new Item();
            item5.setName(context.getString(R.string.recipe_corn));
            item5.setAmount(120);
            item5.setUnit(gram);
            item5.setGroup(vegetable);
            recipe1.addItem(item5);

            Item item3 = new Item();
            item3.setName(context.getString(R.string.recipe_potatoes));
            item3.setAmount(4);
            item3.setGroup(vegetable);
            recipe1.addItem(item3);

            Item item6 = new Item();
            item6.setName(context.getString(R.string.recipe_onion));
            item6.setAmount(2);
            item6.setGroup(vegetable);
            recipe1.addItem(item6);

            m_RecipeStorage.saveList(recipe1);
        }

    }

    //endregion


    private void createUnitsInDatabase(Context context, SimpleStorage<Unit> unitStorage) throws SQLException {

        int count = unitStorage.getItems().size();
        if (count > 0) {
            return;
        }

        Unit[] units = new DefaultDataProvider(context).getPredefinedUnits();
        for (Unit u : units) {
            unitStorage.addItem(u);
        }
    }

    private void createGroupsInDatabase(Context context, SimpleStorage<Group> groupStorage) {

        int count = groupStorage.getItems().size();
        if (count > 0) {
            return;
        }

        Group[] defaultGroups = new DefaultDataProvider(context).getPredefinedGroups();
        for (Group g : defaultGroups) {
            groupStorage.addItem(g);
        }

    }

}
