package dev.heliosclient.event.events;

import dev.heliosclient.event.Event;

public class ChatMessageEvent implements Event {
    private final String message;

    public ChatMessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
