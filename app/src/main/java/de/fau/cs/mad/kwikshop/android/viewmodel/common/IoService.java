package de.fau.cs.mad.kwikshop.android.viewmodel.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public interface IoService {

    FileInputStream openFileInput(String name) throws FileNotFoundException;

    void deleteFile(String name);;

}
