package dev.heliosclient.util.animation;

import dev.heliosclient.util.ColorUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public class AnimationUtils {
    public float FADE_SPEED = 0.1f;
    private float alpha = 1.0f;
    private boolean fading = false;
    private boolean fadeIn = false;
    private EasingType easingType = EasingType.LINEAR_IN;

    public void startFading(boolean fadeIn, EasingType easingType) {
        fading = true;
        this.fadeIn = fadeIn;
        this.easingType = easingType;
        alpha = fadeIn ? 0.0f : 1.0f;
    }
    public void drawFadingBox(DrawContext context, int x, int y, int width, int height, int color) {
        if (fading) {
            float t = alpha;
            alpha = Easing.ease(easingType, t);
            alpha += fadeIn ? FADE_SPEED : -FADE_SPEED;
            if (alpha <= 0.0f || alpha >= 1.0f) {
                fading = false;
                alpha = Math.max(0.0f, Math.min(1.0f, alpha));
            }
        }
        int a = (int) (alpha * 255);
        int newColor = ColorUtils.changeAlpha(ColorUtils.intToColor(color), a).getRGB();
        context.fill(x, y, x + width, y + height, newColor);
    }

    public void drawFadingText(DrawContext context, TextRenderer textRenderer, String text, int x, int y, int color,boolean shadow) {
            if (fading) {
                float t = alpha;
                alpha = Easing.ease(easingType, t);
                alpha += fadeIn ? FADE_SPEED : -FADE_SPEED;
                if (alpha <= 0.0f || alpha >= 1.0f) {
                    fading = false;
                    alpha = Math.max(0.0f, Math.min(1.0f, alpha));
                }
            }
            int a = (int) (alpha * 255);
            if(a>=255)
            {
                a=254; // 255 makes alpha of text to 0 for some reason
            }
            Color nColor = ColorUtils.intToColor(color);
            int newColor = ColorUtils.changeAlpha(nColor, a).getRGB();
            context.drawText(textRenderer, text, x, y, newColor, shadow);
    }
}

