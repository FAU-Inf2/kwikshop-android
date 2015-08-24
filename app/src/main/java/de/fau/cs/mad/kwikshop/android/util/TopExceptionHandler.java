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
import android.content.Intent;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.view.ErrorReportingActivity;

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


        // start activity  with NEW_TASK and CLEAR_TASK flags in order to clear the back stack
        // this makes sure the app restarts at the entry point and android does not start with a
        // activity different from ErrorReportingActivity
        Intent intent = ErrorReportingActivity.getIntent(app.getApplicationContext(), true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        app.getApplicationContext().startActivity(intent);

        defaultUEH.uncaughtException(t, e);
    }
}
