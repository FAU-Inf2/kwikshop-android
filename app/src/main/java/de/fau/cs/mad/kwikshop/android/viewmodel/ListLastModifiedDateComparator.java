package de.fau.cs.mad.kwikshop.android.viewmodel;

import java.util.Comparator;
import java.util.Date;

import de.fau.cs.mad.kwikshop.android.common.interfaces.DomainListObject;

public class ListLastModifiedDateComparator<TList extends DomainListObject> implements Comparator<TList> {

    @Override
    public int compare(TList lhs, TList rhs) {
        Date lhd = lhs.getLastModifiedDate();
        Date rhd = rhs.getLastModifiedDate();


        if(lhd == null) {
            lhd = new Date(0);
        }
        if(rhd == null) {
            rhd = new Date(0);
        }


        //lhs and rhs values switched to sort descending
        return rhd.compareTo(lhd);
    }

}
