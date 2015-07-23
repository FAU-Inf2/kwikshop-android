package de.fau.cs.mad.kwikshop.android.view;

import android.app.Activity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.IoService;

public class IoServiceImplementation implements IoService {

    private final Activity activity;

    public IoServiceImplementation(Activity activity) {

        if(activity == null) {
            throw new ArgumentNullException("activity");
        }

        this.activity = activity;
    }



    @Override
    public FileInputStream openFileInput(String name) throws FileNotFoundException{
        return activity.openFileInput(name);
    }

    @Override
    public void deleteFile(String name) {
        activity.deleteFile(name);
    }
}
