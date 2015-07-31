package de.fau.cs.mad.kwikshop.android.model;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;


public class DoubleStorageBase<T> implements SimpleStorage<T> {

    protected final Dao<T, Double> dao;

    public DoubleStorageBase(Dao<T, Double> dao) {
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

    public void deleteSingleItem(T item) {
        try {
            dao.delete(item);
        } catch (SQLException e) {
            e.printStackTrace();
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

    @Override
    public T getByName(String name) {
        throw new UnsupportedOperationException();
    }


}
