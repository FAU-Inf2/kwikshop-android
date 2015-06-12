package de.cs.fau.mad.kwikshop.android.model;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import java.sql.SQLException;

import cs.fau.mad.kwikshop_android.R;
import de.cs.fau.mad.kwikshop.android.common.Group;
import de.cs.fau.mad.kwikshop.android.common.Item;
import de.cs.fau.mad.kwikshop.android.common.ShoppingList;
import de.cs.fau.mad.kwikshop.android.common.Unit;
import de.cs.fau.mad.kwikshop.android.view.DisplayHelper;

public class ListStorageFragment extends Fragment {

    //region Constants

    public static final String TAG_LISTSTORAGE = "tag_ListStorage";

    //endregion


    //region Fields

    private static LocalListStorage m_LocalListStorage;
    private static SimpleStorage<Group> m_GroupStorage;
    private static SimpleStorage<Unit> m_UnitStorage;
    private static ListStorageFragment m_ListStorageFragment;
    private static DatabaseHelper m_DatabaseHelper;

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

    public static LocalListStorage getLocalListStorage() {
        return m_LocalListStorage;
    }

    public static DatabaseHelper getDatabaseHelper() {
        return m_DatabaseHelper;
    }

    public static ListStorageFragment getListStorageFragment() {
        return m_ListStorageFragment;
    }

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

        // Create ListStorage
        m_LocalListStorage = new LocalListStorage();


        try {

            //create local group storage and local unit storage
            m_GroupStorage = new GroupStorage(m_DatabaseHelper.getGroupDao());
            createGroupsInDatabase(activity, m_GroupStorage);

            m_UnitStorage = new UnitStorage(m_DatabaseHelper.getUnitDao());
            createUnitsInDatabase(activity, m_UnitStorage);

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
            i1.setBrand(context.getString(R.string.descr_quickadd1));
            i1.setComment(context.getString(R.string.descr_quickadd2));
            i1.setGroup(defaultGroup);
            list.addItem(i1);

            Item i5 = new Item();
            i5.setName(context.getString(R.string.descr_sweets));
            i5.setBrand(context.getString(R.string.descr_drag_and_drop1));
            i5.setComment(context.getString(R.string.descr_drag_and_drop2));
            i5.setGroup(defaultGroup);
            list.addItem(i5);

            Item i6 = new Item();
            i6.setName(context.getString(R.string.descr_coke));
            i6.setBrand(context.getString(R.string.descr_swipe1));
            i6.setComment(context.getString(R.string.descr_swipe2));
            i6.setAmount(5);
            i6.setGroup(defaultGroup);
            list.addItem(i6);


            Item i3 = new Item();
            i3.setName(context.getString(R.string.descr_spaghettis));
            i3.setBrand(context.getString(R.string.descr_fab1));
            i3.setComment(context.getString(R.string.descr_fab2));
            i3.setAmount(3);
            i3.setGroup(defaultGroup);
            list.addItem(i3);

            Item i4 = new Item();
            i4.setName(context.getString(R.string.descr_toilet_paper));
            i4.setBrand(context.getString(R.string.descr_details1));
            i4.setComment(context.getString(R.string.descr_details2));
            i4.setHighlight(true);
            i4.setGroup(defaultGroup);
            list.addItem(i4);

            Item i2 = new Item();
            i2.setName(context.getString(R.string.descr_already_bought));
            i2.setBought(true);
            i2.setGroup(defaultGroup);
            list.addItem(i2);


            list.save();
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
