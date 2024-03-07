package dev.heliosclient.event.events.input;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;

@Cancelable
@LuaEvent("CharTypedEvent")
public class CharTypedEvent extends Event {
    private final long window;
    private final char i;
    private final int j;

    public CharTypedEvent(long window, char i, int j) {
        this.window = window;
        this.i = i;
        this.j = j;
    }

    public long getWindow() {
        return window;
    }

    public char getI() {
        return i;
    }

    public int getJ() {
        return j;
    }
}
