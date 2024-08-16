package dev.heliosclient.util.animation;


import net.minecraft.util.math.MathHelper;

public class Animation {
    private float FADE_SPEED = 0.05f;
    private float alpha = 1.0f;
    private boolean fading = false;
    private boolean fadeIn = false;
    private EasingType easingType = EasingType.LINEAR_IN;

    public Animation(EasingType easingType) {
        this.easingType = easingType;
    }

    public void startFading(boolean fadeIn) {
        this.fading = true;
        this.fadeIn = fadeIn;
        this.alpha = fadeIn ? 0.0f : 1.0f;
    }

    private void updateAlpha() {
        if (fading) {
            alpha += fadeIn ? FADE_SPEED : -FADE_SPEED;
            if (alpha <= 0.0f || alpha >= 1.0f) {
                fading = false;
                alpha = MathHelper.clamp(alpha,0.0f,1.0f);
            }
        }
    }

    public void setFadeSpeed(float FADE_SPEED) {
        this.FADE_SPEED = FADE_SPEED;
    }

    public void setEasingType(EasingType easingType) {
        this.easingType = easingType;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float getInterpolatedAlpha() {
        updateAlpha();
        return Easing.ease(easingType, alpha);
    }

    public EasingType getEasingType() {
        return easingType;
    }
}
