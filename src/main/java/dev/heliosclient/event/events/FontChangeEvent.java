package dev.heliosclient.event.events;

import dev.heliosclient.event.Event;

import java.awt.*;

public class FontChangeEvent extends Event {

    private Font[] fonts;

    public FontChangeEvent(Font[] fonts) {
        this.fonts = fonts;
    }

    public Font[] getFonts() {
        return fonts;
    }
}
