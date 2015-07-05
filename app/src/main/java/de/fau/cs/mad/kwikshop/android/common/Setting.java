package de.fau.cs.mad.kwikshop.android.common;

import android.content.Context;


public class Setting {

    private String name;

    private String caption;

    private Context context;

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
}
