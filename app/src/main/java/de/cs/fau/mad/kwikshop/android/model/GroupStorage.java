package de.cs.fau.mad.kwikshop.android.model;

import com.j256.ormlite.dao.Dao;

import de.cs.fau.mad.kwikshop.android.common.Group;
import de.cs.fau.mad.kwikshop.android.common.Unit;

public class GroupStorage extends SimpleStorage<Group> {

    private static Group defaultGroup;


    public GroupStorage(Dao<Group, Integer> dao) {
        super(dao);
    }


    @Override
    public Group getDefaultValue() {

        if (defaultGroup != null) {
            return defaultGroup;
        }

        for (Group g : getItems()) {
            if (g.getName().equals(DefaultDataProvider.getDefaultGroupName())) {
                defaultGroup = g;
                return g;
            }
        }

        return super.getDefaultValue();
    }
}
