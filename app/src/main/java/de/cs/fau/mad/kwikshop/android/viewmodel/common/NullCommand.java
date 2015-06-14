package de.cs.fau.mad.kwikshop.android.viewmodel.common;

public class NullCommand extends Command {

    public static final Command Instance = new NullCommand();


    @Override
    public void execute(Object parameter) {

    }
}
