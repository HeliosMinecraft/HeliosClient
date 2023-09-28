package dev.heliosclient.util.animation;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
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
        int a = (int) (t * 255);
        int newColor = ColorUtils.changeAlpha(ColorUtils.intToColor(color), a).getRGB();
        if (!RoundedBox)
            Renderer2D.drawRectangle(context, x, y, width, height, newColor);
        else
            Renderer2D.drawRoundedRectangle(context, x, y, width, height, radius, newColor);
    }

    public void drawFadingText(MatrixStack matrixStack, String text, int x, int y, int color , boolean fixedSize) {
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
        if(fixedSize){
            FontManager.fxfontRenderer.drawString(matrixStack, text, x, y, 256 - newColor.getRed(), 256 - nColor.getGreen(), 256 - newColor.getBlue(), 256 - newColor.getAlpha());
        }else {
            FontManager.fontRenderer.drawString(matrixStack, text, x, y, 256 - newColor.getRed(), 256 - nColor.getGreen(), 256 - newColor.getBlue(), 256 - newColor.getAlpha());
        }
    }
    public void drawFadingAndPoppingBox(DrawContext context, int x, int y, int width, int height, int color, boolean RoundedBox, int radius) {
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
        context.getMatrices().push();
        context.getMatrices().translate(x + width / 2f, y + height / 2f, 0);
        context.getMatrices().scale(scale, scale, 0);
        if (!RoundedBox)
            Renderer2D.drawRectangle(context, (int) (-width / 2f), (int) (-height / 2f), width, height, newColor);
        else
            Renderer2D.drawRoundedRectangle(context, (int) (-width / 2f), (int) (-height / 2f), width, height, radius, newColor);
        context.getMatrices().pop();
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
        if(fixedSize) {
            FontManager.fxfontRenderer.drawString(context.getMatrices(), text, -FontManager.fontRenderer.getStringWidth(text) / 2, -FontManager.fontRenderer.getStringHeight(text) / 2, 256 - newColor.getRed(), 256 - nColor.getGreen(), 256 - newColor.getBlue(), 256 - newColor.getAlpha());
        }
        else {
            FontManager.fontRenderer.drawString(context.getMatrices(), text, -FontManager.fontRenderer.getStringWidth(text) / 2, -FontManager.fontRenderer.getStringHeight(text) / 2, 256 - newColor.getRed(), 256 - nColor.getGreen(), 256 - newColor.getBlue(), 256 - newColor.getAlpha());
        }
        context.getMatrices().pop();
    }
}

