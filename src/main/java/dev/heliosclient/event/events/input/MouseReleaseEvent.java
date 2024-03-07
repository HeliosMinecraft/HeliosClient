package dev.heliosclient.event.events.input;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;
import net.minecraft.client.gui.screen.Screen;

@Cancelable
@LuaEvent("MouseReleaseEvent")
public class MouseReleaseEvent extends Event {
    private final long window;
    private final int button;
    private final int action;
    private final int modifiers;
    private final double mouseX;
    private final double mouseY;
    private final Screen screen;


    public MouseReleaseEvent(long window, int button, int action, double mouseX, double mouseY, Screen screen, int modifiers) {
        this.window = window;
        this.button = button;
        this.action = action;
        this.modifiers = modifiers;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.screen = screen;
    }

    public int getButton() {
        return button;
    }

    public int getAction() {
        return action;
    }

    public int getModifiers() {
        return modifiers;
    }

    public long getWindow() {
        return window;
    }

    public double getMouseY() {
        return mouseY;
    }

    public double getMouseX() {
        return mouseX;
    }

    public Screen getScreen() {
        return screen;
    }
}
