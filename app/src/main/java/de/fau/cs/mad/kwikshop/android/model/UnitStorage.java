package de.fau.cs.mad.kwikshop.android.model;

import com.j256.ormlite.dao.Dao;

import de.fau.cs.mad.kwikshop.android.common.Unit;

public class UnitStorage extends SimpleStorage<Unit> {

    private static Unit defaultUnit;


    public UnitStorage(Dao<Unit, Integer> dao) {
        super(dao);
    }


    @Override
    public Unit getDefaultValue() {

        if (defaultUnit != null) {
            return defaultUnit;
        }

        for (Unit u : getItems()) {
            if (u.getName().equals(DefaultDataProvider.getDefaultUnitName())) {
                defaultUnit = u;
                return u;
            }
        }

        return super.getDefaultValue();
    }
}
