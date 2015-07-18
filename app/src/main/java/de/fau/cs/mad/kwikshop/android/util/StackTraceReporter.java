/*
    Based on the StackTraceReporter class from the  the FabLab Android project,
    Copyright 2015 MAD FabLab team

    Licensed under the Apache License, version 2
    http://www.apache.org/licenses/LICENSE-2.0

    https://github.com/FAU-Inf2/fablab-android
 */
package de.fau.cs.mad.kwikshop.android.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.NullCommand;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;

public class StackTraceReporter {

    private static final String MAILTO = "mailto";

    //TODO: hide usages of Activity behind some interface, might be cleander
    private final Activity activity;
    private final ViewLauncher viewLauncher;
    private final ResourceProvider resourceProvider;


    @Inject
    public StackTraceReporter(Activity activity, ViewLauncher viewLauncher, ResourceProvider resourceProvider) {

        if(activity == null) {
            throw new IllegalArgumentException("'activity' must not be null");
        }

        if(viewLauncher == null) {
            throw new IllegalArgumentException("'viewLauncher' must not be null");
        }

        if(resourceProvider == null) {
            throw new IllegalArgumentException("'resourceProvider' must not be null");
        }

        this.activity = activity;
        this.viewLauncher = viewLauncher;
        this.resourceProvider = resourceProvider;
    }


    public void reportStackTraceIfAvailable() {

        // see if stracktrace file is available
        String line;
        String trace = "";
        boolean traceAvailable = true;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(activity.openFileInput(TopExceptionHandler.STACKTRACE_FILENAME)));
            while ((line = reader.readLine()) != null) {
                trace += line + "\n";
            }
        } catch (IOException ioe) {
            traceAvailable =false;
        }

        // if yes ask user to send stacktrace
        if (traceAvailable) {

            final String traceFinal = trace;

            viewLauncher.showYesNoDialog(
                    resourceProvider.getString(R.string.errorReporting_messaging_dialog_title),
                    resourceProvider.getString(R.string.errorReporting_messaging_dialog_text),
                    new Command() {
                        @Override
                        public void execute(Object parameter) {

                            Intent sendIntent = new Intent(
                                    Intent.ACTION_SENDTO,
                                    Uri.fromParts(MAILTO, resourceProvider.getString(R.string.errorReporting_messaging_recipient), null));

                            String subject = resourceProvider.getString(R.string.errorReporting_messaging_subject);
                            String body = traceFinal + "\n\n";

                            sendIntent.putExtra(Intent.EXTRA_TEXT, body);
                            sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);

                            activity.startActivity(Intent.createChooser(sendIntent, resourceProvider.getString(R.string.errorReporting_messaging_chooser_title)));
                        }},
                    NullCommand.Instance);

            activity.deleteFile(TopExceptionHandler.STACKTRACE_FILENAME);
        }
    }
}
