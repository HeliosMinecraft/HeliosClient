package dev.heliosclient.event.events;

import dev.heliosclient.event.Event;
import net.minecraft.client.util.math.MatrixStack;

public class Render3DEvent extends Event {

    private MatrixStack matrices;
    private float tickDelta;
    private double offsetX, offsetY, offsetZ;

    public  Render3DEvent(MatrixStack matrices, float tickDelta, double offsetX, double offsetY, double offsetZ) {
        this.matrices = matrices;
        this.tickDelta = tickDelta;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public double getOffsetZ() {
        return offsetZ;
    }

    public float getTickDelta() {
        return tickDelta;
    }

    public MatrixStack getMatrices() {
        return matrices;
    }
}
