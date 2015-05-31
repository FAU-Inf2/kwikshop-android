package de.cs.fau.mad.kwikshop.android.viewmodel.common;

public abstract class ViewModelBase {


    public interface Listener {

        void onFinish();
    }

    private boolean finished = false;


    public void finish() {

        if (finished) {
            return;
        }

        finished = true;
        //Override in sub-classes to add code that needs to run when the view model is closed

        if (getListener() != null) {
            getListener().onFinish();
        }


    }

    public void onDestroyView() {
    }



    protected abstract Listener getListener();

}
