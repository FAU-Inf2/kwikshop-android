package de.cs.fau.mad.quickshop.android.model;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

import de.cs.fau.mad.quickshop.android.common.Group;

//TODO: Proper exception handling
public class LocalGroupStorage implements SimpleStorage<Group> {

    private final Dao<Group, Integer> dao;

    public LocalGroupStorage(Dao<Group, Integer> dao) {
        if (dao == null) {
            throw new IllegalArgumentException("'dao' must not be null");
        }

        this.dao = dao;
    }

    @Override
    public List<Group> getItems() {
        try {
            return dao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void addItem(Group group) {
        try {
            dao.create(group);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
