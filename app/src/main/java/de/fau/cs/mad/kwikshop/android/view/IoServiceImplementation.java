package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.IoService;

public class IoServiceImplementation implements IoService {

    private final Context context;

    @Inject
    public IoServiceImplementation(Context context) {

        if(context == null) {
            throw new ArgumentNullException("context");
        }

        this.context = context;
    }



    @Override
    public FileInputStream openFileInput(String name) throws FileNotFoundException{
        return context.openFileInput(name);
    }

    @Override
    public void deleteFile(String name) {
        context.deleteFile(name);
    }
}
