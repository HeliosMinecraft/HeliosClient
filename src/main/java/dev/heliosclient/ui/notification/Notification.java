package dev.heliosclient.ui.notification;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.animation.Easing;
import dev.heliosclient.util.animation.EasingType;
import dev.heliosclient.util.fontutils.fxFontRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;

public abstract class Notification {
    public static AnimationStyle ANIMATE = AnimationStyle.SLIDE;
    protected PositionMode positionMode = PositionMode.BOTTOM_RIGHT;
    protected int screenWidth;
    protected int screenHeight;

    public static boolean IS_FANCY = false;

    protected int width = 1;
    protected int height = 1;
    protected int targetX;
    public int targetY;
    protected int x;
    protected int y;

    protected long creationTime;
    protected long timeElapsed;
    protected long endDelay = 5000;
    protected float scale = 0.0f;

    protected boolean expired;

    public SoundEvent soundEvent;
    public float volume = 1.0f, pitch = 1.0f;

    public Notification() {
        this(25,60);
    }

    protected Notification(int width, int height) {
        this.width = width;
        this.height = height;

        if(HeliosClient.MC.getWindow() != null) {
            this.screenWidth = HeliosClient.MC.getWindow().getScaledWidth();
            this.screenHeight = HeliosClient.MC.getWindow().getScaledHeight();
        }
        calculateInitialPosition();
    }
    // Flexible position calculation
    protected void calculateInitialPosition() {
        switch (positionMode) {
            case BOTTOM_RIGHT:
                targetX = screenWidth - width - 5;
                targetY = screenHeight - height - 5;
                x = screenWidth + 5; // Start off-screen to the right
                y = targetY;
                break;
            case TOP_RIGHT:
                targetX = screenWidth - width - 5;
                targetY = 5;
                x = screenWidth + 5; // Start off-screen to the right
                y = targetY;
                break;
            case BOTTOM_LEFT:
                targetX = 5;
                targetY = screenHeight - height - 5;
                x = -width - 5; // Start off-screen to the left
                y = targetY;
                break;
            case TOP_LEFT:
                targetX = 5;
                targetY = 5;
                x = -width - 5; // Start off-screen to the left
                y = targetY;
                break;
            case CENTER:
                targetX = (screenWidth - width) / 2;
                targetY = (screenHeight - height) / 2;
                x = screenWidth + 5; // Start off-screen to the right
                y = targetY;
                break;
        }
    }

    public void update() {
        timeElapsed = System.currentTimeMillis() - creationTime;

        // Update based on animation style
        if (ANIMATE == AnimationStyle.SLIDE) {
            updateSlideAnimation();
        } else if (ANIMATE == AnimationStyle.POP) {
            updatePopAnimation();
        }

        // Check for expiration
        if (timeElapsed > endDelay + 1000) {
            expired = true;
        }
    }

    protected void updateSlideAnimation() {
        // Sliding in
        if (isSlideIn()) {
            float time = calculateSlideInProgress();
            x = calculateSlideInX(time);
            y = targetY;
        }
        // Sliding out
        else if (isSlideOut()) {
            float time = calculateSlideOutProgress();
            x = calculateSlideOutX(time);
            y = targetY;

            // Mark as expired when fully off-screen
            if (isOffScreen()) {
                expired = true;
            }
        }
        // Stationary
        else {
            x = targetX;
            y = targetY;
        }
    }

    protected void updatePopAnimation() {
        if (timeElapsed < 200) {
            scale = Easing.ease(EasingType.BACK_OUT, MathHelper.clamp((float) timeElapsed / 200, 0f, 1f));
        } else if (timeElapsed <= endDelay) {
            scale = 1.0f;
        } else {
            float fadeOutProgress = (float) (timeElapsed - endDelay) / 200f;
            scale = 1.0f - Easing.ease(EasingType.CUBIC_OUT, MathHelper.clamp(fadeOutProgress, 0f, 1f));

            if (scale <= 0.01f) {
                expired = true;
                scale = 0;
            }
        }
        x = targetX;
        y = targetY;
    }

