package de.fau.cs.mad.kwikshop.android.model.synchronization;

public class SynchronizationException extends RuntimeException {

    public  SynchronizationException(Throwable cause, String message) {
        super(message, cause);
    }

    public  SynchronizationException(Throwable cause, String messageFormat, Object... messageArgs) {
        super(String.format(messageFormat, messageArgs), cause);
    }



}
