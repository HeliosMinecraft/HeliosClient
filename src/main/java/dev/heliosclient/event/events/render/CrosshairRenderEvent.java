package dev.heliosclient.event.events.render;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import net.minecraft.client.gui.DrawContext;

@Cancelable
public class CrosshairRenderEvent extends Event {
    public final int x ,y,width,height;
    public final DrawContext drawContext;

    public CrosshairRenderEvent(DrawContext context, int x, int y, int width, int height) {
        this.drawContext = context;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
