package de.fau.cs.mad.kwikshop.android.common;

import android.content.Context;
import android.util.Log;
import android.view.View;

public class Setting {

    private String name;
    private String caption;
    private Context context;
    private int viewVisibility = View.INVISIBLE;
    private boolean checked = false;
    private boolean isHeader = false;

    public Setting(Context context){
        this.context = context;
    }

    public String getName() {
        return name;
    }

    public void setName(int id) {
        this.name = context.getResources().getString(id);
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(int id) {
        this.caption = context.getResources().getString(id);
    }

    public int getViewVisibility() {return viewVisibility;}

    public void setViewVisibility(int viewVisibility) {this.viewVisibility = viewVisibility;}

    public boolean isChecked() {return checked; }

    public void setChecked(boolean checked) {this.checked = checked;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setIsHeader(boolean isHeader) {
        this.isHeader = isHeader;
    }
}
