/*
    Based on the TopExceptionHandler class from the  the FabLab Android project,
    Copyright 2015 MAD FabLab team

    Licensed under the Apache License, version 2
    http://www.apache.org/licenses/LICENSE-2.0

    https://github.com/FAU-Inf2/fablab-android
 */
package de.fau.cs.mad.kwikshop.android.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

import de.fau.cs.mad.kwikshop.android.R;

public class TopExceptionHandler implements Thread.UncaughtExceptionHandler {


    static final String STACKTRACE_FILENAME = "stack.trace";

    private Thread.UncaughtExceptionHandler defaultUEH;
    private final Activity app;


    public TopExceptionHandler(Activity app) {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.app = app;
    }

    public void uncaughtException(Thread t, Throwable e) {

        Log.e("TopExceptionHandler", "Uncaught Exception", e);

        StackTraceElement[] arr = e.getStackTrace();
        String report = "";

        report += String.format("Version: %s\n", app.getResources().getString(R.string.BuildInfo_Version));
        report += "Commit:\n";
        report += String.format("  kwikshop-android: %s\n", app.getResources().getString(R.string.BuildInfo_Git_Commit));
        report += String.format("  kwikshop-common:  %s\n", app.getResources().getString(R.string.BuildInfo_CommonRepository_Git_Commit));
        report += String.format("Branch: %s\n", app.getResources().getString(R.string.BuildInfo_Git_Branch));
        report += "\n\n";

        report += e.toString() + "\n\n";
        report += "--------- Stack trace ---------\n\n";
        for (int i = 0; i < arr.length; i++) {
            report += "    " + arr[i].toString() + "\n";
        }
        report += "-------------------------------\n\n";

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        report += "--------- Cause ---------\n\n";
        Throwable cause = e.getCause();
        if (cause != null) {
            report += cause.toString() + "\n\n";
            arr = cause.getStackTrace();
            for (int i = 0; i < arr.length; i++) {
                report += "    " + arr[i].toString() + "\n";
            }
        }
        report += "-------------------------------\n\n";

        try {
            FileOutputStream trace = app.openFileOutput(
                    STACKTRACE_FILENAME, Context.MODE_PRIVATE);
            trace.write(report.getBytes());
            trace.close();
        } catch (IOException ioe) {
            // ...
        }

        defaultUEH.uncaughtException(t, e);
    }
}
