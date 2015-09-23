package de.fau.cs.mad.kwikshop.android.model.messages;

public class LoginEvent {

    public LoginEvent(EventType eventType) {
        this.eventType = eventType;
    }

    public enum EventType {
        Started,
        Failed,
        Success
    }

    public EventType getEventType() {
        return eventType;
    }

    private EventType eventType;
}
