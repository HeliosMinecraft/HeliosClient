package dev.heliosclient.ui.notification;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.MathUtils;
import dev.heliosclient.util.animation.Easing;
import dev.heliosclient.util.animation.EasingType;
import dev.heliosclient.util.fontutils.fxFontRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;

public abstract class Notification {
    public static AnimationStyle ANIMATE = AnimationStyle.SLIDE;
    public int HEIGHT = 25;
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
    public static boolean IS_FANCY = false;

    public Notification() {
        initialise();
    }

    protected void initialise() {
        if(ANIMATE != AnimationStyle.SLIDE) {
            int screenWidth = HeliosClient.MC.getWindow().getScaledWidth();
            this.y = targetY + HEIGHT;
            this.x = screenWidth - WIDTH - 5;
        }
    }

    public void update() {
        timeElapsed = System.currentTimeMillis() - creationTime;

        int screenWidth = HeliosClient.MC.getWindow().getScaledWidth();

        if (ANIMATE == AnimationStyle.POP) {
            if (timeElapsed < 200) {
                scale = Easing.ease(EasingType.CUBIC_OUT, (float) timeElapsed / 200);
            }
            if (timeElapsed > endDelay) {
                scale =  1.0f - Easing.ease(EasingType.CUBIC_IN, (float) (timeElapsed - endDelay) / 200);
                if (scale < 0.0f) {
                    scale = 0.0f;
                    expired = true;
                }
            }
            this.x = screenWidth - WIDTH - 5;
        }
        if (ANIMATE == AnimationStyle.SLIDE) {
            if (y > targetY) {
                y -= (int) (HEIGHT * MathHelper.clamp(timeElapsed / 2500.0f, 0.0f, 1.0f));
            }

            int targetX =  screenWidth - WIDTH - 5;

            if (timeElapsed > endDelay) {
                float time = (timeElapsed - endDelay) / 1000.0f;
                int deltaX = (int) (WIDTH * Easing.ease(EasingType.CUBIC_IN, time));
                x += deltaX;

                // + 5 is the buffer zone (aka hopefully no visual bugs)
                if (x > HeliosClient.MC.getWindow().getScaledWidth() + 5) {
                    expired = true;
                }

            }else if (timeElapsed < endDelay * 0.2) {
                // 20% of end delay should be put for sliding in
                float time = timeElapsed / (endDelay * 0.2f);
                x = screenWidth - MathHelper.floor(WIDTH * Easing.ease(EasingType.CUBIC_IN, time));
            } else {
                x = targetX;
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