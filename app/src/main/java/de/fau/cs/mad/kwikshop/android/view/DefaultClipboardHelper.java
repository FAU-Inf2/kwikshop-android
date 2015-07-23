package de.fau.cs.mad.kwikshop.android.view;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ClipboardHelper;

public class DefaultClipboardHelper implements ClipboardHelper {

    private final Activity activity;

    @Inject
    public DefaultClipboardHelper(Activity activity) {

        if(activity == null) {
            throw new ArgumentNullException("activity");
        }

        this.activity = activity;
    }


    @Override
    public void setClipBoardText(String label, String value) {

        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Activity.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, value);
        clipboard.setPrimaryClip(clip);

    }
}
