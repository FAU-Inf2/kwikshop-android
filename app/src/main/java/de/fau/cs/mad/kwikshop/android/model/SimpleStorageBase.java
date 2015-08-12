package de.fau.cs.mad.kwikshop.android.model;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;


public class SimpleStorageBase<T> implements SimpleStorage<T> {

    protected final Dao<T, Integer> dao;

    public SimpleStorageBase(Dao<T, Integer> dao) {
        if (dao == null) {
            throw new IllegalArgumentException("'dao' must not be null");
        }

        this.dao = dao;
    }

    @Override
    public List<T> getItems() {
        try {
            return dao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void addItem(T item) {
        try {
            dao.create(item);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateItem(T item) {
        try {
            dao.update(item);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void deleteSingleItem(T item) {
        try {
            dao.delete(item);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteAll() {
        try {
            List<T> items = dao.queryForAll();
            dao.delete(items);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public T getDefaultValue() {
        return null;
    }

    @Override
    public T getByName(String name) {
        //override in sub-classes if necessary
        throw new UnsupportedOperationException();
    }

    @Override
    public void refresh(T instance) {

        if(instance != null) {

            try {
                dao.refresh(instance);
            } catch (SQLException e) {
                //whatever...
                e.printStackTrace();
            }
        }

    }




}
