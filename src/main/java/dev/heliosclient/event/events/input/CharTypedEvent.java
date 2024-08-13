package dev.heliosclient.event.events.input;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;

@Cancelable
@LuaEvent("CharTypedEvent")
public class CharTypedEvent extends Event {
    private final long window;
    private final char character;
    private final int j;

    public CharTypedEvent(long window, char character, int j) {
        this.window = window;
        this.character = character;
        this.j = j;
    }

    public long getWindow() {
        return window;
    }

    public char getCharacter() {
        return character;
    }

    public int getJ() {
        return j;
    }
}
