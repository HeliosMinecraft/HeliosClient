package dev.heliosclient.event.events.heliosclient;

import dev.heliosclient.event.Event;

import java.awt.*;

public class FontChangeEvent extends Event {
    private final Font[] fonts;

    public FontChangeEvent(Font[] fonts) {
        this.fonts = fonts;
    }

    public Font[] getFonts() {
        return fonts;
    }
}
