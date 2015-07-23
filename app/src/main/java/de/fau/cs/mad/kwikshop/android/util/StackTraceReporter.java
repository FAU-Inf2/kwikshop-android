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
import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ClipboardHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.NullCommand;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;

public class StackTraceReporter {

    private static final String MAILTO = "mailto";

    //TODO: hide usages of Activity behind some interface, might be cleaner
    private final Activity activity;
    private final ViewLauncher viewLauncher;
    private final ResourceProvider resourceProvider;
    private final ClipboardHelper clipBoardHelper;


    @Inject
    public StackTraceReporter(Activity activity, ViewLauncher viewLauncher, ResourceProvider resourceProvider,
                              ClipboardHelper clipBoardHelper) {

        if(activity == null) {
            throw new ArgumentNullException("activity");
        }

        if(viewLauncher == null) {
            throw new ArgumentNullException("viewLauncher");
        }

        if(resourceProvider == null) {
            throw new ArgumentNullException("resourceProvider");
        }

        if(clipBoardHelper == null) {
            throw new ArgumentNullException("clipBoardHelper");
        }

        this.activity = activity;
        this.viewLauncher = viewLauncher;
        this.resourceProvider = resourceProvider;
        this.clipBoardHelper = clipBoardHelper;
    }


    public void reportStackTraceIfAvailable() {

        // see if stacktrace file is available
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

            viewLauncher.showMessageDialog(
                    resourceProvider.getString(R.string.errorReporting_messaging_dialog_title),
                    resourceProvider.getString(R.string.errorReporting_messaging_dialog_text),
                    resourceProvider.getString(android.R.string.yes),
                    new Command<Void>() {
                        @Override
                        public void execute(Void parameter) {

                            Intent sendIntent = new Intent(
                                    Intent.ACTION_SENDTO,
                                    Uri.fromParts(MAILTO, resourceProvider.getString(R.string.errorReporting_messaging_recipient), null));

                            String subject = resourceProvider.getString(R.string.errorReporting_messaging_subject);
                            String body = traceFinal + "\n\n";

                            sendIntent.putExtra(Intent.EXTRA_TEXT, body);
                            sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);

                            activity.startActivity(Intent.createChooser(sendIntent, resourceProvider.getString(R.string.errorReporting_messaging_chooser_title)));
                        }
                    },
                    resourceProvider.getString(R.string.errorReporting_copyToClipboard),
                    new Command<Void>() {
                        @Override
                        public void execute(Void parameter) {
                            clipBoardHelper.setClipBoardText(
                                    resourceProvider.getString(R.string.errorReporting_copyToClipboard_Label),
                                    traceFinal);
                        }
                    },
                    resourceProvider.getString(android.R.string.no),
                    NullCommand.VoidInstance);

            activity.deleteFile(TopExceptionHandler.STACKTRACE_FILENAME);
        }
    }
}
