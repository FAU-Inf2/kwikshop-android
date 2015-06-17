package de.fau.cs.mad.kwikshop.android.model;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;


public class SimpleStorage<T> {

    protected final Dao<T, Integer> dao;

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

    public void updateItem(T item) {
        try {
            dao.update(item);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }

    public void deleteAll() {
        try {
            List<T> items = dao.queryForAll();
            dao.delete(items);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public T getDefaultValue() {
        return null;
    }

}
