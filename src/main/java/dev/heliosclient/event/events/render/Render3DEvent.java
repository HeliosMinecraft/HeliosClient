package dev.heliosclient.event.events.render;

import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

@LuaEvent("Render3dEvent")
public class Render3DEvent extends Event {

    public static Render3DEvent INSTANCE = new Render3DEvent();
    private MatrixStack matrices;
    private float tickDelta;
    private double offsetX;
    private double offsetY;
    private double offsetZ;

    public static Render3DEvent get(MatrixStack matrices, float tickDelta, double offsetX, double offsetY, double offsetZ) {
        INSTANCE.matrices = matrices;
        INSTANCE.tickDelta = tickDelta;
        INSTANCE.offsetX = offsetX;
        INSTANCE.offsetY = offsetY;
        INSTANCE.offsetZ = offsetZ;
        return INSTANCE;
    }

    private Render3DEvent(){

    }


    public double getOffsetX() {
        return INSTANCE.offsetX;
    }

    public double getOffsetY() {
        return INSTANCE.offsetY;
    }

    public double getOffsetZ() {
        return INSTANCE.offsetZ;
    }

    public float getTickDelta() {
        return INSTANCE.tickDelta;
    }

    public MatrixStack getMatrices() {
        return INSTANCE.matrices;
    }
}
