package dev.heliosclient.event.events;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;

@Cancelable
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
