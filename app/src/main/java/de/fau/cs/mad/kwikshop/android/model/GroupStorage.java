package de.fau.cs.mad.kwikshop.android.model;

import com.j256.ormlite.dao.Dao;

import de.fau.cs.mad.kwikshop.android.common.Group;

public class GroupStorage extends SimpleStorageBase<Group> {

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
