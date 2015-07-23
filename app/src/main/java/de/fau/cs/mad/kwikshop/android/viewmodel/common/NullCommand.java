package de.fau.cs.mad.kwikshop.android.viewmodel.common;

public class NullCommand<T> extends Command<T> {

    public static final Command<Object> ObjectInstance = new NullCommand<>();
    public static final Command<String> StringInstance = new NullCommand<>();
    public static final Command<Void> VoidInstance = new NullCommand<>();

    @Override
    public void execute(T parameter) {

    }
}
