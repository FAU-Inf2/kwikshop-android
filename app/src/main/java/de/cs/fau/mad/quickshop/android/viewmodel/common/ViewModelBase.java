package de.cs.fau.mad.quickshop.android.viewmodel.common;

public abstract class ViewModelBase {

    public interface Listener {

        void onNameChanged(String value);

        void onFinish();
    }


    public void finish() {

        //Override in sub-classes to add code that needs to run when the view model is closed

        if (getListener() != null) {
            getListener().onFinish();
        }
    }


    protected abstract Listener getListener();

}
