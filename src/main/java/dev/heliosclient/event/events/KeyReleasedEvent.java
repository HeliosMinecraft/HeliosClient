package dev.heliosclient.event.events;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;

@Cancelable
public class KeyReleasedEvent extends Event {
    private final long window;
    private final int key;
    private final int scancode;
    private final int action;
    private final int modifiers;

    public KeyReleasedEvent(long window, int key, int scancode, int action, int modifiers) {
        this.window = window;
        this.key = key;
        this.scancode = scancode;
        this.action = action;
        this.modifiers = modifiers;

    }

    public int getKey() {
        return key;
    }

    public int getAction() {
        return action;
    }

    public int getModifiers() {
        return modifiers;
    }

    public int getScancode() {
        return scancode;
    }

    public long getWindow() {
        return window;
    }
}
