package dev.heliosclient.event.events;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import net.minecraft.client.gui.DrawContext;

@Cancelable

public class RenderEvent extends Event {
    private final DrawContext drawContext;
    private final float tickDelta;

    public RenderEvent(DrawContext drawContext, float tickDelta) {
        this.drawContext = drawContext;
        this.tickDelta = tickDelta;
    }

    public DrawContext getDrawContext() {
        return drawContext;
    }

    public float getTickDelta() {
        return tickDelta;
    }
}
