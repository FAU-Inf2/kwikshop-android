package de.fau.cs.mad.kwikshop.android.model.messages;

import de.fau.cs.mad.kwikshop.android.util.StringHelper;

public class SynchronizationEvent {

    private final SynchronizationEventType eventType;
    private final String message;



    public SynchronizationEvent(SynchronizationEventType eventType) {
        this(eventType, null);
    }

    public SynchronizationEvent(SynchronizationEventType eventType, String message) {
        this.eventType = eventType;
        this.message = message;
    }



    public SynchronizationEventType getEventType() {
        return this.eventType;
    }

    public String getMessage() {
        return StringHelper.isNullOrWhiteSpace(this.message)
                ? ""
                : this.message;
    }



    public static SynchronizationEvent CreateStartedMessage() {
        return new SynchronizationEvent(SynchronizationEventType.Started);
    }

    public static SynchronizationEvent CreateCompletedMessage() {
        return new SynchronizationEvent(SynchronizationEventType.Completed);
    }

    public static SynchronizationEvent CreateProgressMessage(String message) {
        return new SynchronizationEvent(SynchronizationEventType.Progress, message);
    }

    public static SynchronizationEvent CreateFailedMessage(String message) {
        return new SynchronizationEvent(SynchronizationEventType.Failed, message);
    }
}
