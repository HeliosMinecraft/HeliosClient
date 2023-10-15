package dev.heliosclient.util.animation;

import dev.heliosclient.managers.FontManager;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class AnimationUtils {
    public float FADE_SPEED = 0.05f;
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

    public void drawFadingBox(DrawContext context, int x, int y, int width, int height, int color, boolean RoundedBox, int radius) {
        if (fading) {
            alpha += fadeIn ? FADE_SPEED : -FADE_SPEED;
            if (alpha <= 0.0f || alpha >= 1.0f) {
                fading = false;
                alpha = Math.max(0.0f, Math.min(1.0f, alpha));
            }
        }
        float t = Easing.ease(easingType, alpha);
        int a = (int) (t * ColorUtils.getAlpha(color));
        int newColor = ColorUtils.changeAlpha(ColorUtils.intToColor(color), a).getRGB();
        if (!RoundedBox)
            Renderer2D.drawRectangle(context.getMatrices().peek().getPositionMatrix(), x, y, width, height, newColor);
        else
            Renderer2D.drawRoundedRectangle(context.getMatrices().peek().getPositionMatrix(), x, y, width, height, radius, newColor);
    }

    public void drawFadingText(MatrixStack matrixStack, String text, int x, int y, int color, boolean fixedSize) {
        if (fading) {
            alpha += fadeIn ? FADE_SPEED : -FADE_SPEED;
            if (alpha <= 0.0f || alpha >= 1.0f) {
                fading = false;
                alpha = Math.max(0.0f, Math.min(1.0f, alpha));
            }
        }
        float t = Easing.ease(easingType, alpha);
        int a = (int) (t * 255);
        if (a >= 255) {
            a = 254; // 255 makes alpha of text to 0 for some reason
        }
        Color nColor = ColorUtils.intToColor(color);
        Color newColor = ColorUtils.changeAlpha(nColor, a);
        if (fixedSize) {
            Renderer2D.drawFixedString(matrixStack, text, x, y, newColor.getRGB());
        } else {
            Renderer2D.drawString(matrixStack, text, x, y, newColor.getRGB());
        }
    }

    public void drawFadingAndPoppingBox(DrawContext drawContext, int x, int y, int width, int height, int color, boolean RoundedBox, int radius) {
        if (fading) {
            alpha += fadeIn ? FADE_SPEED : -FADE_SPEED;
            if (alpha <= 0.0f || alpha >= 1.0f) {
                fading = false;
                alpha = Math.max(0.0f, Math.min(1.0f, alpha));
            }
        }
        float t = Easing.ease(easingType, alpha);
        int a = (int) (t * 255);
        int newColor = ColorUtils.changeAlpha(ColorUtils.intToColor(color), a).getRGB();
        float scale = Easing.ease(easingType, alpha);
        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(x + width / 2f, y + height / 2f, 0);
        drawContext.getMatrices().scale(scale, scale, 0);
        if (!RoundedBox)
            Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), (int) (-width / 2f), (int) (-height / 2f), width, height, newColor);
        else
            Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), (int) (-width / 2f), (int) (-height / 2f), width, height, radius, newColor);
        drawContext.getMatrices().pop();
    }

    public void drawFadingAndPoppingText(DrawContext context, String text, int x, int y, int color, boolean fixedSize) {
        if (fading) {
            alpha += fadeIn ? FADE_SPEED : -FADE_SPEED;
            if (alpha <= 0.0f || alpha >= 1.0f) {
                fading = false;
                alpha = Math.max(0.0f, Math.min(1.0f, alpha));
            }
        }
        float t = Easing.ease(easingType, alpha);
        int a = (int) (t * 255);

        if (a >= 255) {
            a = 254; // 255 makes alpha of text to 0 for some reason
        }
        Color nColor = ColorUtils.intToColor(color);
        Color newColor = ColorUtils.changeAlpha(nColor, a);
        float scale = Easing.ease(easingType, alpha);
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(scale, scale, 0);
        if (fixedSize) {
            Renderer2D.drawFixedString(context.getMatrices(), text, -Renderer2D.getStringWidth(text) / 2, -FontManager.fontRenderer.getStringHeight(text) / 2, newColor.getRGB());
        } else {
            Renderer2D.drawString(context.getMatrices(), text, -Renderer2D.getStringWidth(text) / 2, -FontManager.fontRenderer.getStringHeight(text) / 2, newColor.getRGB());
        }
        context.getMatrices().pop();
    }
}

