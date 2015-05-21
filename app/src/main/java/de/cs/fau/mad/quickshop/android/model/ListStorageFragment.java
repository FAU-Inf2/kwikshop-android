package de.cs.fau.mad.quickshop.android.model;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import de.cs.fau.mad.quickshop.android.DatabaseHelper;
import de.cs.fau.mad.quickshop.android.Item;
import de.cs.fau.mad.quickshop.android.ListStorage;
import de.cs.fau.mad.quickshop.android.LocalListStorage;
import de.cs.fau.mad.quickshop.android.ShoppingList;

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

    public void SetupLocalListStorageFragment(FragmentManager fm, Context context) {
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
            list.addItem(i1);
            list.save();
        }
    }

    //endregion

}
