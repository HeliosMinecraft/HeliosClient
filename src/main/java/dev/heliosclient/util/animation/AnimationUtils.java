package dev.heliosclient.util.animation;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.animation.toasts.ErrorToast;
import dev.heliosclient.util.animation.toasts.InfoToast;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Unstable to use now.
 * Todo: Update and optimise
 */
public class AnimationUtils {
    public static void addErrorToast(String message, boolean hasProgressBar, long endDelay) {
        Toast toast = new ErrorToast(message, hasProgressBar, endDelay);
        if (HeliosClient.MC.getToastManager() != null && toast != null) {
            HeliosClient.MC.getToastManager().add(toast);
        }
    }

    public static void addInfoToast(String message, boolean hasProgressBar, long endDelay) {
        Toast toast = new InfoToast(message, hasProgressBar, endDelay);
        if (HeliosClient.MC.getToastManager() != null && toast != null) {
            HeliosClient.MC.getToastManager().add(toast);
        }
    }

    public static float lerp(float point1, float point2, float alpha) {
        return (1 - alpha) * point1 + alpha * point2;
    }

    public static int getFadingColor(int color, float alpha) {
        int a = (int) (alpha * ColorUtils.getAlpha(color));
        return ColorUtils.changeAlpha(ColorUtils.intToColor(color), a).getRGB();
    }

    public static void drawFadingBox(DrawContext context, Animation animation, float x, float y, float width, float height, int color, boolean roundedBox, float radius) {
        int newColor = getFadingColor(color, animation.getInterpolatedAlpha());
        if (!roundedBox)
            Renderer2D.drawRectangle(context.getMatrices().peek().getPositionMatrix(), x, y, width, height, newColor);
        else
            Renderer2D.drawRoundedRectangle(context.getMatrices().peek().getPositionMatrix(), x, y, width, height, radius, newColor);
    }


    public static void drawFadingText(MatrixStack matrixStack, Animation animation, String text, float x, float y, int color, boolean fixedSize) {
        int newColor = getFadingColor(color, animation.getInterpolatedAlpha());

        if (fixedSize) {
            Renderer2D.drawFixedString(matrixStack, text, x, y, newColor);
        } else {
            Renderer2D.drawString(matrixStack, text, x, y, newColor);
        }
    }

    public static void drawFadingAndPoppingBox(DrawContext drawContext, Animation animation, float x, float y, float width, float height, int color, boolean RoundedBox, float radius) {
        int newColor = getFadingColor(color, animation.getInterpolatedAlpha());

        float scale = Easing.ease(animation.getEasingType(), animation.getInterpolatedAlpha());
        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(x + width / 2f, y + height / 2f, 0);
        drawContext.getMatrices().scale(scale, scale, 0);
        if (!RoundedBox)
            Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), (int) (-width / 2f), (int) (-height / 2f), width, height, newColor);
        else
            Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), (int) (-width / 2f), (int) (-height / 2f), width, height, radius, newColor);
        drawContext.getMatrices().pop();
    }

    public static void drawFadingAndPoppingText(DrawContext context, Animation animation, String text, float x, float y, int color, boolean fixedSize) {
        int newColor = getFadingColor(color, animation.getInterpolatedAlpha());

        float scale = Easing.ease(animation.getEasingType(), animation.getInterpolatedAlpha());
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
