package dev.heliosclient.event.events.render;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;
import dev.heliosclient.event.events.TickEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;

@Cancelable
@LuaEvent("Render2dEvent")
public class RenderEvent extends Event {
    public static RenderEvent INSTANCE = new RenderEvent();
    private DrawContext drawContext;
    private float tickDelta;

    public static RenderEvent get(DrawContext drawContext, float tickDelta) {
        INSTANCE.drawContext = drawContext;
        INSTANCE.tickDelta = tickDelta;
        return INSTANCE;
    }

    private RenderEvent(){

    }


    public DrawContext getDrawContext() {
        return INSTANCE.drawContext;
    }

    public float getTickDelta() {
        return  INSTANCE.tickDelta;
    }
}
