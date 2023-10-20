package dev.heliosclient.util.notification;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.animation.Easing;
import dev.heliosclient.util.animation.EasingType;
import dev.heliosclient.util.fontutils.fxFontRenderer;
import net.minecraft.client.util.math.MatrixStack;

public abstract class Notification {
    public final int HEIGHT = 25;
    protected final long creationTime;
    public int WIDTH = 60;
    public int targetY;
    public int currentY;
    protected int currentX;
    protected boolean expired;
    protected long endDelay = 5000;

    public Notification() {
        int screenWidth = HeliosClient.MC.getWindow().getScaledWidth();
        this.currentY = targetY + HEIGHT;
        this.currentX = screenWidth - WIDTH - 5;
        this.creationTime = System.currentTimeMillis();
    }

    public void update() {
        long timeElapsed = System.currentTimeMillis() - creationTime;

        if (timeElapsed > endDelay) {
            float t = (timeElapsed - endDelay) / 1000.0f; // convert to seconds
            int deltaX = (int) (WIDTH * Easing.ease(EasingType.CUBIC_IN, t));
            currentX += deltaX;
            if (currentX > HeliosClient.MC.getWindow().getScaledWidth()) {
                expired = true;
            }
        } else if (currentY > targetY) {
            currentY--;
        }
    }

    public void moveY(int deltaY) {
        targetY += deltaY;
        currentY += deltaY;
    }

    public boolean isExpired() {
        return expired;
    }

    public abstract void render(MatrixStack matrices, int y, fxFontRenderer fontRenderer);
}