package de.fau.cs.mad.kwikshop.android.view;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ClipboardHelper;

public class DefaultClipboardHelper implements ClipboardHelper {

    private final Context context;

    @Inject
    public DefaultClipboardHelper(Context context) {

        if(context == null) {
            throw new ArgumentNullException("context");
        }

        this.context = context;
    }


    @Override
    public void setClipBoardText(String label, String value) {

        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Activity.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, value);
        clipboard.setPrimaryClip(clip);

    }
}
