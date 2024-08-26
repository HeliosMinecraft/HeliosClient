package dev.heliosclient.event.events.client;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import net.minecraft.client.gui.screen.Screen;

@Cancelable
public class OpenScreenEvent extends Event {
    public final Screen screen;

    public OpenScreenEvent(Screen screen) {
        this.screen = screen;
    }

    public Screen getScreen() {
        return screen;
    }
}
