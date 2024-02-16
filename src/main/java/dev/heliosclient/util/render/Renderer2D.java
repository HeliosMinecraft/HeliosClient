package dev.heliosclient.util.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.render.RenderEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.animation.KeyframeAnimation;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.fontutils.fxFontRenderer;
import me.x150.renderer.font.FontRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL40C;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static me.x150.renderer.render.Renderer2d.renderTexture;
import static me.x150.renderer.util.RendererUtils.registerBufferedImageTexture;

public class Renderer2D implements Listener {
    private static final int NUM_LINES = 3;
    private static final KeyframeAnimation[] animations = new KeyframeAnimation[NUM_LINES];
    public static Renderer2D INSTANCE = new Renderer2D();

    // static  AnimatedOutlineBox outlineBox = new AnimatedOutlineBox(30,30,5,3,50);
    public Renderer2D() {
        for (int i = 0; i < NUM_LINES; i++) {
            animations[i] = new KeyframeAnimation();
            animations[i].addKeyframe(0, 0);
            animations[i].addKeyframe(1, 1);
        }
    }
    public enum Direction {
        // Left_Right means from left to right. Same for others //
        LEFT_RIGHT, TOP_BOTTOM, RIGHT_LEFT, BOTTOM_TOP
    }

    /**
     * Draws a singular gradient rectangle  on screen with the given parameters
     *
     * @param matrix4f   Matrix4f object to draw the gradient
     * @param x          X position of the gradient
     * @param y          Y position of the gradient
     * @param width      Width of the gradient
     * @param height     Height of the gradient
     * @param startColor start color of the gradient
     * @param endColor   end color of the gradient
     * @param direction  Draws the gradient in the given direction
     */
    public static void drawGradient(Matrix4f matrix4f, float x, float y, float width, float height, int startColor, int endColor, Direction direction) {
        float startRed = (float) (startColor >> 16 & 255) / 255.0F;
        float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
        float startBlue = (float) (startColor & 255) / 255.0F;
        float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;

        float endRed = (float) (endColor >> 16 & 255) / 255.0F;
        float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
        float endBlue = (float) (endColor & 255) / 255.0F;
        float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        switch (direction) {
            case LEFT_RIGHT:
                bufferBuilder.vertex(matrix4f, x, y + height, 0.0F).color(startRed, startGreen, startBlue, startAlpha).next();
                bufferBuilder.vertex(matrix4f, x + width, y + height, 0.0F).color(endRed, endGreen, endBlue, endAlpha).next();
                bufferBuilder.vertex(matrix4f, x + width, y, 0.0F).color(endRed, endGreen, endBlue, endAlpha).next();
                bufferBuilder.vertex(matrix4f, x, y, 0.0F).color(startRed, startGreen, startBlue, startAlpha).next();
                break;
            case TOP_BOTTOM:
                bufferBuilder.vertex(matrix4f, x, y + height, 0.0F).color(endRed, endGreen, endBlue, endAlpha).next();
                bufferBuilder.vertex(matrix4f, x + width, y + height, 0.0F).color(endRed, endGreen, endBlue, endAlpha).next();
                bufferBuilder.vertex(matrix4f, x + width, y, 0.0F).color(startRed, startGreen, startBlue, startAlpha).next();
                bufferBuilder.vertex(matrix4f, x, y, 0.0F).color(startRed, startGreen, startBlue, startAlpha).next();
                break;
            case RIGHT_LEFT:
                bufferBuilder.vertex(matrix4f, x, y + height, 0.0F).color(endRed, endGreen, endBlue, endAlpha).next();
                bufferBuilder.vertex(matrix4f, x + width, y + height, 0.0F).color(startRed, startGreen, startBlue, startAlpha).next();
                bufferBuilder.vertex(matrix4f, x + width, y, 0.0F).color(startRed, startGreen, startBlue, startAlpha).next();
                bufferBuilder.vertex(matrix4f, x, y, 0.0F).color(endRed, endGreen, endBlue, endAlpha).next();
                break;
            case BOTTOM_TOP:
                bufferBuilder.vertex(matrix4f, x, y + height, 0.0F).color(startRed, startGreen, startBlue, startAlpha).next();
                bufferBuilder.vertex(matrix4f, x + width, y + height, 0.0F).color(startRed, startGreen, startBlue, startAlpha).next();
                bufferBuilder.vertex(matrix4f, x + width, y, 0.0F).color(endRed, endGreen, endBlue, endAlpha).next();
                bufferBuilder.vertex(matrix4f, x, y, 0.0F).color(endRed, endGreen, endBlue, endAlpha).next();
                break;
        }

        tessellator.draw();

        RenderSystem.disableBlend();
    }
    private static final String TEXT = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-=_+|{};<>?~`,./;'[] ";
    public static DrawContext drawContext;
    public static Renderers renderer = Renderers.CUSTOM;
    public static HashMap<Integer, BlurredShadow> shadowCache = new HashMap<>();

    public static void enableScissor(int x, int y, int width, int height) {
        double scaleFactor = HeliosClient.MC.getWindow().getScaleFactor();

        int scissorX = (int) (x * scaleFactor);
        int scissorY = (int) (HeliosClient.MC.getWindow().getHeight() - ((y + height) * scaleFactor));
        int scissorWidth = (int) (width * scaleFactor);
        int scissorHeight = (int) (height * scaleFactor);

        RenderSystem.enableScissor(scissorX, scissorY, scissorWidth, scissorHeight);
    }

    public static void disableScissor() {
        RenderSystem.disableScissor();
    }

    /* ==== Drawing Rectangles ==== */

    /**
     * Draws a singular rectangle on screen with the given parameters
     *
     * @param matrix4f Matrix4f object to draw the rectangle
     * @param x        X position of the rectangle
     * @param y        Y position of the rectangle
     * @param width    Width of the rectangle
     * @param height   Height of the rectangle
     * @param color    Color of the rectangle
     */
    public static void drawRectangle(Matrix4f matrix4f, float x, float y, float width, float height, int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix4f, x, y + height, 0.0F).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(matrix4f, x + width, y + height, 0.0F).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(matrix4f, x + width, y, 0.0F).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(matrix4f, x, y, 0.0F).color(red, green, blue, alpha).next();

        tessellator.draw();

        RenderSystem.disableBlend();
    }

    /**
     * Draws a singular outline rectangle on screen with the given parameters
     *
     * @param matrix4f Matrix4f object to draw the rectangle
     * @param x        X position of the rectangle
     * @param y        Y position of the rectangle
     * @param width    Width of the rectangle
     * @param height   Height of the rectangle
     * @param color    Color of the rectangle
     */
    public static void drawOutlineBox(Matrix4f matrix4f, float x, float y, float width, float height, float thickness, int color) {
        drawRectangle(matrix4f, x, y, width, thickness, color);
        drawRectangle(matrix4f, x, y + height - thickness, width, thickness, color);
        drawRectangle(matrix4f, x, y + thickness, thickness, height - thickness * 2, color);
        drawRectangle(matrix4f, x + width - thickness, y + thickness, thickness, height - thickness * 2, color);
    }


    /**
     * Draws a singular rectangle with a dark shadow on screen with the given parameters
     * Bad way because there is a better way
     *
     * @param matrix4f      Matrix4f object to draw the rectangle and shadow
     * @param x             X position of the rectangle
     * @param y             Y position of the rectangle
     * @param width         Width of the rectangle
     * @param height        Height of the rectangle
     * @param color         Color of the rectangle
     * @param shadowOpacity Opacity of the shadow (Dark --> Lighter)
     * @param shadowOffsetX X position Offset of the shadow from the main rectangle X pos
     * @param shadowOffsetY Y position Offset of the shadow from the main rectangle Y pos
     */
    public static void drawRectangleWithShadowBadWay(Matrix4f matrix4f, float x, float y, float width, float height, int color, int shadowOpacity, float shadowOffsetX, float shadowOffsetY) {
        // First, render the shadow
        drawRectangle(matrix4f, x + shadowOffsetX, y + shadowOffsetY, width, height, ColorUtils.rgbaToInt(0, 0, 0, shadowOpacity));

        // Then, render the rectangle
        drawRectangle(matrix4f, x, y, width, height, color);
    }

    /**
     * Draws a singular rectangle with a shadow on screen with the given parameters
     *
     * @param matrices   MatrixStack object to draw the rectangle and shadow
     * @param x          X position of the rectangle
     * @param y          Y position of the rectangle
     * @param width      Width of the rectangle
     * @param height     Height of the rectangle
     * @param color      Color of the rectangle
     * @param blurRadius blur radius of the shadow for gaussian blur algorithm
     */
    public static void drawRectangleWithShadow(MatrixStack matrices, float x, float y, float width, float height, int color, int blurRadius) {
        //Shadow
        drawBlurredShadow(matrices, x, y, width, height, blurRadius, new Color(color));

        //Rectangle
        drawRectangle(matrices.peek().getPositionMatrix(), x, y, width, height, color);
    }

    /**
     * Draws a singular gradient rectangle with a shadow on screen with the given parameters
     *
     * @param matrices   MatrixStack object to draw the gradient
     * @param x          X position of the gradient
     * @param y          Y position of the gradient
     * @param width      Width of the gradient
     * @param height     Height of the gradient
     * @param blurRadius blur radius of the shadow for gaussian blur algorithm
     * @param startColor start color of the gradient
     * @param endColor   end color of the gradient
     */
    public static void drawGradientWithShadow(MatrixStack matrices, float x, float y, float width, float height, int blurRadius, int startColor, int endColor, Direction direction) {
        drawBlurredShadow(matrices, x, y, width, height, blurRadius, new Color(startColor));

        drawGradient(matrices.peek().getPositionMatrix(), x, y, width, height, startColor, endColor, direction);
    }

    /**
     * Draws an outline rounded rectangle by drawing 4 side rectangles, and 4 arcs
     *
     * @param matrix4f  Matrix4f object to draw the rounded rectangle
     * @param x         X pos
     * @param y         Y pos
     * @param width     Width of rounded rectangle
     * @param height    Height of rounded rectangle
     * @param radius    Radius of the quadrants / the rounded rectangle
     * @param color     Color of the rounded rectangle
     * @param thickness thickness of the outline
     */
    public static void drawOutlineRoundedBox(Matrix4f matrix4f, float x, float y, float width, float height, float radius, float thickness, int color) {
        // Draw the rectangles for the outline
        drawRectangle(matrix4f, x + radius, y, width - radius * 2, thickness, color); // Top rectangle
        drawRectangle(matrix4f, x + radius, y + height - thickness, width - radius * 2, thickness, color); // Bottom rectangle
        drawRectangle(matrix4f, x, y + radius, thickness, height - radius * 2, color); // Left rectangle
        drawRectangle(matrix4f, x + width - thickness, y + radius, thickness, height - radius * 2, color); // Right rectangle

        // Draw the arcs at the corners for the outline
        drawArc(matrix4f, x + radius, y + radius, radius, thickness, color, 180, 270); // Top-left arc
        drawArc(matrix4f, x + width - radius, y + radius, radius, thickness, color, 90, 180); // Top-right arc
        drawArc(matrix4f, x + width - radius, y + height - radius, radius, thickness, color, 0, 90); // Bottom-right arc
        drawArc(matrix4f, x + radius, y + height - radius, radius, thickness, color, 270, 360); // Bottom-left arc
    }

    public static void drawRainbowGradientRectangle(Matrix4f matrix4f, float x, float y, float width, float height) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (int i = 0; i < width; i++) {
            float hue = (i / width); // Multiply by 1 to go through the whole color spectrum once (red to red)
            Color color = Color.getHSBColor(hue, 1.0f, 1.0f); // Full saturation and brightness

            bufferBuilder.vertex(matrix4f, x + i, y, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
            bufferBuilder.vertex(matrix4f, x + i + 1, y, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
            bufferBuilder.vertex(matrix4f, x + i + 1, y + height, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
            bufferBuilder.vertex(matrix4f, x + i, y + height, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        }

        Tessellator.getInstance().draw();

        RenderSystem.disableBlend();
    }

    public static void drawRainbowGradient(Matrix4f matrix, float x, float y, float width, float height) {
        RenderSystem.enableBlend();
        RenderSystem.colorMask(false, false, false, true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT, false);
        RenderSystem.colorMask(true, true, true, true);

        drawRectangle(matrix, x, y, width, height, Color.BLACK.getRGB());

        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (float i = 0; i < width; i += 1.0f) {
            float hue = (i / width); // Multiply by 1 to go through the whole color spectrum once (red to red)
            Color color = Color.getHSBColor(hue, 1.0f, 1.0f); // Full saturation and brightness

            bufferBuilder.vertex(matrix, x + i, y, 0.0F).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
            bufferBuilder.vertex(matrix, x + i + 1.0f, y, 0.0F).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
            bufferBuilder.vertex(matrix, x + i + 1.0f, y + height, 0.0F).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
            bufferBuilder.vertex(matrix, x + i, y + height, 0.0F).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();

        RenderSystem.defaultBlendFunc();
    }

    /* ==== Drawing Blurred Shadow ==== */

    /**
     * Draws a singular blurred shadow using the GaussianBlur algorithm on screen with the given parameters
     * Credits:  <a href="https://github.com/Pan4ur/ThunderHack-Recode/blob/main/src/main/java/thunder/hack/utility/render/Render2DEngine.java#L187">Thunderhack</a>
     *
     * @param matrices   MatrixStack object to draw the blurred shadow
     * @param x          X position of the blurred shadow
     * @param y          Y position of the blurred shadow
     * @param width      Width of the blurred shadow
     * @param height     Height of the blurred shadow
     * @param blurRadius blur radius of the shadow for gaussian blur algorithm
     * @param color      color of the blurred shadow
     */

    public static void drawBlurredShadow(MatrixStack matrices, float x, float y, float width, float height, int blurRadius, Color color) {
        width = width + blurRadius * 2;
        height = height + blurRadius * 2;
        x = x - blurRadius;
        y = y - blurRadius;

        int identifier = (int) (width * height + width * blurRadius);
        if (shadowCache.containsKey(identifier)) {
            shadowCache.get(identifier).bind();
            RenderSystem.defaultBlendFunc();
        } else {
            BufferedImage original = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);
            Graphics g = original.getGraphics();
            g.setColor(new Color(-1));
            g.fillRect(blurRadius, blurRadius, (int) (width - blurRadius * 2), (int) (height - blurRadius * 2));
            g.dispose();
            GaussianBlur op = new GaussianBlur(blurRadius);
            BufferedImage blurred = op.applyFilter(original, null);
            shadowCache.put(identifier, new BlurredShadow(blurred));
            return;
        }

        RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        RenderSystem.enableBlend();
        renderTexture(matrices, x, y, width, height, 0, 0, width, height, width, height);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    /**
     * Draws a circular blurred shadow using the GaussianBlur algorithm on screen with the given parameters
     *
     * @param matrices   MatrixStack object to draw the blurred shadow
     * @param xCenter    X position of the blurred shadow
     * @param yCenter    Y position of the blurred shadow
     * @param radius     radius of the circle of the blurred shadow
     * @param color      color of the blurred shadow
     * @param blurRadius blur radius of the shadow for gaussian blur algorithm
     */
    public static void drawCircularBlurredShadow(MatrixStack matrices, float xCenter, float yCenter, float radius, Color color, int blurRadius) {
        // Calculate the size of the shadow image
        int diameter = (int) (radius * 2);
        int shadowWidth = diameter + blurRadius * 2;
        int shadowHeight = diameter + blurRadius * 2;

        int identifier = shadowWidth * shadowHeight + shadowWidth * blurRadius;
        if (shadowCache.containsKey(identifier)) {
            shadowCache.get(identifier).bind();
            RenderSystem.defaultBlendFunc();
        } else {
            BufferedImage original = new BufferedImage(shadowWidth, shadowHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = original.createGraphics();
            g.setColor(new Color(-1));
            g.fillOval(blurRadius, blurRadius, diameter, diameter); // Draw a circle instead of a rectangle
            g.dispose();
            GaussianBlur op = new GaussianBlur(blurRadius);
            BufferedImage blurred = op.applyFilter(original, null);
            shadowCache.put(identifier, new BlurredShadow(blurred));
            return;
        }

        RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        RenderSystem.enableBlend();
        renderTexture(matrices, xCenter - radius, yCenter - radius, shadowWidth, shadowHeight, 0, 0, shadowWidth, shadowHeight, shadowWidth, shadowHeight);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }


    /* ====  Drawing filled and outline circles  ==== */

    /**
     * Draws an outline of a circle
     *
     * @param matrix4f Matrix4f object to draw the circle outline
     * @param xCenter  X position of the circle outline
     * @param yCenter  Y position of the circle outline
     * @param radius   radius of the circle outline
     * @param color    color of the circle outline
     */
    public static void drawCircle(Matrix4f matrix4f, float xCenter, float yCenter, float radius, float lineWidth, int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        for (int i = 0; i <= 360; i++) {
            double x = xCenter + Math.sin(Math.toRadians(i)) * radius;
            double y = yCenter + Math.cos(Math.toRadians(i)) * radius;
            double x2 = xCenter + Math.sin(Math.toRadians(i)) * (radius + lineWidth);
            double y2 = yCenter + Math.cos(Math.toRadians(i)) * (radius + lineWidth);
            bufferBuilder.vertex(matrix4f, (float) x, (float) y, 0).color(red, green, blue, alpha).next();
            bufferBuilder.vertex(matrix4f, (float) x2, (float) y2, 0).color(red, green, blue, alpha).next();
        }


        tessellator.draw();
    }

    /**
     * Draws a filled circle
     *
     * @param matrix4f Matrix4f object to draw the circle outline
     * @param xCenter  X position of the circle outline
     * @param yCenter  Y position of the circle outline
     * @param radius   radius of the circle outline
     * @param color    color of the circle outline
     */
    public static void drawFilledCircle(Matrix4f matrix4f, float xCenter, float yCenter, float radius, int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);


        bufferBuilder.vertex(matrix4f, xCenter, yCenter, 0).color(red, green, blue, alpha).next();

        for (int i = 0; i <= 360; i++) {
            double x = xCenter + Math.sin(Math.toRadians(i)) * radius;
            double y = yCenter + Math.cos(Math.toRadians(i)) * radius;
            bufferBuilder.vertex(matrix4f, (float) x, (float) y, 0).color(red, green, blue, alpha).next();
        }

        tessellator.draw();
        RenderSystem.disableBlend();
    }

    /**
     * Draws a filled circle with a shadow bad way
     *
     * @param matrix4f      Matrix4f object to draw the circle
     * @param xCenter       X position of the circle
     * @param yCenter       Y position of the circle
     * @param radius        Radius of the circle
     * @param color         Color of the circle
     * @param shadowOffsetX X position of the circle shadow offset from main circle
     * @param shadowOffsetY X position of the circle shadow offset from main circle
     * @param shadowOpacity Opacity of the circle shadow offset from main circle
     */
    public static void drawCircleWithShadowBadWay(Matrix4f matrix4f, float xCenter, float yCenter, float radius, int color, int shadowOpacity, float shadowOffsetX, float shadowOffsetY) {
        // First, render the shadow
        drawFilledCircle(matrix4f, xCenter + shadowOffsetX, yCenter + shadowOffsetY, radius, ColorUtils.rgbaToInt(0, 0, 0, shadowOpacity));

        // Then, render the circle
        drawFilledCircle(matrix4f, xCenter, yCenter, radius, color);
    }

    /**
     * Draws a filled circle with a shadow
     *
     * @param matrices   MatrixStack object to draw the circle
     * @param xCenter    X position of the circle
     * @param yCenter    Y position of the circle
     * @param radius     Radius of the circle
     * @param color      Color of the circle
     * @param blurRadius blur radius of the shadow for gaussian blur algorithm
     */
    public static void drawCircleWithShadow(MatrixStack matrices, float xCenter, float yCenter, float radius, int blurRadius, int color) {
        drawCircularBlurredShadow(matrices, xCenter, yCenter, radius, new Color(color), blurRadius);

        drawFilledCircle(matrices.peek().getPositionMatrix(), xCenter, yCenter, radius, color);
    }

    /* ====  Drawing Quadrants, Arcs, and Triangles  ==== */

    /**
     * Not Tested
     *
     * @param matrix4f
     * @param x
     * @param y
     * @param radius
     * @param startAngle
     * @param endAngle
     * @param color
     */
    @Deprecated
    public static void drawFilledArc(Matrix4f matrix4f, float x, float y, float radius, float startAngle, float endAngle, int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;


        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        RenderSystem.enableBlend();

        for (float angle = startAngle; angle <= endAngle; angle += 1.0F) {
            float x1 = x + MathHelper.cos(angle * 0.017453292F) * radius;
            float y1 = y + MathHelper.sin(angle * 0.017453292F) * radius;
            float x2 = x + MathHelper.cos((angle + 1.0F) * 0.017453292F) * radius;
            float y2 = y + MathHelper.sin((angle + 1.0F) * 0.017453292F) * radius;

            bufferBuilder.vertex(matrix4f, x, y, 0).color(red, green, blue, alpha).next();
            bufferBuilder.vertex(matrix4f, x1, y1, 0).color(red, green, blue, alpha).next();
            bufferBuilder.vertex(matrix4f, x2, y2, 0).color(red, green, blue, alpha).next();
        }
        tessellator.draw();
        RenderSystem.disableBlend();
    }

    /**
     * Draws a filled Gradient quadrant
     *
     * @param matrix4f   Matrix4f object to draw the quadrant
     * @param xCenter    X position of the quadrant
     * @param yCenter    Y position of the quadrant
     * @param radius     Radius of the quadrant
     * @param startColor start color of the gradient
     * @param endColor   end color of the gradient
     * @param quadrant   Integer value of the quadrant of the circle. 1 == Top Right, 2 == Top Left, 3 == Bottom Right, 4 == Bottom Left
     */
    public static void drawFilledGradientQuadrant(Matrix4f matrix4f, float xCenter, float yCenter, float radius, int startColor, int endColor, int quadrant) {
        float startRed = (float) (startColor >> 16 & 255) / 255.0F;
        float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
        float startBlue = (float) (startColor & 255) / 255.0F;
        float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;

        float endRed = (float) (endColor >> 16 & 255) / 255.0F;
        float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
        float endBlue = (float) (endColor & 255) / 255.0F;
        float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        RenderSystem.enableBlend();

        bufferBuilder.vertex(matrix4f, xCenter, yCenter, 0).color(startRed, startGreen, startBlue, startAlpha).next();

        for (int i = quadrant * 90; i <= quadrant * 90 + 90; i++) {
            double x = xCenter + Math.sin(Math.toRadians(i)) * radius;
            double y = yCenter + Math.cos(Math.toRadians(i)) * radius;

            // Interpolate the color based on the angle
            float t = (float) (i - quadrant * 90) / 90.0f;
            float red = startRed * (1 - t) + endRed * t;
            float green = startGreen * (1 - t) + endGreen * t;
            float blue = startBlue * (1 - t) + endBlue * t;
            float alpha = startAlpha * (1 - t) + endAlpha * t;

            bufferBuilder.vertex(matrix4f, (float) x, (float) y, 0).color(red, green, blue, alpha).next();
        }

        tessellator.draw();
        RenderSystem.disableBlend();
    }

    /**
     * Draws an arc
     *
     * @param matrix4f   Matrix4f object to draw the arc
     * @param xCenter    X position of the arc's center
     * @param yCenter    Y position of the arc's center
     * @param radius     Radius of the arc's center circle
     * @param startAngle start Angle of the arc
     * @param endAngle   end Angle of the arc
     * @param thickness  Thickness of the arc (width of the arc)
     */
    public static void drawArc(Matrix4f matrix4f, float xCenter, float yCenter, float radius, float thickness, int color, int startAngle, int endAngle) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        RenderSystem.enableBlend();

        for (int i = startAngle; i <= endAngle; i++) {
            double innerX = xCenter + Math.sin(Math.toRadians(i)) * (radius - thickness);
            double innerY = yCenter + Math.cos(Math.toRadians(i)) * (radius - thickness);
            double outerX = xCenter + Math.sin(Math.toRadians(i)) * radius;
            double outerY = yCenter + Math.cos(Math.toRadians(i)) * radius;

            bufferBuilder.vertex(matrix4f, (float) innerX, (float) innerY, 0).color(red, green, blue, alpha).next();
            bufferBuilder.vertex(matrix4f, (float) outerX, (float) outerY, 0).color(red, green, blue, alpha).next();
        }

        tessellator.draw();

        RenderSystem.disableBlend();
    }

    /**
     * Draws a filled quadrant
     *
     * @param matrix4f Matrix4f object to draw the quadrant
     * @param xCenter  X position of the quadrant
     * @param yCenter  Y position of the quadrant
     * @param radius   Radius of the quadrant
     * @param color    color of the quadrant
     * @param quadrant Integer value of the quadrant of the circle. 1 == Top Right, 2 == Top Left, 3 == Bottom Right, 4 == Bottom Left
     */
    public static void drawFilledQuadrant(Matrix4f matrix4f, float xCenter, float yCenter, float radius, int color, int quadrant) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        RenderSystem.enableBlend();

        bufferBuilder.vertex(matrix4f, xCenter, yCenter, 0).color(red, green, blue, alpha).next();

        for (int i = quadrant * 90; i <= quadrant * 90 + 90; i++) {
            double x = xCenter + Math.sin(Math.toRadians(i)) * radius;
            double y = yCenter + Math.cos(Math.toRadians(i)) * radius;
            bufferBuilder.vertex(matrix4f, (float) x, (float) y, 0).color(red, green, blue, alpha).next();
        }

        tessellator.draw();
        RenderSystem.disableBlend();

    }

    /**
     * Draws a Triangle with the given coordinates
     *
     * @param matrix4f
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param x3
     * @param y3
     * @param color
     */
    public static void drawTriangle(Matrix4f matrix4f, int x1, int y1, int x2, int y2, int x3, int y3, int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix4f, x1, y1, 0).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(matrix4f, x2, y2, 0).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(matrix4f, x3, y3, 0).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(matrix4f, x1, y1, 0).color(red, green, blue, alpha).next();

        tessellator.draw();
    }

    /**
     * Draws a outline quadrant
     *
     * @param matrix4f Matrix4f object to draw the quadrant
     * @param xCenter  X position of the quadrant
     * @param yCenter  Y position of the quadrant
     * @param radius   Radius of the quadrant
     * @param color    color of the quadrant
     * @param quadrant Integer value of the quadrant of the circle. 1 == Top Right, 2 == Top Left, 3 == Bottom Right, 4 == Bottom Left
     */
    public static void drawQuadrant(Matrix4f matrix4f, float xCenter, float yCenter, float radius, int quadrant, int color) {
        int startAngle = 0;
        int endAngle = 0;

        if (quadrant == 1) {
            startAngle = 270;
            endAngle = 360;
        } else if (quadrant == 2) {
            startAngle = 180;
            endAngle = 270;
        } else if (quadrant == 3) {
            startAngle = 90;
            endAngle = 180;
        } else if (quadrant == 4) {
            endAngle = 90;
        }

        drawArc(matrix4f, xCenter, yCenter, radius, 1f, color, startAngle, endAngle);
    }

    /* ====  Drawing Rounded Rectangles  ==== */

    /**
     * Draws a filled rounded rectangle by drawing 1 main rectangle, 4 side rectangles, and 4 filled quadrants
     *
     * @param matrix4f Matrix4f object to draw the rounded rectangle
     * @param x        X pos
     * @param y        Y pos
     * @param width    Width of rounded rectangle
     * @param height   Height of rounded rectangle
     * @param radius   Radius of the quadrants / the rounded rectangle
     * @param color    Color of the rounded rectangle
     */
    public static void drawRoundedRectangle(Matrix4f matrix4f, float x, float y, float width, float height, float radius, int color) {
        // Draw the main rectangle
        drawRectangle(matrix4f, x + radius, y + radius, width - 2 * radius, height - 2 * radius, color);

        // Draw rectangles at the sides
        drawRectangle(matrix4f, x + radius, y, width - 2 * radius, radius, color); // top
        drawRectangle(matrix4f, x + radius, y + height - radius, width - 2 * radius, radius, color); // bottom
        drawRectangle(matrix4f, x, y + radius, radius, height - 2 * radius, color); // left
        drawRectangle(matrix4f, x + width - radius, y + radius, radius, height - 2 * radius, color); // right

        // Draw quadrants at the corners
        drawFilledQuadrant(matrix4f, x + radius, y + radius, radius, color, 2);
        drawFilledQuadrant(matrix4f, x + width - radius, y + radius, radius, color, 1);
        drawFilledQuadrant(matrix4f, x + radius, y + height - radius, radius, color, 3);
        drawFilledQuadrant(matrix4f, x + width - radius, y + height - radius, radius, color, 4);
    }

    /**
     * Draws a filled rounded rectangle by drawing 1 main rectangle, 4 side rectangles, and specified filled quadrants
     *
     * @param matrix4f Matrix4f object to draw the rounded rectangle
     * @param x        X pos
     * @param y        Y pos
     * @param TL       Whether to draw the top left quadrant
     * @param TR       Whether to draw the top right quadrant
     * @param BL       Whether to draw the bottom left quadrant
     * @param BR       Whether to draw the bottom right quadrant
     * @param width    Width of rounded rectangle
     * @param height   Height of rounded rectangle
     * @param radius   Radius of the quadrants / the rounded rectangle
     * @param color    Color of the rounded rectangle
     */
    public static void drawRoundedRectangle(Matrix4f matrix4f, float x, float y, boolean TL, boolean TR, boolean BL, boolean BR, float width, float height, int radius, int color) {
        // Draw the main rectangle
        drawRectangle(matrix4f, x + radius, y + radius, width - 2 * radius, height - 2 * radius, color);

        // Draw rectangles at the sides
        drawRectangle(matrix4f, x + radius, y, width - 2 * radius, radius, color); // top
        drawRectangle(matrix4f, x + radius, y + height - radius, width - 2 * radius, radius, color); // bottom
        drawRectangle(matrix4f, x, y + radius, radius, height - 2 * radius, color); // left
        drawRectangle(matrix4f, x + width - radius, y + radius, radius, height - 2 * radius, color); // right

        if (TL) {
            drawFilledQuadrant(matrix4f, x + radius, y + radius, radius, color, 2);
        } else {
            drawRectangle(matrix4f, x, y, radius, radius, color);
        }
        if (TR) {
            drawFilledQuadrant(matrix4f, x + width - radius, y + radius, radius, color, 1);
        } else {
            drawRectangle(matrix4f, x + width - radius, y, radius, radius, color);
        }
        if (BL) {
            drawFilledQuadrant(matrix4f, x + radius, y + height - radius, radius, color, 3);
        } else {
            drawRectangle(matrix4f, x, y + height - radius, radius, radius, color);
        }
        if (BR) {
            drawFilledQuadrant(matrix4f, x + width - radius, y + height - radius, radius, color, 4);
        } else {
            drawRectangle(matrix4f, x + width - radius, y + height - radius, radius, radius, color);
        }
    }
    /**
     * Draws a outline rounded gradient rectangle
     *
     * @param matrix4f Matrix4f object to draw the rounded gradient rectangle
     * @param color1 is applied to the bottom-left vertex (x, y + height).
     * @param color2 is applied to the bottom-right vertex (x + width, y + height).
     * @param color3 is applied to the top-right vertex (x + width, y).
     * @param color4 is applied to the top-left vertex (x, y).
     * @param x      X pos
     * @param y      Y pos
     * @param width  Width of rounded gradient rectangle
     * @param height Height of rounded gradient rectangle
     * @param radius Radius of the quadrants / the rounded gradient rectangle
     */
    public static void drawOutlineGradientRoundedBox(Matrix4f matrix4f, float x, float y, float width, float height, float radius, float thickness, Color color1, Color color2, Color color3, Color color4) {
        // Draw the rectangles for the outline with gradient
        drawGradient(matrix4f, x + radius, y, width - radius * 2, thickness, color1.getRGB(), color2.getRGB(), Direction.LEFT_RIGHT); // Top rectangle
        drawGradient(matrix4f, x + radius, y + height - thickness, width - radius * 2, thickness, color3.getRGB(), color4.getRGB(), Direction.RIGHT_LEFT); // Bottom rectangle

        drawGradient(matrix4f, x, y + radius, thickness, height - radius * 2, color4.getRGB(),color1.getRGB(), Direction.BOTTOM_TOP); // Left rectangle
        drawGradient(matrix4f, x + width - thickness, y + radius, thickness, height - radius * 2, color2.getRGB(),color3.getRGB(),Direction.TOP_BOTTOM); // Right rectangle

        // Draw the arcs at the corners for the outline with gradient
        drawArc(matrix4f, x + radius, y + radius, radius, thickness, color1.getRGB(), 180, 270); // Top-left arc
        drawArc(matrix4f, x + width - radius, y + radius, radius, thickness, color2.getRGB(), 90, 180); // Top-right arc
        drawArc(matrix4f, x + width - radius, y + height - radius, radius, thickness, color3.getRGB(), 0, 90); // Bottom-right arc
        drawArc(matrix4f, x + radius, y + height - radius, radius, thickness, color4.getRGB(), 270, 360); // Bottom-left arc
    }
    public static void drawOutlineGradientRoundedBoxEffect(Matrix4f matrix4f, float x, float y, float width, float height, float radius,float ratio, float thickness, Color primaryColor, Color secondaryColor, int currentColorCorner) {
        Color color1, color2, color3, color4;
        switch (currentColorCorner) {
            case 1:
                color1 = ColorUtils.blend(primaryColor, secondaryColor, ratio);
                color2 = color3 = color4 = primaryColor;
                break;
            case 2:
                color2 = ColorUtils.blend(primaryColor, secondaryColor, ratio);
                color1 = color3 = color4 = primaryColor;
                break;
            case 3:
                color3 = ColorUtils.blend(primaryColor, secondaryColor, ratio);
                color1 = color2 = color4 = primaryColor;
                break;
            default:
                color4 = ColorUtils.blend(primaryColor, secondaryColor, ratio);
                color1 = color2 = color3 = primaryColor;
                break;
        }

        drawOutlineGradientRoundedBox(matrix4f,x,y,width,height,radius,thickness,color1,color2,color3,color4);
    }


    /**
     * Draws a rounded rectangle with a shadow in a bad way
     *
     * @param matrix4f      Matrix4f object to draw the rounded rectangle
     * @param x             X pos
     * @param y             Y pos
     * @param width         Width of rounded rectangle
     * @param height        Height of rounded rectangle
     * @param radius        Radius of the quadrants / the rounded rectangle
     * @param color         Color of the rounded rectangle
     * @param shadowOpacity opacity of the shadow
     * @param shadowOffsetX X offset of the shadow
     * @param shadowOffsetY Y offset of the shadow
     */
    public static void drawRoundedRectangleWithShadowBadWay(Matrix4f matrix4f, float x, float y, float width, float height, float radius, int color, int shadowOpacity, float shadowOffsetX, float shadowOffsetY) {
        // First, render the shadow
        drawRoundedRectangle(matrix4f, x + shadowOffsetX, y + shadowOffsetY, width, height, radius, ColorUtils.rgbaToInt(0, 0, 0, shadowOpacity));

        // Then, render the rounded rectangle
        drawRoundedRectangle(matrix4f, x, y, width, height, radius, color);
    }
    /**
     * Draws a rounded rectangle with a shadow
     *
     * @param matrices   MatrixStack object to draw the rounded rectangle
     * @param x          X pos
     * @param y          Y pos
     * @param width      Width of rounded rectangle
     * @param height     Height of rounded rectangle
     * @param radius     Radius of the quadrants / the rounded rectangle
     * @param color      Color of the rounded rectangle
     * @param blurRadius blur radius of the shadow
     */
    public static void drawRoundedRectangleWithShadow(MatrixStack matrices, float x, float y, float width, float height, float radius, int blurRadius, int color) {
      drawRoundedRectangleWithShadow(matrices,x,y,width,height,radius,blurRadius,color,color);
    }

    /**
     * Draws a rounded rectangle with a shadow of color given
     *
     * @param matrices   MatrixStack object to draw the rounded rectangle
     * @param x          X pos
     * @param y          Y pos
     * @param width      Width of rounded rectangle
     * @param height     Height of rounded rectangle
     * @param radius     Radius of the quadrants / the rounded rectangle
     * @param color      Color of the rounded rectangle
     * @param blurRadius blur radius of the shadow
     * @param shadowColor color of the shadow
     */
    public static void drawRoundedRectangleWithShadow(MatrixStack matrices, float x, float y, float width, float height, float radius, int blurRadius, int color, int shadowColor) {
        // First, render the shadow
        drawBlurredShadow(matrices, x, y, width, height, blurRadius, new Color(shadowColor));

        // Then, render the rounded rectangle
        drawRoundedRectangle(matrices.peek().getPositionMatrix(), x, y, width, height, radius, color);
    }

    /**
     * Draws a rounded gradient rectangle
     *
     * @param matrix Matrix4f object to draw the rounded gradient rectangle
     * @param color1 is applied to the bottom-left vertex (x, y + height).
     * @param color2 is applied to the bottom-right vertex (x + width, y + height).
     * @param color3 is applied to the top-right vertex (x + width, y).
     * @param color4 is applied to the top-left vertex (x, y).
     * @param x      X pos
     * @param y      Y pos
     * @param width  Width of rounded gradient rectangle
     * @param height Height of rounded gradient rectangle
     * @param radius Radius of the quadrants / the rounded gradient rectangle
     */
    public static void drawRoundedGradientRectangle(Matrix4f matrix, Color color1, Color color2, Color color3, Color color4, float x, float y, float width, float height, float radius) {
        RenderSystem.enableBlend();
        RenderSystem.colorMask(false, false, false, true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT, false);
        RenderSystem.colorMask(true, true, true, true);

        drawRoundedRectangle(matrix, x, y, width, height, (int) radius, color1.getRGB());

        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix, x, y + height, 0.0F).color(color1.getRGB()).next();
        bufferBuilder.vertex(matrix, x + width, y + height, 0.0F).color(color2.getRGB()).next();
        bufferBuilder.vertex(matrix, x + width, y, 0.0F).color(color3.getRGB()).next();
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(color4.getRGB()).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();

        RenderSystem.defaultBlendFunc();
    }

    /**
     * Draws a rounded gradient rectangle with a shadow
     *
     * @param matrices   MatrixStack object to draw the rounded gradient rectangle
     * @param x          X pos
     * @param y          Y pos
     * @param width      Width of rounded gradient rectangle
     * @param height     Height of rounded gradient rectangle
     * @param radius     Radius of the quadrants / the rounded gradient rectangle
     * @param color1     is applied to the bottom-left vertex (x, y + height).
     * @param color2     is applied to the bottom-right vertex (x + width, y + height).
     * @param color3     is applied to the top-right vertex (x + width, y).
     * @param color4     is applied to the top-left vertex (x, y).
     * @param blurRadius blur radius of the shadow
     */
    public static void drawRoundedGradientRectangleWithShadow(MatrixStack matrices, float x, float y, float width, float height, Color color1, Color color2, Color color3, Color color4, float radius, int blurRadius, Color blurColor) {
        drawBlurredShadow(matrices, x, y, width, height, blurRadius, blurColor);

        drawRoundedGradientRectangle(matrices.peek().getPositionMatrix(), color1, color2, color3, color4, x, y, width, height, radius);
    }

    /* ==== Drawing Lines ==== */
    public static void drawVerticalLine(Matrix4f matrix4f, float x, float y1, float height, float thickness, int color) {
        drawRectangle(matrix4f, x, y1, thickness, height, color);
    }

    public static void drawHorizontalLine(Matrix4f matrix4f, float x1, float width, float y, float thickness, int color) {
        drawRectangle(matrix4f, x1, y, width, thickness, color);
    }

    /* ==== Drawing Custom Stuff ==== */

    // Minecraft InventoryScreen source code but 360 degree support and smoother tickdelta
    public static void drawEntity(DrawContext context, int x, int y, int size, float mouseX, float mouseY, LivingEntity entity) {
        float f = (float) Math.atan(mouseX / 40.0F);
        float g = (float) Math.atan(mouseY / 40.0F);
        Quaternionf quaternionf = (new Quaternionf()).rotateZ(3.1415927F);
        Quaternionf quaternionf2 = (new Quaternionf()).rotateX(g * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);
        float h = entity.bodyYaw;
        float i = entity.getYaw();
        float j = entity.getPitch();
        float k = entity.prevHeadYaw;
        float l = entity.headYaw;
        entity.bodyYaw = 360.0F + f * 20.0F;
        entity.setYaw(360.0F + f * 40.0F);
        entity.headYaw = entity.getYaw();
        entity.prevHeadYaw = entity.getYaw();
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 50.0);
        context.getMatrices().multiplyPositionMatrix((new Matrix4f()).scaling((float) size, (float) size, (float) (-size)));
        context.getMatrices().multiply(quaternionf);
        DiffuseLighting.method_34742();
        EntityRenderDispatcher entityRenderDispatcher = HeliosClient.MC.getEntityRenderDispatcher();
        if (quaternionf2 != null) {
            quaternionf2.conjugate();
            entityRenderDispatcher.setRotation(quaternionf2);
        }

        entityRenderDispatcher.setRenderShadows(false);
        RenderSystem.runAsFancy(() -> {
            entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, HeliosClient.MC.getTickDelta(), context.getMatrices(), context.getVertexConsumers(), 15728880);
        });
        context.draw();
        entityRenderDispatcher.setRenderShadows(true);
        context.getMatrices().pop();
        DiffuseLighting.enableGuiDepthLighting();
        entity.bodyYaw = h;
        entity.setYaw(i);
        entity.setPitch(j);
        entity.prevHeadYaw = k;
        entity.headYaw = l;
    }


    public static void drawEntity(DrawContext context, int x, int y, int size, double z, Entity entity, float delta) {
        float yaw = MathHelper.wrapDegrees(entity.prevYaw + (entity.getYaw() - entity.prevYaw) * HeliosClient.MC.getTickDelta());
        float pitch = entity.getPitch();

        Quaternionf quaternionf = (new Quaternionf()).rotateZ(3.1415927F);
        Quaternionf quaternionf2 = (new Quaternionf()).rotateX(-pitch * 0.017453292F); // Invert the pitch rotation
        quaternionf.mul(quaternionf2);
        float h = entity.getBodyYaw();
        float i = entity.getYaw();
        float j = entity.getPitch();
        float k = entity.prevYaw;
        float l = entity.getHeadYaw();
        entity.setBodyYaw(yaw);
        entity.setPitch(pitch);
        entity.setHeadYaw(entity.getYaw());
        entity.prevYaw = entity.getYaw();

        context.getMatrices().push();
        context.getMatrices().translate(x, y, 50.0);
        context.getMatrices().multiplyPositionMatrix((new Matrix4f()).scaling((float) size, (float) size, (float) (-size)));
        context.getMatrices().multiply(quaternionf);
        DiffuseLighting.method_34742();
        EntityRenderDispatcher entityRenderDispatcher = HeliosClient.MC.getEntityRenderDispatcher();
        if (quaternionf2 != null) {
            quaternionf2.conjugate();
            entityRenderDispatcher.setRotation(quaternionf2);
        }

        entityRenderDispatcher.setRenderShadows(false);
        RenderSystem.runAsFancy(() -> entityRenderDispatcher.render(entity, 0.0, 0.0, z, 0.0F, delta, context.getMatrices(), context.getVertexConsumers(), 15728880));
        context.draw();
        entityRenderDispatcher.setRenderShadows(true);
        context.getMatrices().pop();
        DiffuseLighting.enableGuiDepthLighting();
        entity.setBodyYaw(h);
        entity.setYaw(i);
        entity.setPitch(j);
        entity.prevYaw = k;
        entity.setHeadYaw(l);
    }


    /* ==== Drawing Custom Text ==== */

    public static float getStringWidth(String text) {
        if (isVanillaRenderer()) {
            return HeliosClient.MC.textRenderer.getWidth(text);
        }
        return getFontRenderer() != null ? getFontRenderer().getStringWidth(text) : 0;
    }

    public static float getCustomStringWidth(String text, FontRenderer fontRenderer) {
        if(fontRenderer == null)
            return 0;

        if (isVanillaRenderer()) {
            return HeliosClient.MC.textRenderer.getWidth(text);
        }
        return fontRenderer.getStringWidth(text);
    }

    public static float getStringWidth() {
        return getStringWidth(TEXT);
    }

    public static float getCustomStringWidth(FontRenderer fontRenderer) {
        if(fontRenderer == null)
            return 0;

        return fontRenderer.getStringWidth(TEXT);
    }

    public static float getFxStringWidth(String text) {
        if (isVanillaRenderer()) {
            return HeliosClient.MC.textRenderer.getWidth(text);
        }
        return getFxFontRenderer() != null ? getFxFontRenderer().getStringWidth(text) : 0;
    }

    public static float getFxStringWidth() {
        return getFxStringWidth(TEXT);
    }

    public static float getStringHeight(String text) {
        if (isVanillaRenderer()) {
            return HeliosClient.MC.textRenderer.fontHeight;
        }
        return getFontRenderer() != null ? getFontRenderer().getStringHeight(text) : 0;
    }

    public static float getCustomStringHeight(String text, FontRenderer fontRenderer) {
        if(fontRenderer == null)
            return 0;

        if (isVanillaRenderer()) {
            return HeliosClient.MC.textRenderer.fontHeight;
        }
        return fontRenderer.getStringHeight(text);
    }


    public static float getStringHeight() {
        return getStringHeight(TEXT);
    }

    public static float getCustomStringHeight(FontRenderer fontRenderer) {
        if(fontRenderer == null)
            return 0;

        return fontRenderer.getStringHeight(TEXT);
    }

    public static float getFxStringHeight(String text) {
        if (isVanillaRenderer()) {
            return HeliosClient.MC.textRenderer.fontHeight;
        }
        return getFxFontRenderer() != null ? getFxFontRenderer().getStringHeight(text) : 0;
    }

    public static float getFxStringHeight() {
        return getFxStringHeight(TEXT);
    }

    public static boolean isVanillaRenderer() {
        return renderer == Renderers.VANILLA && drawContext != null && HeliosClient.MC.textRenderer != null;
    }

    public static fxFontRenderer getFxFontRenderer() {
        return FontRenderers.fxfontRenderer;
    }

    public static FontRenderer getFontRenderer() {
        return FontRenderers.fontRenderer;
    }

    public static void drawString(MatrixStack matrixStack, String text, float x, float y, int color) {
        if (isVanillaRenderer()) {
            drawContext.drawText(HeliosClient.MC.textRenderer, text, (int) x, (int) y, color, false);
        } else if (getFontRenderer() != null) {
            getFontRenderer().drawString(matrixStack, text, x, y, 256 - ColorUtils.getRed(color), 256 - ColorUtils.getGreen(color), 256 - ColorUtils.getBlue(color), 256 - ColorUtils.getAlpha(color));
        }
    }

    public static void drawCenteredString(MatrixStack matrixStack, String text, float x, float y, int color) {
        if (isVanillaRenderer()) {
            drawContext.drawText(HeliosClient.MC.textRenderer, text, (int) x, (int) y, color, false);
        } else if (getFontRenderer() != null) {
            getFontRenderer().drawCenteredString(matrixStack, text, x, y, 256 - ColorUtils.getRed(color), 256 - ColorUtils.getGreen(color), 256 - ColorUtils.getBlue(color), 256 - ColorUtils.getAlpha(color));
        }
    }

    public static void drawFixedString(MatrixStack matrixStack, String text, float x, float y, int color) {
        if (isVanillaRenderer()) {
            drawContext.drawText(HeliosClient.MC.textRenderer, text, (int) x, (int) y, color, false);
        } else if (getFxFontRenderer() != null) {
            getFxFontRenderer().drawString(matrixStack, text, x, y, color);
        }
    }

    public static void drawFixedCenteredString(MatrixStack matrixStack, String text, float x, float y, int color) {
        if (isVanillaRenderer()) {
            drawContext.drawText(HeliosClient.MC.textRenderer, text, (int) x, (int) y, color, false);
        } else if (getFxFontRenderer() != null) {
            getFxFontRenderer().drawCenteredString(matrixStack, text, x, y, color);
        }
    }

    public static void drawCustomString(fxFontRenderer fontRenderer, MatrixStack matrixStack, String text, float x, float y, int color) {
        if (isVanillaRenderer()) {
            drawContext.drawText(HeliosClient.MC.textRenderer, text, (int) x, (int) y, color, false);
        } else if (fontRenderer != null) {
            fontRenderer.drawString(matrixStack, text, x, y, color);
        }
    }

    public static void drawCustomCenteredString(fxFontRenderer fontRenderer, MatrixStack matrixStack, String text, float x, float y, int color) {
        if (isVanillaRenderer()) {
            drawContext.drawText(HeliosClient.MC.textRenderer, text, (int) x, (int) y, color, false);
        } else if (fontRenderer != null) {
            fontRenderer.drawCenteredString(matrixStack, text, x, y, color);
        }
    }

    public static void setRenderer(Renderers renderer) {
        Renderer2D.renderer = renderer;
    }

    public static void setDrawContext(DrawContext drawContext) {
        Renderer2D.drawContext = drawContext;
    }

    public static List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            if (Renderer2D.getStringWidth(line + word) > maxWidth) {
                lines.add(line.toString());
                line.setLength(0);
            }
            line.append(word).append(" ");
        }

        if (!line.isEmpty()) {
            lines.add(line.toString());
        }

        return lines;
    }

    @SubscribeEvent
    public void renderEvent(RenderEvent renderEvent) {
        drawContext = renderEvent.getDrawContext();
    }

    public enum Renderers {
        CUSTOM,
        VANILLA
    }

    // https://github.com/Pan4ur/ThunderHack-Recode/blob/main/src/main/java/thunder/hack/utility/render/Render2DEngine.java
    public static class BlurredShadow {
        Texture id;

        public BlurredShadow(BufferedImage bufferedImage) {
            this.id = new Texture("identifier/blur/" + RandomStringUtils.randomAlphanumeric(16));
            registerBufferedImageTexture(id, bufferedImage);
        }

        public void bind() {
            RenderSystem.setShaderTexture(0, id);
        }
    }

}