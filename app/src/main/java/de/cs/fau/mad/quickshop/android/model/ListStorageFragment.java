package de.cs.fau.mad.quickshop.android.model;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import de.cs.fau.mad.quickshop.android.ListStorage;

public class ListStorageFragment extends Fragment {


    //region Fields

    private ListStorage m_ListStorage;

    //endregion


    //region Overrides

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    //endregion


    //region Public Methods

    public void setListStorage(ListStorage value) {
        this.m_ListStorage = value;
    }


    public ListStorage getListStorage() {
        return this.m_ListStorage;
    }

    //endregion

}
