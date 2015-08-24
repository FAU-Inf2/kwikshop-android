package de.fau.cs.mad.kwikshop.android.util;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.util.EqualityComparer;

public class ClientEqualityComparer extends EqualityComparer {

    @Inject
    public ClientEqualityComparer() {

    }

    @Override
    protected boolean idEquals(Group group1, Group group2) {
        return group1.getId() == group2.getId();
    }

    @Override
    protected boolean idEquals(Unit unit1, Unit unit2) {
        return unit1.getId() == unit2.getId();
    }

    @Override
    protected boolean idEquals(LastLocation location1, LastLocation location2) {
        return location1.getId() == location2.getId();
    }
}
