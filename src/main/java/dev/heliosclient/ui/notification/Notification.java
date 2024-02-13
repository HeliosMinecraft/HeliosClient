package dev.heliosclient.ui.notification;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.animation.Easing;
import dev.heliosclient.util.animation.EasingType;
import dev.heliosclient.util.fontutils.fxFontRenderer;
import net.minecraft.client.util.math.MatrixStack;

public abstract class Notification {
    public final int HEIGHT = 25;
    protected long creationTime;
    public int WIDTH = 60;
    public int targetY;
    public int y;
    public long timeElapsed;
    protected boolean expired;
    protected long endDelay = 5000;
    protected int x;

    public Notification() {
        initialise();
    }

    protected void initialise() {
        int screenWidth = HeliosClient.MC.getWindow().getScaledWidth();
        this.y = targetY + HEIGHT;
        this.x = screenWidth - WIDTH - 5;
        this.creationTime = System.currentTimeMillis();
    }

    public void update() {
        timeElapsed = System.currentTimeMillis() - creationTime;

        if (timeElapsed > endDelay) {
            float t = (timeElapsed - endDelay) / 1000.0f; // convert to seconds
            int deltaX = (int) (WIDTH * Easing.ease(EasingType.CUBIC_IN, t));
            x += deltaX;
            if (x > HeliosClient.MC.getWindow().getScaledWidth()) {
                expired = true;
            }
        } else if (y > targetY) {
            y--;
        }
    }

    public void moveY(int deltaY) {
        targetY += deltaY;
        y += deltaY;
    }

    public boolean isExpired() {
        return expired;
    }

    public abstract void render(MatrixStack matrices, int y, fxFontRenderer fontRenderer);
}