package dev.heliosclient.event.events.player;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;

@Cancelable
@LuaEvent("ChatMsgEvent")
public class ChatMessageEvent extends Event {
    private final String message;

    public ChatMessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