    // Helper methods for slide animation
    protected float calculateSlideInProgress() {
        // Adjust slide-in duration (e.g., 20% of total delay)
        return Math.min(timeElapsed / (endDelay * 0.2f), 1f);
    }

    protected float calculateSlideOutProgress() {
        // Slide out duration (e.g., 300ms)
        float slideOutDuration = 300f;
        return Math.min((timeElapsed - endDelay) / slideOutDuration, 1f);
    }

    protected boolean isSlideIn() {
        return timeElapsed < endDelay * 0.2;
    }

    protected boolean isSlideOut() {
        return timeElapsed > endDelay;
    }

    protected boolean isOffScreen() {
        return switch (positionMode) {
            case BOTTOM_RIGHT, TOP_RIGHT -> x >= screenWidth + width;
            case BOTTOM_LEFT, TOP_LEFT -> x <= -width;
            default -> false;
        };
    }

    protected int calculateSlideInX(float progress) {
        return calculatePositionWithEasing(targetX, progress, true);
    }

    protected int calculateSlideOutX(float progress) {
        return calculatePositionWithEasing(targetX, progress, false);
    }

    protected int calculatePositionWithEasing(int target, float progress, boolean slideIn) {
        EasingType easingType;
        int startPosition;

        switch (positionMode) {
            case BOTTOM_RIGHT, TOP_RIGHT -> {
                startPosition = screenWidth + width;
                easingType = slideIn ? EasingType.BACK_OUT : EasingType.CUBIC_IN;
            }
            case BOTTOM_LEFT, TOP_LEFT -> {
                startPosition = -width;
                easingType = slideIn ? EasingType.BACK_OUT : EasingType.CUBIC_IN;
            }
            default -> {
                startPosition = target;
                easingType = EasingType.CUBIC_OUT;
            }
        }

        float easedProgress = Easing.ease(easingType, progress);

        if (slideIn) {
            // Slide in: from off-screen to target
            return MathHelper.lerp(easedProgress, startPosition, target);
        } else {
            // Slide out: from target to off-screen
            switch (positionMode) {
                case BOTTOM_RIGHT, TOP_RIGHT -> {
                    return MathHelper.lerp(easedProgress, target, screenWidth + width);
                }
                case BOTTOM_LEFT, TOP_LEFT -> {
                    return MathHelper.lerp(easedProgress, target, -width);
                }
                default -> {
                    return target;
                }
            }
        }
    }

    public void moveY(int deltaY) {
        targetY += deltaY;
        y += deltaY;
    }

    public void smoothMoveY(int targetY) {
        int currentY = getY();

        // Calculate new Y with smoothness (easing)
        int newY = Math.round((targetY - currentY) * 0.25f);

        moveY(newY);
    }
    protected void updateDimensions(int newWidth, int newHeight) {
        // Only recalculate if dimensions actually change
        if (this.width != newWidth || this.height != newHeight) {
            this.width = newWidth;
            this.height = newHeight;

            // Preserve current position relative to target
            int deltaX = x - targetX;
            int deltaY = y - targetY;

            // Recalculate target positions
            calculateInitialPosition();

            // Adjust current position to maintain relative offset
            x = targetX + deltaX;
            y = targetY + deltaY;
        }
    }
    public boolean isExpired() {
        return expired;
    }

    public abstract void render(MatrixStack matrices, int y, fxFontRenderer fontRenderer);

    public void playSound(SoundEvent soundEvent, float volume, float pitch) {}

    public long getCreationTime() {
        return creationTime;
    }

    public int getWidth() {
        return width;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getHeight() {
        return height;
    }

    public float getScale() {
        return scale;
    }

    public long getEndDelay() {
        return endDelay;
    }

    public long getTimeElapsed() {
        return timeElapsed;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setTargetX(int targetX) {
        this.targetX = targetX;
    }

    public void setTargetY(int targetY) {
        this.targetY = targetY;
    }

    public void setPositionMode(PositionMode mode) {
        this.positionMode = mode;
        calculateInitialPosition();
    }

    public enum AnimationStyle {
        SLIDE,
        POP
    }

    public enum PositionMode {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        CENTER
    }
}