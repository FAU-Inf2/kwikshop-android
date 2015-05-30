package de.cs.fau.mad.quickshop.android.model;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import de.cs.fau.mad.quickshop.android.common.Item;
import de.cs.fau.mad.quickshop.android.common.ShoppingList;
import de.cs.fau.mad.quickshop.android.common.Unit;

public class ListStorageFragment extends Fragment {

    //region Constants

    public static final String TAG_LISTSTORAGE = "tag_ListStorage";

    //endregion


    //region Fields

    private static LocalListStorage m_LocalListStorage;
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

        // TODO: Remove this - Creates a test list
        if(m_LocalListStorage.getAllLists().size() == 0) {
            int m = m_LocalListStorage.createList();
            ShoppingList list = m_LocalListStorage.loadList(m);
            list.setName("DB List 1");

            Item i1 = new Item();
            i1.setName("DB Item 1");
            i1.setComment("Sample comment");
            list.addItem(i1);

            Item i2 = new Item();
            i2.setName("DB Item 2");
            i2.setComment("Some really long comment. It just goes on and on and on and on and on and on and on and on and on and on and on and on");
            i2.setBought(true);
            list.addItem(i2);

            Item i3 = new Item();
            i3.setName("DB Item 3");
            list.addItem(i3);

            Item i4 = new Item();
            i4.setName("DB Item 4");
            i4.setComment("Comment");
            i4.setBrand("Some brand name");
            list.addItem(i4);


            Item i5 = new Item();
            i5.setName("DB Item 4");
            i5.setComment("Comment");
            i5.setBrand("Some brand name");
            i5.setAmount(250);
            list.addItem(i5);

            Item i6 = new Item();
            i6.setName("DB Item 6");
            i6.setAmount(5);
            list.addItem(i6);

            list.save();
        }


        try {
            createUnitsInDatabase(activity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //endregion


    private void createUnitsInDatabase(Context context) throws SQLException {

        Dao<Unit, Integer> dao = getDatabaseHelper().getUnitDao();
        int count = dao.queryForAll().size();

        if (count > 0) {
            return;
        }

        Unit[] units = new DefaultDataProvider(context).getDefaultUnits();
        for (Unit u : units) {
            dao.create(u);
        }
    }

}
