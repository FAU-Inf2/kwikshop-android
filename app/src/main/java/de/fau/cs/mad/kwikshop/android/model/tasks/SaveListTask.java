package de.fau.cs.mad.kwikshop.android.model.tasks;

import android.os.AsyncTask;

import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListStorage;

public class SaveListTask<TList extends DomainListObject> extends AsyncTask<TList, Void, Void> {


    private final ListStorage<TList> listStorage;

    public SaveListTask(ListStorage<TList> listStorage) {

        if(listStorage == null) {
            throw new IllegalArgumentException("'listStorage' must not be null");
        }

        this.listStorage = listStorage;
    }


    @Override
    protected Void doInBackground(TList... params) {

        for(TList list : params) {
            listStorage.saveList(list);
        }

        return null;
    }
}
