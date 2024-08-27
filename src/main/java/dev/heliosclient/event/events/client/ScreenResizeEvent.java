package dev.heliosclient.event.events.client;

import dev.heliosclient.event.Event;

public class ScreenResizeEvent extends Event {
   public final int prevWidth, prevHeight,  newWidth,  newHeight;

    public ScreenResizeEvent(int prevWidth, int prevHeight, int newWidth, int newHeight) {
        this.prevWidth = prevWidth;
        this.prevHeight = prevHeight;
        this.newWidth = newWidth;
        this.newHeight = newHeight;
    }
}
