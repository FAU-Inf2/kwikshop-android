package de.cs.fau.mad.quickshop.android.model;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;


public class SimpleStorage<T> {

    private final Dao<T, Integer> dao;

    public SimpleStorage(Dao<T, Integer> dao) {
        if (dao == null) {
            throw new IllegalArgumentException("'dao' must not be null");
        }

        this.dao = dao;
    }

    public List<T> getItems() {
        try {
            return dao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addItem(T item) {
        try {
            dao.create(item);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
