package dev.heliosclient.ui.notification;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.animation.Easing;
import dev.heliosclient.util.animation.EasingType;
import dev.heliosclient.util.fontutils.fxFontRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvent;

public abstract class Notification {
    public static AnimationStyle ANIMATE = AnimationStyle.SLIDE;
    public final int HEIGHT = 25;
    public long creationTime;
    public int WIDTH = 60;
    public int targetY;
    public int y;
    public long timeElapsed;
    public int x;
    public float scale = 0.0f;
    public SoundEvent soundEvent;
    public float volume = 1.0f, pitch = 1.0f;
    protected boolean expired;
    protected long endDelay = 5000;
    protected float endDelayInS = 5.0f;

    public Notification() {
        initialise();
    }

    protected void initialise() {
        int screenWidth = HeliosClient.MC.getWindow().getScaledWidth();
        this.y = targetY + HEIGHT;
        this.x = screenWidth - WIDTH - 5;
    }

    public void update() {
        timeElapsed = System.currentTimeMillis() - creationTime;
        float t = timeElapsed / 1000.0f;

        if (ANIMATE == AnimationStyle.POP) {
            if (timeElapsed < 200) {
                scale = Easing.ease(EasingType.CUBIC_OUT, (float) timeElapsed / 200);
            }
            if (timeElapsed > endDelay) {
                scale = 1.0f - Easing.ease(EasingType.CUBIC_IN, (float) (timeElapsed - endDelay) / 200);
                if (scale < 0.0f) {
                    expired = true;
                }
            }
        }
        if (ANIMATE == AnimationStyle.SLIDE) {
            if (timeElapsed > endDelay) {
                float time = (timeElapsed - endDelay) / 1000.0f;
                int deltaX = (int) (WIDTH * Easing.ease(EasingType.CUBIC_IN, time));
                x += deltaX;
                if (x > HeliosClient.MC.getWindow().getScaledWidth()) {
                    expired = true;
                }
            } else {
                int deltaY = (int) (HEIGHT * Easing.ease(EasingType.QUADRATIC_IN_OUT, t));
                if (y > targetY) {
                    y -= deltaY;
                }
            }
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

    public void playSound(SoundEvent soundEvent, float volume, float pitch) {

    }

    public enum AnimationStyle {
        SLIDE,
        POP
    }
}