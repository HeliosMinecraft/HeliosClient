package dev.heliosclient.event.events.input;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;

@Cancelable
@LuaEvent("MouseClickedEvent")
public class MouseScrollEvent extends Event {
    private final double mouseX;
    private final double mouseY;
    private final double verticalAmount;
    private final double horizontalAmount;

    public MouseScrollEvent(double mouseX, double mouseY, double verticalAmount, double horizontalAmount) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.verticalAmount = verticalAmount;
        this.horizontalAmount = horizontalAmount;
    }

    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    public double getVerticalAmount() {
        return verticalAmount;
    }

    public double getHorizontalAmount() {
        return horizontalAmount;
    }
}
