package de.fau.cs.mad.kwikshop.android.viewmodel.common;

import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;

public class ListIdExtractor<TList extends DomainListObject> implements ObservableArrayList.IdExtractor<TList, Integer> {

    @Override
    public Integer getId(TList object) {
        return object.getId();
    }

}
