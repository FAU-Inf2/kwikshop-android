package de.fau.cs.mad.kwikshop.android.model.messages;

public class SynchronizationEvent {

    private final SynchronizationEventType eventType;

    public SynchronizationEvent(SynchronizationEventType eventType) {
        this.eventType = eventType;
    }

    public SynchronizationEventType getEventType() {
        return this.eventType;
    }

}
