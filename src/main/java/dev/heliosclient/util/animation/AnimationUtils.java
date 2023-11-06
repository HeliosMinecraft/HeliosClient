package dev.heliosclient.util.animation;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.animation.toasts.ErrorToast;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class AnimationUtils implements Listener {
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

    public static void addErrorToast(String message, boolean hasProgressBar, long endDelay) {
        HeliosClient.MC.getToastManager().add(new ErrorToast(message, hasProgressBar, endDelay));
    }

    public void updateAlpha() {
        if (fading) {
            alpha += fadeIn ? FADE_SPEED : -FADE_SPEED;
            if (alpha <= 0.0f || alpha >= 1.0f) {
                fading = false;
                alpha = Math.max(0.0f, Math.min(1.0f, alpha));
            }
        }
    }

    public int getFadingColor(int color) {
        updateAlpha();
        float t = Easing.ease(easingType, alpha);
        int a = (int) (t * ColorUtils.getAlpha(color));
        return ColorUtils.changeAlpha(ColorUtils.intToColor(color), a).getRGB();
    }

    public static float lerp(float point1, float point2, float alpha) {
        return (1 - alpha) * point1 + alpha * point2;
    }

    public void drawFadingBox(DrawContext context, float x, float y, float width, float height, int color, boolean RoundedBox, float radius) {
        int newColor = getFadingColor(color);
        if (!RoundedBox)
            Renderer2D.drawRectangle(context.getMatrices().peek().getPositionMatrix(), x, y, width, height, newColor);
        else
            Renderer2D.drawRoundedRectangle(context.getMatrices().peek().getPositionMatrix(), x, y, width, height, radius, newColor);
    }

    public void drawFadingText(MatrixStack matrixStack, String text, float x, float y, int color, boolean fixedSize) {
        int newColor = getFadingColor(color);

        if (fixedSize) {
            Renderer2D.drawFixedString(matrixStack, text, x, y, newColor);
        } else {
            Renderer2D.drawString(matrixStack, text, x, y, newColor);
        }
    }

    public void drawFadingAndPoppingBox(DrawContext drawContext, float x, float y, float width, float height, int color, boolean RoundedBox, float radius) {
        int newColor = getFadingColor(color);

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

    public void drawFadingAndPoppingText(DrawContext context, String text, float x, float y, int color, boolean fixedSize) {
        int newColor = getFadingColor(color);

        float scale = Easing.ease(easingType, alpha);
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(scale, scale, 0);
        if (fixedSize) {
            Renderer2D.drawFixedString(context.getMatrices(), text, -Renderer2D.getStringWidth(text) / 2, -Renderer2D.getFxStringHeight(text) / 2, newColor);
        } else {
            Renderer2D.drawString(context.getMatrices(), text, -Renderer2D.getStringWidth(text) / 2, -Renderer2D.getStringHeight(text) / 2, newColor);
        }
        context.getMatrices().pop();
    }



}

