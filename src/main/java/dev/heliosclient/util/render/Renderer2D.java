package dev.heliosclient.util.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.render.RenderEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.entity.DisplayPreviewEntity;
import dev.heliosclient.util.fontutils.BetterFontRenderer;
import dev.heliosclient.util.fontutils.FontRenderers;
import me.x150.renderer.font.FontRenderer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.equipment.EquipmentModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.apache.commons.lang3.RandomStringUtils;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL40C;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

import static dev.heliosclient.util.render.Renderer3D.mc;
import static me.x150.renderer.render.Renderer2d.renderTexture;
import static me.x150.renderer.util.RendererUtils.registerBufferedImageTexture;

public class Renderer2D implements Listener {

    public static final String TEXT = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-=_+|{};<>?~`,./;'[] ";
    //0 to 2pi values of sin and cos
    private static final float[] sina = new float[]{
            0, 0.1736482f, 0.3420201f, 0.5f, 0.6427876f, 0.7660444f, 0.8660254f, 0.9396926f, 0.9848077f, 1, 0.9848078f, 0.9396927f, 0.8660255f, 0.7660446f, 0.6427878f, 0.5000002f, 0.3420205f, 0.1736485f, 3.894144E-07f, -0.1736478f, -0.3420197f, -0.4999996f, -0.6427872f, -0.7660443f, -0.8660252f, -0.9396925f, -0.9848077f, -1, -0.9848078f, -0.9396928f, -0.8660257f, -0.7660449f, -0.6427881f, -0.5000006f, -0.3420208f, -0.1736489f, 0, 0.1736482f, 0.3420201f, 0.5f, 0.6427876f, 0.7660444f, 0.8660254f, 0.9396926f, 0.9848077f
    };

    private static final float[] cosa = new float[36];
    public static Renderer2D INSTANCE = new Renderer2D();
    public static DrawContext drawContext;
    public static Renderers renderer = Renderers.CUSTOM;
    public static final Map<Integer, BlurredShadow> shadowCache = new HashMap<>();
    public static final Map<Integer, BlurredShadow> outlineShadowCache = new HashMap<>();
    public static ProjectionType projectionType;

    static {
        for (int i = 0; i < 36; i++) {
            float angle = (float) (i * Math.PI / 18);
            cosa[i] = (float) Math.cos(angle);
        }
    }

    public static void init(){
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


        BufferBuilder bufferBuilder = setupAndBegin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        switch (direction) {
            case LEFT_RIGHT:
                bufferBuilder.vertex(matrix4f, x, y + height, 0.0F).color(startRed, startGreen, startBlue, startAlpha);
                bufferBuilder.vertex(matrix4f, x + width, y + height, 0.0F).color(endRed, endGreen, endBlue, endAlpha);
                bufferBuilder.vertex(matrix4f, x + width, y, 0.0F).color(endRed, endGreen, endBlue, endAlpha);
                bufferBuilder.vertex(matrix4f, x, y, 0.0F).color(startRed, startGreen, startBlue, startAlpha);
                break;
            case TOP_BOTTOM:
                bufferBuilder.vertex(matrix4f, x, y + height, 0.0F).color(endRed, endGreen, endBlue, endAlpha);
                bufferBuilder.vertex(matrix4f, x + width, y + height, 0.0F).color(endRed, endGreen, endBlue, endAlpha);
                bufferBuilder.vertex(matrix4f, x + width, y, 0.0F).color(startRed, startGreen, startBlue, startAlpha);
                bufferBuilder.vertex(matrix4f, x, y, 0.0F).color(startRed, startGreen, startBlue, startAlpha);
                break;
            case RIGHT_LEFT:
                bufferBuilder.vertex(matrix4f, x, y + height, 0.0F).color(endRed, endGreen, endBlue, endAlpha);
                bufferBuilder.vertex(matrix4f, x + width, y + height, 0.0F).color(startRed, startGreen, startBlue, startAlpha);
                bufferBuilder.vertex(matrix4f, x + width, y, 0.0F).color(startRed, startGreen, startBlue, startAlpha);
                bufferBuilder.vertex(matrix4f, x, y, 0.0F).color(endRed, endGreen, endBlue, endAlpha);
                break;
            case BOTTOM_TOP:
                bufferBuilder.vertex(matrix4f, x, y + height, 0.0F).color(startRed, startGreen, startBlue, startAlpha);
                bufferBuilder.vertex(matrix4f, x + width, y + height, 0.0F).color(startRed, startGreen, startBlue, startAlpha);
                bufferBuilder.vertex(matrix4f, x + width, y, 0.0F).color(endRed, endGreen, endBlue, endAlpha);
                bufferBuilder.vertex(matrix4f, x, y, 0.0F).color(endRed, endGreen, endBlue, endAlpha);
                break;
        }

        draw(bufferBuilder);

        RenderSystem.disableBlend();
    }

    public static void enableScissor(int x, int y, int width, int height) {
        enableScissor(x,y,width,height,mc.getWindow().getScaleFactor());
    }

    public static void enableScissor(int x, int y, int width, int height, double scaleFactor) {
        int scissorX = (int) (x * scaleFactor);
        int scissorY = (int) (HeliosClient.MC.getWindow().getHeight() - ((y + height) * scaleFactor));
        int scissorWidth = (int) (width * scaleFactor);
        int scissorHeight = (int) (height * scaleFactor);

        RenderSystem.enableScissor(scissorX, scissorY, scissorWidth, scissorHeight);
    }

    public static void disableScissor() {
        RenderSystem.disableScissor();
    }

    /**
     * This method assumes that the x, y coords are the origin of the element, i.e. it is scaled from this position.
     */
    public static void scaleAndPosition(MatrixStack matrices, float x, float y, float scale) {
        matrices.push(); // Save the current transformation state

        // Translate the origin back to the desired position
        matrices.translate(x, y, 0);

        // Scale the matrix
        matrices.scale(scale, scale, 1.0F);

        matrices.translate(-x, -y, 0);
    }

    /**
     * This method scales the matrices by the centre
     */
    public static void scaleAndPosition(MatrixStack matrices, float x, float y, float width, float height, float scale) {
        matrices.push(); // Save the current transformation state

        // Translate the origin back to the desired position
        matrices.translate(x + (width / 2.0f), y + (height / 2.0f), 0);

        // Scale the matrix
        matrices.scale(scale, scale, 1.0F);

        matrices.translate(-(x + width / 2.0f), -(y + height / 2.0f), 0);
    }

    /* ==== Drawing Rectangles ==== */

    public static void stopScaling(MatrixStack matrices) {
        matrices.pop(); // Restore the previous transformation state
    }

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
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder bufferBuilder = setupAndBegin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);


        bufferBuilder.vertex(matrix4f, x, y + height, 0.0F).color(color);
        bufferBuilder.vertex(matrix4f, x + width, y + height, 0.0F).color(color);
        bufferBuilder.vertex(matrix4f, x + width, y, 0.0F).color(color);
        bufferBuilder.vertex(matrix4f, x, y, 0.0F).color(color);

        draw(bufferBuilder);

        RenderSystem.disableBlend();
    }

    /**
     * Draws a quad with the given positions as vertices on a 2D plane
     */
    public static void drawQuad(Matrix4f matrix, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, int color) {
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder bufferBuilder = setupAndBegin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix, x1, y1, 0).color(color);
        bufferBuilder.vertex(matrix, x2, y2, 0).color(color);
        bufferBuilder.vertex(matrix, x3, y3, 0).color(color);
        bufferBuilder.vertex(matrix, x4, y4, 0).color(color);

        draw(bufferBuilder);
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
       drawOutlineBox(matrix4f,x,y,width,height,thickness,true,true,true,true,color);
    }
    /**
     * Draws a singular outline rectangle on screen with the given parameters.
    */
    public static void drawOutlineBox(Matrix4f matrix4f, float x, float y, float width, float height, float thickness, boolean TOP, boolean BOTTOM, boolean LEFT, boolean RIGHT, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder bufferBuilder = setupAndBegin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        if (TOP) drawRectangleBufferInternal(matrix4f, bufferBuilder, x, y - thickness, width, thickness, red, green, blue, alpha);
        if (BOTTOM) drawRectangleBufferInternal(matrix4f, bufferBuilder, x, y + height, width, thickness, red, green, blue, alpha);
        if (LEFT) drawRectangleBufferInternal(matrix4f, bufferBuilder, x - thickness, y, thickness, height, red, green, blue, alpha);
        if (RIGHT) drawRectangleBufferInternal(matrix4f, bufferBuilder, x + width, y, thickness, height, red, green, blue, alpha);

        draw(bufferBuilder);

        RenderSystem.disableBlend();
    }

    /**
     * Draws a singular outline rectangle with a blurred shadow
     */
    public static void drawOutlineBoxWithShadow(MatrixStack stack, float x, float y, float width, float height, float thickness, boolean TOP, boolean BOTTOM, boolean LEFT, boolean RIGHT, int color, int shadowRadius, Color shadowColor) {
        drawOutlineBlurredShadow(stack,x,y,width,height,shadowRadius, shadowColor);

        drawOutlineBox(stack.peek().getPositionMatrix(), x,y,width,height,thickness,TOP,BOTTOM,LEFT,RIGHT,color);
    }


    /**
     * Draws an outline rounded gradient rectangle
     *
     * @param matrix4f Matrix4f object to draw the gradient rectangle
     * @param color1   is applied to the bottom-left vertex (x, y + height).
     * @param color2   is applied to the bottom-right vertex (x + width, y + height).
     * @param color3   is applied to the top-right vertex (x + width, y).
     * @param color4   is applied to the top-left vertex (x, y).
     * @param x        X pos
     * @param y        Y pos
     * @param width    Width of gradient rectangle
     * @param height   Height of gradient rectangle
     */
    public static void drawOutlineGradientBox(Matrix4f matrix4f, float x, float y, float width, float height, float thickness, Color color1, Color color2, Color color3, Color color4) {
        // Draw the rectangles for the outline with gradient
        drawGradient(matrix4f, x + thickness, y, width - thickness * 2, thickness, color1.getRGB(), color2.getRGB(), Direction.LEFT_RIGHT); // Top rectangle
        drawGradient(matrix4f, x + thickness, y + height - thickness, width - thickness * 2, thickness, color3.getRGB(), color4.getRGB(), Direction.RIGHT_LEFT); // Bottom rectangle

        drawGradient(matrix4f, x, y, thickness, height, color4.getRGB(), color1.getRGB(), Direction.BOTTOM_TOP); // Left rectangle
        drawGradient(matrix4f, x + width - thickness, y, thickness, height, color2.getRGB(), color3.getRGB(), Direction.TOP_BOTTOM); // Right rectangle
    }
    /**
     * Draws an outline rounded gradient rectangle with a shadow
     *
     * @param matrices MatrixStack object to draw the gradient rectangle
     * @param color1   is applied to the bottom-left vertex (x, y + height).
     * @param color2   is applied to the bottom-right vertex (x + width, y + height).
     * @param color3   is applied to the top-right vertex (x + width, y).
     * @param color4   is applied to the top-left vertex (x, y).
     * @param x        X pos
     * @param y        Y pos
     * @param width    Width of gradient rectangle
     * @param height   Height of gradient rectangle
     */
    public static void drawOutlineGradientBoxWithShadow(MatrixStack matrices, float x, float y, float width, float height, float thickness, Color color1, Color color2, Color color3, Color color4, int shadowRadius, Color shadowColor) {
        //First draw the shadow
        drawOutlineBlurredShadow(matrices,x,y,width,height,shadowRadius,shadowColor);

        drawOutlineGradientBox(matrices.peek().getPositionMatrix(), x,y,width,height,thickness,color1,color2,color3,color4);
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
    public static void drawRectangleWithShadow(MatrixStack matrices, float x, float y, float width, float height, int color, Color blurColor, int blurRadius) {
        //Shadow
        drawBlurredShadow(matrices, x, y, width, height, blurRadius, blurColor);

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
    public static void drawGradientWithShadow(MatrixStack matrices, float x, float y, float width, float height, int blurRadius, int startColor, int endColor, Color shadowColor, Direction direction) {
        drawBlurredShadow(matrices, x, y, width, height, blurRadius, shadowColor);

        drawGradient(matrices.peek().getPositionMatrix(), x, y, width, height, startColor, endColor, direction);
    }

    public static void drawRainbowGradientRectangle(Matrix4f matrix4f, float x, float y, float width, float height) {
        BufferBuilder bufferBuilder = setupAndBegin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);


        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        
        for (int i = 0; i < width; i++) {
            float hue = (i / width); // Multiply by 1 to go through the whole color spectrum once (red to red)
            Color color = Color.getHSBColor(hue, 1.0f, 1.0f); // Full saturation and brightness

            bufferBuilder.vertex(matrix4f, x + i, y, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            bufferBuilder.vertex(matrix4f, x + i + 1, y, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            bufferBuilder.vertex(matrix4f, x + i + 1, y + height, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            bufferBuilder.vertex(matrix4f, x + i, y + height, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        }
        
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        RenderSystem.disableBlend();
    }

    public static void drawRainbowGradient(Matrix4f matrix, float x, float y, float width, float height) {
        RenderSystem.enableBlend();
        RenderSystem.colorMask(false, false, false, true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT);
        RenderSystem.colorMask(true, true, true, true);

        drawRectangle(matrix, x, y, width, height, Color.BLACK.getRGB());

        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_DST_ALPHA);

        RenderSystem.enableBlend();

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        BufferBuilder bufferBuilder = setupAndBegin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (float i = 0; i < width; i += 1.0f) {
            float hue = (i / width); // Multiply by 1 to go through the whole color spectrum once (red to red)
            Color color = Color.getHSBColor(hue, 1.0f, 1.0f); // Full saturation and brightness

            bufferBuilder.vertex(matrix, x + i, y, 0.0F).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            bufferBuilder.vertex(matrix, x + i + 1.0f, y, 0.0F).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            bufferBuilder.vertex(matrix, x + i + 1.0f, y + height, 0.0F).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            bufferBuilder.vertex(matrix, x + i, y + height, 0.0F).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        }

        draw(bufferBuilder);

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
        if(HeliosClient.CLICKGUI.disableGlobalShadows.value) return;

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
     * Draws a singular outline blurred shadow using the GaussianBlur algorithm on screen with the given parameters
     *
     * @param matrices   MatrixStack object to draw the blurred shadow
     * @param x          X position of the blurred shadow
     * @param y          Y position of the blurred shadow
     * @param width      Width of the blurred shadow
     * @param height     Height of the blurred shadow
     * @param blurRadius blur radius of the shadow for gaussian blur algorithm
     * @param color      color of the blurred shadow
     */
    public static void drawOutlineBlurredShadow(MatrixStack matrices, float x, float y, float width, float height, int blurRadius, Color color, boolean rounded, float radius) {
        if(HeliosClient.CLICKGUI.disableGlobalShadows.value) return;
        width = width + blurRadius * 2;
        height = height + blurRadius * 2;
        x = x - blurRadius;
        y = y - blurRadius;

        int identifier = (int) (width * height + width * blurRadius + radius * radius);
        if (outlineShadowCache.containsKey(identifier)) {
            outlineShadowCache.get(identifier).bind();
            RenderSystem.defaultBlendFunc();
        } else {
            // Create the original image with a transparent center
            int radiusR = Math.round(radius);
            BufferedImage original = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = original.createGraphics();
            g.setComposite(AlphaComposite.Src);
            g.setColor(new Color(-1));
            if (rounded) {
                g.fillRoundRect(blurRadius, blurRadius, (int) (width - blurRadius * 2), (int) (height - blurRadius * 2), radiusR, radiusR);
            } else {
                g.fillRect(blurRadius, blurRadius, (int) (width - blurRadius * 2), (int) (height - blurRadius * 2));
            }
            g.dispose();

            // Apply the Gaussian blur
            GaussianBlur op = new GaussianBlur(blurRadius);
            BufferedImage blurred = op.applyFilter(original, null);

            // Clear the center of the blurred image to make it transparent
            g = blurred.createGraphics();
            g.setComposite(AlphaComposite.Clear);
            if (rounded) {
                g.fillRoundRect(blurRadius, blurRadius, (int) (width - blurRadius * 2), (int) (height - blurRadius * 2),radiusR,radiusR);
            } else {
                g.fillRect(blurRadius, blurRadius, (int) (width - blurRadius * 2), (int) (height - blurRadius * 2));
            }
            g.dispose();

            outlineShadowCache.put(identifier, new BlurredShadow(blurred));
            return;
        }

        RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        RenderSystem.enableBlend();
        renderTexture(matrices, x, y, width, height, 0, 0, width, height, width, height);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    /**
     * This is the same as the previous one but with fewer parameters
     */
    public static void drawOutlineBlurredShadow(MatrixStack matrices, float x, float y, float width, float height, int blurRadius, Color color) {
        drawOutlineBlurredShadow(matrices,x,y,width,height,blurRadius,color,false,0);
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
        if(HeliosClient.CLICKGUI.disableGlobalShadows.value) return;

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
            g.setColor(Color.WHITE);
            g.fillOval(blurRadius, blurRadius, diameter, diameter); // Draw a circle instead of a rectangle
            g.dispose();
            GaussianBlur op = new GaussianBlur(blurRadius);
            BufferedImage blurred = op.applyFilter(original, null);
            shadowCache.put(identifier, new BlurredShadow(blurred));
            return;
        }

        RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        RenderSystem.enableBlend();
        renderTexture(matrices, xCenter - blurRadius - radius, yCenter - blurRadius - radius, shadowWidth, shadowHeight, 0, 0, shadowWidth, shadowHeight, shadowWidth, shadowHeight);
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

        BufferBuilder bufferBuilder = setupAndBegin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        for (int i = 0; i <= 360; i++) {
            double x = xCenter + Math.sin(Math.toRadians(i)) * radius;
            double y = yCenter + Math.cos(Math.toRadians(i)) * radius;
            double x2 = xCenter + Math.sin(Math.toRadians(i)) * (radius + lineWidth);
            double y2 = yCenter + Math.cos(Math.toRadians(i)) * (radius + lineWidth);
            bufferBuilder.vertex(matrix4f, (float) x, (float) y, 0).color(red, green, blue, alpha);
            bufferBuilder.vertex(matrix4f, (float) x2, (float) y2, 0).color(red, green, blue, alpha);
        }


        draw(bufferBuilder);
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

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder bufferBuilder = setupAndBegin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);


        bufferBuilder.vertex(matrix4f, xCenter, yCenter, 0).color(red, green, blue, alpha);

        for (int i = 0; i <= 360; i++) {
            double x = xCenter + Math.sin(Math.toRadians(i)) * radius;
            double y = yCenter + Math.cos(Math.toRadians(i)) * radius;
            bufferBuilder.vertex(matrix4f, (float) x, (float) y, 0).color(red, green, blue, alpha);
        }

        draw(bufferBuilder);
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


        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();

        BufferBuilder bufferBuilder = setupAndBegin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        for (float angle = startAngle; angle <= endAngle; angle += 1.0F) {
            float x1 = x + MathHelper.cos(angle * 0.017453292F) * radius;
            float y1 = y + MathHelper.sin(angle * 0.017453292F) * radius;
            float x2 = x + MathHelper.cos((angle + 1.0F) * 0.017453292F) * radius;
            float y2 = y + MathHelper.sin((angle + 1.0F) * 0.017453292F) * radius;

            bufferBuilder.vertex(matrix4f, x, y, 0).color(red, green, blue, alpha);
            bufferBuilder.vertex(matrix4f, x1, y1, 0).color(red, green, blue, alpha);
            bufferBuilder.vertex(matrix4f, x2, y2, 0).color(red, green, blue, alpha);
        }
        draw(bufferBuilder);
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


        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();

        BufferBuilder bufferBuilder = setupAndBegin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix4f, xCenter, yCenter, 0).color(startRed, startGreen, startBlue, startAlpha);

        for (int i = quadrant * 90; i <= quadrant * 90 + 90; i++) {
            double x = xCenter + Math.sin(Math.toRadians(i)) * radius;
            double y = yCenter + Math.cos(Math.toRadians(i)) * radius;

            // Interpolate the color based on the angle
            float t = (float) (i - quadrant * 90) / 90.0f;
            float red = startRed * (1 - t) + endRed * t;
            float green = startGreen * (1 - t) + endGreen * t;
            float blue = startBlue * (1 - t) + endBlue * t;
            float alpha = startAlpha * (1 - t) + endAlpha * t;

            bufferBuilder.vertex(matrix4f, (float) x, (float) y, 0).color(red, green, blue, alpha);
        }

        draw(bufferBuilder);
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

        BufferBuilder bufferBuilder = setupAndBegin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();

        for (int i = startAngle; i <= endAngle; i++) {
            double innerX = xCenter + Math.sin(Math.toRadians(i)) * (radius - thickness);
            double innerY = yCenter + Math.cos(Math.toRadians(i)) * (radius - thickness);
            double outerX = xCenter + Math.sin(Math.toRadians(i)) * radius;
            double outerY = yCenter + Math.cos(Math.toRadians(i)) * radius;

            bufferBuilder.vertex(matrix4f, (float) innerX, (float) innerY, 0).color(red, green, blue, alpha);
            bufferBuilder.vertex(matrix4f, (float) outerX, (float) outerY, 0).color(red, green, blue, alpha);
        }

        draw(bufferBuilder);

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


        BufferBuilder bufferBuilder = setupAndBegin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        RenderSystem.enableBlend();

        bufferBuilder.vertex(matrix4f, xCenter, yCenter, 0).color(red, green, blue, alpha);

        for (int i = quadrant * 90; i <= quadrant * 90 + 90; i++) {
            double x = xCenter + Math.sin(Math.toRadians(i)) * radius;
            double y = yCenter + Math.cos(Math.toRadians(i)) * radius;
            bufferBuilder.vertex(matrix4f, (float) x, (float) y, 0).color(red, green, blue, alpha);
        }

        draw(bufferBuilder);
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


        BufferBuilder bufferBuilder = setupAndBegin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix4f, x1, y1, 0).color(red, green, blue, alpha);
        bufferBuilder.vertex(matrix4f, x2, y2, 0).color(red, green, blue, alpha);
        bufferBuilder.vertex(matrix4f, x3, y3, 0).color(red, green, blue, alpha);
        bufferBuilder.vertex(matrix4f, x1, y1, 0).color(red, green, blue, alpha);

        draw(bufferBuilder);
    }
    public static void drawFilledTriangle(Matrix4f matrix4f, int x1, int y1, int x2, int y2, int x3, int y3, int color) {
        BufferBuilder bufferBuilder = setupAndBegin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix4f, x1, y1, 0).color(color);
        bufferBuilder.vertex(matrix4f, x2, y2, 0).color(color);
        bufferBuilder.vertex(matrix4f, x3, y3, 0).color(color);

        draw(bufferBuilder);
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
    public static void drawRoundedRectangle(Matrix4f matrix4f, float x, float y, float width, float height, float radius, int color) {
        drawRoundedRectangle(matrix4f, x, y, true, true, true, true, width, height, radius, color);
    }


    public static void drawRoundedRectangle(Matrix4f matrix4f, float x, float y, boolean TL, boolean TR, boolean BL, boolean BR, float width, float height, float radius, int color) {
        radius = Math.min(radius, Math.min(width, height) / 2);

        float centerX = x + (width / 2.0f);
        float centerY = y + (height / 2.0f);

        drawRoundedRectangleInternal(matrix4f, centerX, centerY, width, height, radius, color, TL, TR, BL, BR);
    }
    public static void drawRoundedRectangleInternal(Matrix4f ma, float cx, float cy, float dx, float dy, float r, int rgba, boolean TL, boolean TR, boolean BL, boolean BR) {
        float halfWidth = dx * 0.5f;
        float halfHeight = dy * 0.5f;

        int i;
        dx -= r + r;
        dy -= r + r;

        float x0 = cx + (0.5f * dx);
        float y0 = cy + (0.5f * dy);

        float x = 0, y;


        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(1f,1f,1f,1f);
        RenderSystem.disableCull();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder buf = setupAndBegin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        buf.vertex(ma, cx, cy, 0).color(rgba);

        if (BR) {
            for (i = 0; i < 9; i++) {
                x = x0 + (r * cosa[i]);
                y = y0 + (r * sina[i]);
                buf.vertex(ma, x, y, 0).color(rgba);
            }
        } else {
            buf.vertex(ma, cx + halfWidth, cy + halfHeight, 0).color(rgba);
        }


        x0 -= dx;
        if (BL) {
            for (i = 9; i < 18; i++) {
                x = x0 + (r * cosa[i]);
                y = y0 + (r * sina[i]);
                buf.vertex(ma, x, y, 0).color(rgba);
            }
        } else {
            buf.vertex(ma, cx - halfWidth, cy + halfHeight, 0).color(rgba);
        }

        y0 -= dy;
        if (TL) {
            for (i = 18; i < 27; i++) {
                x = x0 + (r * cosa[i]);
                y = y0 + (r * sina[i]);
                buf.vertex(ma, x, y, 0).color(rgba);
            }

        } else {
            buf.vertex(ma, cx - halfWidth, cy - halfHeight, 0).color(rgba);
        }

        x0 += dx;
        if (TR) {
            for (i = 27; i < 36; i++) {
                x = x0 + (r * cosa[i]);
                y = y0 + (r * sina[i]);
                buf.vertex(ma, x, y, 0).color(rgba);
            }
        } else {
            x = cx + halfWidth;
            buf.vertex(ma, x, cy - halfHeight, 0).color(rgba);
        }

        if (!BR) {
            buf.vertex(ma, x, cy + halfHeight, 0).color(rgba);
        } else {
            buf.vertex(ma, x, cy + (0.5f * dy), 0).color(rgba);
        }

        draw(buf);

        RenderSystem.disableBlend();

        RenderSystem.depthMask(true);
    }


    /**
     * Draws a filled rounded rectangle by drawing 1 main rectangle, 4 side rectangles, and specified filled quadrants
     * This is the outdated version of drawing a rounded rectangle and is replaced with {@link #drawRoundedRectangleInternal(Matrix4f, float, float, float, float, float, int, boolean, boolean, boolean, boolean)}
     * <p>
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
    @Deprecated
    public static void drawRoundedRectangleDeprecated(Matrix4f matrix4f, float x, float y, boolean TL, boolean TR, boolean BL, boolean BR, float width, float height, float radius, int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        //Draw a single rectangle for radius zero to protect FPS
        if (radius == 0) {
            drawRectangle(matrix4f, x, y, width, height, color);
            return;
        }


        BufferBuilder bufferBuilder = setupAndBegin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        // Draw the main rectangle
        drawRectangleBufferInternal(matrix4f, bufferBuilder, x + radius, y + radius, width - 2 * radius, height - 2 * radius, red, green, blue, alpha);

        // Draw rectangles at the sides
        drawRectangleBufferInternal(matrix4f, bufferBuilder, x + radius, y, width - 2 * radius, radius, red, green, blue, alpha); // top
        drawRectangleBufferInternal(matrix4f, bufferBuilder, x + radius, y + height - radius, width - 2 * radius, radius, red, green, blue, alpha); // bottom
        drawRectangleBufferInternal(matrix4f, bufferBuilder, x, y + radius, radius, height - 2 * radius, red, green, blue, alpha); // left
        drawRectangleBufferInternal(matrix4f, bufferBuilder, x + width - radius, y + radius, radius, height - 2 * radius, red, green, blue, alpha); //right


        if (!TL || radius < 0) {
            drawRectangleBufferInternal(matrix4f, bufferBuilder, x, y, radius, radius, red, green, blue, alpha);
        }
        if (!TR || radius < 0) {
            drawRectangleBufferInternal(matrix4f, bufferBuilder, x + width - radius, y, radius, radius, red, green, blue, alpha);
        }
        if (!BL || radius < 0) {
            drawRectangleBufferInternal(matrix4f, bufferBuilder, x, y + height - radius, radius, radius, red, green, blue, alpha);
        }
        if (!BR || radius < 0) {
            drawRectangleBufferInternal(matrix4f, bufferBuilder, x + width - radius, y + height - radius, radius, radius, red, green, blue, alpha);
        }

        draw(bufferBuilder);


        if (TL && radius > 0) {
            drawFilledQuadrant(matrix4f, x + radius, y + radius, radius, color, 2);
        }

        if (TR && radius > 0) {
            drawFilledQuadrant(matrix4f, x + width - radius, y + radius, radius, color, 1);
        }

        if (BL && radius > 0) {
            drawFilledQuadrant(matrix4f, x + radius, y + height - radius, radius, color, 3);
        }

        if (BR && radius > 0) {
            drawFilledQuadrant(matrix4f, x + width - radius, y + height - radius, radius, color, 4);
        }
        RenderSystem.disableBlend();
    }

    public static void drawRectangleBufferInternal(Matrix4f matrix4f, BufferBuilder bufferBuilder, float x, float y, float width, float height, float red, float green, float blue, float alpha) {
        bufferBuilder.vertex(matrix4f, x, y + height, 0.0F).color(red, green, blue, alpha);
        bufferBuilder.vertex(matrix4f, x + width, y + height, 0.0F).color(red, green, blue, alpha);
        bufferBuilder.vertex(matrix4f, x + width, y, 0.0F).color(red, green, blue, alpha);
        bufferBuilder.vertex(matrix4f, x, y, 0.0F).color(red, green, blue, alpha);
    }

    /**
     * Draws an outline rounded gradient rectangle
     *
     * @param matrix4f Matrix4f object to draw the rounded gradient rectangle
     * @param color1   is applied to the bottom-left vertex (x, y + height).
     * @param color2   is applied to the bottom-right vertex (x + width, y + height).
     * @param color3   is applied to the top-right vertex (x + width, y).
     * @param color4   is applied to the top-left vertex (x, y).
     * @param x        X pos
     * @param y        Y pos
     * @param width    Width of rounded gradient rectangle
     * @param height   Height of rounded gradient rectangle
     * @param radius   Radius of the quadrants / the rounded gradient rectangle
     */
    public static void drawOutlineGradientRoundedBox(Matrix4f matrix4f, float x, float y, float width, float height, float radius, float thickness, Color color1, Color color2, Color color3, Color color4) {
        // Draw the rectangles for the outline with gradient
        drawGradient(matrix4f, x + radius, y, width - radius * 2, thickness, color1.getRGB(), color2.getRGB(), Direction.LEFT_RIGHT); // Top rectangle
        drawGradient(matrix4f, x + radius, y + height - thickness, width - radius * 2, thickness, color3.getRGB(), color4.getRGB(), Direction.RIGHT_LEFT); // Bottom rectangle

        drawGradient(matrix4f, x, y + radius, thickness, height - radius * 2, color4.getRGB(), color1.getRGB(), Direction.BOTTOM_TOP); // Left rectangle
        drawGradient(matrix4f, x + width - thickness, y + radius, thickness, height - radius * 2, color2.getRGB(), color3.getRGB(), Direction.TOP_BOTTOM); // Right rectangle

        // Draw the arcs at the corners for the outline with gradient
        drawArc(matrix4f, x + radius, y + radius, radius, thickness, color1.getRGB(), 180, 270); // Top-left arc
        drawArc(matrix4f, x + width - radius, y + radius, radius, thickness, color2.getRGB(), 90, 180); // Top-right arc
        drawArc(matrix4f, x + width - radius, y + height - radius, radius, thickness, color3.getRGB(), 0, 90); // Bottom-right arc
        drawArc(matrix4f, x + radius, y + height - radius, radius, thickness, color4.getRGB(), 270, 360); // Bottom-left arc
    }

    /**
     * Draws an outline rounded gradient rectangle
     *
     * @param matrices MatrixStack object to draw the rounded gradient rectangle
     * @param color1   is applied to the bottom-left vertex (x, y + height).
     * @param color2   is applied to the bottom-right vertex (x + width, y + height).
     * @param color3   is applied to the top-right vertex (x + width, y).
     * @param color4   is applied to the top-left vertex (x, y).
     * @param x        X pos
     * @param y        Y pos
     * @param width    Width of rounded gradient rectangle
     * @param height   Height of rounded gradient rectangle
     * @param radius   Radius of the quadrants / the rounded gradient rectangle
     */
    public static void drawOutlineGradientRoundedBoxWithShadow(MatrixStack matrices, float x, float y, float width, float height, float radius, float thickness, Color color1, Color color2, Color color3, Color color4, int shadowRadius, Color shadowColor) {
        //Draw the shadow first
        drawOutlineBlurredShadow(matrices,x,y,width,height,shadowRadius,shadowColor,true,radius);

        drawOutlineGradientRoundedBox(matrices.peek().getPositionMatrix(), x,y,width,height,radius,thickness,color1,color2,color3,color4);
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
        drawRoundedRectangleWithShadow(matrices, x, y, width, height, radius, blurRadius, color, color, true, true, true, true);
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
    public static void drawRoundedRectangleWithShadow(MatrixStack matrices, float x, float y, float width, float height, float radius, int blurRadius, int color, boolean TL, boolean TR, boolean BL, boolean BR) {
        drawRoundedRectangleWithShadow(matrices, x, y, width, height, radius, blurRadius, color, color, TL, TR, BL, BR);
    }

    /**
     * Draws a rounded rectangle with a shadow of color given
     *
     * @param matrices    MatrixStack object to draw the rounded rectangle
     * @param x           X pos
     * @param y           Y pos
     * @param width       Width of rounded rectangle
     * @param height      Height of rounded rectangle
     * @param radius      Radius of the quadrants / the rounded rectangle
     * @param color       Color of the rounded rectangle
     * @param blurRadius  blur radius of the shadow
     * @param shadowColor color of the shadow
     */
    public static void drawRoundedRectangleWithShadow(MatrixStack matrices, float x, float y, float width, float height, float radius, int blurRadius, int color, int shadowColor, boolean TL, boolean TR, boolean BL, boolean BR) {
        // First, render the shadow
        drawBlurredShadow(matrices, x, y, width, height, blurRadius, new Color(shadowColor));

        // Then, render the rounded rectangle
        drawRoundedRectangle(matrices.peek().getPositionMatrix(), x, y, TL, TR, BL, BR, width, height, radius, color);
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
        drawRoundedGradientRectangle(matrix, color1, color2, color3, color4, x, y, width, height, radius, true, true, true, true);
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
    public static void drawRoundedGradientRectangle(Matrix4f matrix, Color color1, Color color2, Color color3, Color color4, float x, float y, float width, float height, float radius, boolean TL, boolean TR, boolean BL, boolean BR) {
        //Draw a single rounded rectangle for same colors
        if(color1 == color2 && color2 == color3 && color3 == color4){
            drawRoundedRectangle(matrix, x, y, TL, TR, BL, BR, width, height, radius, color1.getRGB());
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.colorMask(false, false, false, true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT);
        RenderSystem.colorMask(true, true, true, true);

        drawRoundedRectangle(matrix, x, y, TL, TR, BL, BR, width, height, radius, color1.getRGB());

        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.enableBlend();
        BufferBuilder bufferBuilder = setupAndBegin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix, x, y + height, 0.0F).color(color1.getRGB());
        bufferBuilder.vertex(matrix, x + width, y + height, 0.0F).color(color2.getRGB());
        bufferBuilder.vertex(matrix, x + width, y, 0.0F).color(color3.getRGB());
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(color4.getRGB());

        draw(bufferBuilder);

        RenderSystem.disableBlend();

        RenderSystem.defaultBlendFunc();
    }
    /**
     * Draws an outline rounded rectangle using lines and arcs.
     *
     * @param matrix4f  Matrix4f object to draw the rounded rectangle
     * @param x         X pos
     * @param y         Y pos
     * @param width     Width of rounded rectangle
     * @param height    Height of rounded rectangle
     * @param radius    Radius of the quadrants / the rounded rectangle
     * @param color     Color of the rounded rectangle
     * @param thickness Thickness of the outline
     */
    public static void drawOutlineRoundedBox(Matrix4f matrix4f, float x, float y, float width, float height, float radius, float thickness, int color) {
        // Ensure the radius is not larger than half the width or height
        radius = Math.min(radius, Math.min(width, height) / 2);

        // Draw the straight edges using lines
        drawLine(matrix4f, x + radius, y, x + width - radius, y, thickness, color); // Top edge
        drawLine(matrix4f, x + width, y + radius, x + width, y + height - radius, thickness, color); // Right edge
        drawLine(matrix4f, x + width - radius, y + height, x + radius, y + height, thickness, color); // Bottom edge
        drawLine(matrix4f, x, y + height - radius, x, y + radius, thickness, color); // Left edge

        // Draw the arcs at the corners for the outline
        drawArc(matrix4f, x + radius, y + radius, radius, thickness, color, 180, 270); // Top-left arc
        drawArc(matrix4f, x + width - radius, y + radius, radius, thickness, color, 90, 180); // Top-right arc
        drawArc(matrix4f, x + width - radius, y + height - radius, radius, thickness, color, 0, 90); // Bottom-right arc
        drawArc(matrix4f, x + radius, y + height - radius, radius, thickness, color, 270, 360); // Bottom-left arc
    }

    /**
     * Draws an outline rounded rectangle with a shadow
     *
     * @param matrices  MatrixStack object to draw the rounded rectangle and shadow
     * @param x         X pos
     * @param y         Y pos
     * @param width     Width of rounded rectangle
     * @param height    Height of rounded rectangle
     * @param radius    Radius of the quadrants / the rounded rectangle
     * @param color     Color of the rounded rectangle
     * @param thickness thickness of the outline
     */
    public static void drawOutlineRoundedBoxWithShadow(MatrixStack matrices, float x, float y, float width, float height, float radius, float thickness, int color, int shadowRadius, Color shadowColor) {
        //First draw shadow
        drawOutlineBlurredShadow(matrices,x,y,width,height,shadowRadius,shadowColor,true,radius);

        drawOutlineRoundedBox(matrices.peek().getPositionMatrix(),x,y,width,height,radius,thickness,color);
    }

    public static void drawTexture(MatrixStack matrices, double x, double y, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight) {
        double x1 = x + width;
        double y1 = y + height;
        double z = 0;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(matrix, (float) x, (float) y1, (float) z).texture((u) / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight);
        buffer.vertex(matrix, (float) x1, (float) y1, (float) z).texture((u + (float) regionWidth) / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight);
        buffer.vertex(matrix, (float) x1, (float) y, (float) z).texture((u + (float) regionWidth) / (float) textureWidth, (v) / (float) textureHeight);
        buffer.vertex(matrix, (float) x, (float) y, (float) z).texture((u) / (float) textureWidth, (v + 0.0F) / (float) textureHeight);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private static List<Color> generateGradientColors(Color[] colors, int steps) {
        List<Color> gradientColors = new ArrayList<>();

        for (int i = 0; i < colors.length - 1; i++) {
            Color startColor = colors[i];
            Color endColor = colors[i + 1];

            for (int j = 0; j < steps; j++) {
                float ratio = (float) j / (float) steps;
                int red = (int) (startColor.getRed() * (1 - ratio) + endColor.getRed() * ratio);
                int green = (int) (startColor.getGreen() * (1 - ratio) + endColor.getGreen() * ratio);
                int blue = (int) (startColor.getBlue() * (1 - ratio) + endColor.getBlue() * ratio);
                int alpha = (int) (startColor.getAlpha() * (1 - ratio) + endColor.getAlpha() * ratio);

                gradientColors.add(new Color(red, green, blue, alpha));
            }
        }

        // Add the final end color
        gradientColors.add(colors[colors.length - 1]);

        return gradientColors;
    }

    public static void drawRoundedGradientRectangle(Matrix4f matrix, Color[] colors, float x, float y, float width, float height, float radius, boolean TL, boolean TR, boolean BL, boolean BR) {
        // Generate intermediate colors with 10 steps for each transition
        List<Color> gradientColors = generateGradientColors(colors, 10);

        // Draw a single rounded rectangle for the same colors
        if (Arrays.stream(colors).allMatch(color -> color.equals(colors[0]))) {
            drawRoundedRectangle(matrix, x, y, TL, TR, BL, BR, width, height, radius, colors[0].getRGB());
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.colorMask(false, false, false, true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT);
        RenderSystem.colorMask(true, true, true, true);
        drawRoundedRectangle(matrix, x, y, TL, TR, BL, BR, width, height, radius, colors[0].getRGB());

        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableBlend();

        BufferBuilder bufferBuilder = setupAndBegin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        float stepY = y;
        float stepHeight = height / (gradientColors.size() - 1);  // Ensure proper interpolation steps

        for (int i = 0; i < gradientColors.size() - 1; i++) {
            Color colorTop = gradientColors.get(i);
            Color colorBottom = gradientColors.get(i + 1);

            bufferBuilder.vertex(matrix, x, stepY + stepHeight, 0.0F).color(colorBottom.getRGB());
            bufferBuilder.vertex(matrix, x + width, stepY + stepHeight, 0.0F).color(colorBottom.getRGB());
            bufferBuilder.vertex(matrix, x + width, stepY, 0.0F).color(colorTop.getRGB());
            bufferBuilder.vertex(matrix, x, stepY, 0.0F).color(colorTop.getRGB());

            stepY += stepHeight;
        }

        draw(bufferBuilder);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }


    /**
     * Draws and masks to a gradient;
     */
    public static void drawToGradientMask(Matrix4f matrix, Color color1, Color color2, Color color3, Color color4, float x, float y, float width, float height,Runnable run) {
        RenderSystem.enableBlend();
        RenderSystem.colorMask(false, false, false, true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT);
        RenderSystem.colorMask(true, true, true, true);

        run.run();

        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        BufferBuilder bufferBuilder = setupAndBegin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix, x, y + height, 0.0F).color(color1.getRGB());
        bufferBuilder.vertex(matrix, x + width, y + height, 0.0F).color(color2.getRGB());
        bufferBuilder.vertex(matrix, x + width, y, 0.0F).color(color3.getRGB());
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(color4.getRGB());

        draw(bufferBuilder);

        RenderSystem.disableBlend();

        RenderSystem.defaultBlendFunc();
    }

    public static BufferBuilder setupAndBegin(VertexFormat.DrawMode m, VertexFormat vf) {
        return Tessellator.getInstance().begin(m,vf);
    }
    

    static void draw(BufferBuilder builder) {
        BufferRenderer.drawWithGlobalProgram(builder.end());
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

    public static void drawLine(Matrix4f matrix, float x1, float y1, float x2, float y2,float lineWidth, int color) {
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        float alpha = (color >> 24 & 255) / 255.0F;

        RenderSystem.lineWidth(lineWidth);

        BufferBuilder bufferBuilder = setupAndBegin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        bufferBuilder.vertex(matrix, x1, y1, 0.0F).color(red, green, blue, alpha);
        bufferBuilder.vertex(matrix, x2, y2, 0.0F).color(red, green, blue, alpha);

        draw(bufferBuilder);

        RenderSystem.lineWidth(1.0f);
    }

    public static void drawVerticalLine(Matrix4f matrix4f, float x, float y, float height, float thickness, int color) {
        drawRectangle(matrix4f, x, y, thickness, height, color);
    }

    public static void drawHorizontalLine(Matrix4f matrix4f, float x1, float width, float y, float height, int color) {
        drawRectangle(matrix4f, x1, y, width, height, color);
    }

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
        entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, HeliosClient.MC.getRenderTime(), context.getMatrices(), HeliosClient.MC.getBufferBuilders().getEntityVertexConsumers(), 15728880);
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

    /* ==== Drawing Custom Stuff ==== */
    public static void drawDisplayPreviewEntity( int x, int y, int size, DisplayPreviewEntity entity, float mouseX, float mouseY) {
        Matrix4fStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.pushMatrix();
        matrixStack.translate(x, y, 1050.0f);
        matrixStack.scale(1.0f, 1.0f, -1.0f);
        MatrixStack matrixStack2 = new MatrixStack();
        matrixStack2.translate(0.0, 0.0, 1000.0);
        matrixStack2.scale(size, size, size);

        matrixStack2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));

        DiffuseLighting.method_34742();
        EntityRenderDispatcher entityRenderDispatcher = mc.getEntityRenderDispatcher();
        entityRenderDispatcher.setRenderShadows(false);
        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
        EntityRendererFactory.Context ctx = new EntityRendererFactory.Context(
                    mc.getEntityRenderDispatcher(),
                    mc.getItemModelManager(),
                    mc.getMapRenderer(),
                    mc.getBlockRenderManager(),
                    mc.getResourceManager(),
                    mc.getLoadedEntityModels(),
                    new EquipmentModelLoader(),
                    mc.textRenderer
        );

        DisplayPreviewEntityRenderer displayPlayerEntityRenderer = new DisplayPreviewEntityRenderer(ctx,entity.slim);
        displayPlayerEntityRenderer.render(entity,x,y, mc.getRenderTime(), matrixStack2, immediate, 0xF000F0,mouseX, mouseY);
        immediate.draw();
        entityRenderDispatcher.setRenderShadows(true);
        matrixStack.popMatrix();
        DiffuseLighting.enableGuiDepthLighting();
    }

    public static void drawEntity(DrawContext context, int x, int y, int size, Entity entity, float delta) {
        float yaw = MathHelper.wrapDegrees(entity.prevYaw + (entity.getYaw() - entity.prevYaw) * HeliosClient.MC.getRenderTickCounter().getTickDelta(false));
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
        context.getMatrices().translate(x, y, 70.0);
        context.getMatrices().multiplyPositionMatrix((new Matrix4f()).scaling((float) size, (float) size, (float) (-size)));
        context.getMatrices().multiply(quaternionf);
        DiffuseLighting.method_34742();
        EntityRenderDispatcher entityRenderDispatcher = HeliosClient.MC.getEntityRenderDispatcher();
        if (quaternionf2 != null) {
            quaternionf2.conjugate();
            entityRenderDispatcher.setRotation(quaternionf2);
        }

        entityRenderDispatcher.setRenderShadows(false);
        entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, delta, context.getMatrices(), HeliosClient.MC.getBufferBuilders().getEntityVertexConsumers(), 15728880);
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

    public static float getStringWidth(String text) {
        if (isVanillaRenderer()) {
            return HeliosClient.MC.textRenderer.getWidth(text);
        }
        return getFontRenderer() != null ? getFontRenderer().getStringWidth(text) : 0;
    }

    /* ==== Projection ==== */

    public static void unscaledProjection() {
        projectionType = RenderSystem.getProjectionType();
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0, mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight(), 0, 1000, 21000), ProjectionType.ORTHOGRAPHIC);
    }

    public static void scaledProjection() {
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0, (float) (mc.getWindow().getFramebufferWidth() / mc.getWindow().getScaleFactor()), (float) (mc.getWindow().getFramebufferHeight() / mc.getWindow().getScaleFactor()), 0, 1000, 21000), projectionType);
    }

    public static void customScaledProjection(float scale) {
        projectionType = RenderSystem.getProjectionType();
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0, mc.getWindow().getFramebufferWidth() / scale, mc.getWindow().getFramebufferHeight() / scale, 0, 1000, 21000),  ProjectionType.ORTHOGRAPHIC);
    }

    /* ==== Drawing Custom Text ==== */

    public static float getCustomStringWidth(String text, FontRenderer fontRenderer) {
        if (fontRenderer == null)
            return 0;

        if (isVanillaRenderer()) {
            return HeliosClient.MC.textRenderer.getWidth(text);
        }
        return fontRenderer.getTextWidth(Text.of(text));
    }

    public static float getStringWidth() {
        return getStringWidth(TEXT);
    }

    public static float getCustomStringWidth(FontRenderer fontRenderer) {
        if (fontRenderer == null)
            return 0;
        if (isVanillaRenderer()) {
            return HeliosClient.MC.textRenderer.getWidth(TEXT);
        }

        return fontRenderer.getTextWidth(Text.of(TEXT));
    }

    public static float getFxStringWidth(String text) {
        if (isVanillaRenderer()) {
            return HeliosClient.MC.textRenderer.getWidth(text);
        }
        return getFxFontRenderer() != null ? getFxFontRenderer().getTextWidth(Text.of(text)) : 0;
    }

    public static float getFxStringWidth() {
        return getFxStringWidth(TEXT);
    }

    public static float getStringHeight(String text) {
        if (isVanillaRenderer()) {
            return HeliosClient.MC.textRenderer.fontHeight;
        }
        return getFontRenderer() != null ? getFontRenderer().getTextHeight(Text.of(text)) : 0;
    }

    public static float getCustomStringHeight(String text, FontRenderer fontRenderer) {
        if (fontRenderer == null)
            return 0;

        if (isVanillaRenderer()) {
            return HeliosClient.MC.textRenderer.fontHeight;
        }
        return fontRenderer.getTextHeight(Text.of(text));
    }

    public static float getStringHeight() {
        return getStringHeight(TEXT);
    }

    public static float getCustomStringHeight(FontRenderer fontRenderer) {
        if (fontRenderer == null)
            return 0;
        if (isVanillaRenderer()) {
            return HeliosClient.MC.textRenderer.fontHeight;
        }

        return fontRenderer.getTextHeight(Text.of(TEXT));
    }

    public static float getFxStringHeight(String text) {
        if (isVanillaRenderer()) {
            return HeliosClient.MC.textRenderer.fontHeight;
        }
        return getFxFontRenderer() != null ? getFxFontRenderer().getTextHeight(Text.of(text)) : 0;
    }

    public static float getFxStringHeight() {
        return getFxStringHeight(TEXT);
    }

    public static boolean isVanillaRenderer() {
        return renderer == Renderers.VANILLA && drawContext != null && HeliosClient.MC.textRenderer != null;
    }

    public static BetterFontRenderer getFxFontRenderer() {
        return FontRenderers.fxfontRenderer;
    }

    public static FontRenderer getFontRenderer() {
        return FontRenderers.fontRenderer;
    }

    public static void drawString(MatrixStack matrixStack, String text, float x, float y, int color) {
        if (isVanillaRenderer()) {
            color = fixColorValue(color);
            drawContext.drawText(HeliosClient.MC.textRenderer, text, (int) x, (int) y, color, false);
        } else if (getFontRenderer() != null) {
            try {
                getFontRenderer().drawString(matrixStack, text, x, y - 1, ColorUtils.getRed(color)/255.0F, ColorUtils.getGreen(color)/255.0F, ColorUtils.getBlue(color)/255.0F, ColorUtils.getAlpha(color)/255.0F);
            } catch (NullPointerException ignored) {
            }
        }
    }

    public static void drawCenteredString(MatrixStack matrixStack, String text, float x, float y, int color) {
        if (isVanillaRenderer()) {
            color = fixColorValue(color);
            drawContext.drawText(HeliosClient.MC.textRenderer, text, (int) x, (int) y, color, false);
        } else if (getFontRenderer() != null) {
            getFontRenderer().drawCenteredString(matrixStack, text, x, y - 1, ColorUtils.getRed(color)/255.0F, ColorUtils.getGreen(color)/255.0F, ColorUtils.getBlue(color)/255.0F, ColorUtils.getAlpha(color)/255.0F);
        }
    }

    public static void drawFixedString(MatrixStack matrixStack, String text, float x, float y, int color) {
        if (isVanillaRenderer()) {
            color = fixColorValue(color);
            drawContext.drawText(HeliosClient.MC.textRenderer, text, (int) x, (int) y, color, false);
        } else if (getFxFontRenderer() != null) {
            try {
                getFxFontRenderer().drawString(matrixStack, text, x, y, color);
            } catch (NullPointerException ignored) {
            }
        }
    }

    public static void drawFixedCenteredString(MatrixStack matrixStack, String text, float x, float y, int color) {
        if (isVanillaRenderer()) {
            color =fixColorValue(color);
            drawContext.drawText(HeliosClient.MC.textRenderer, text, (int) x, (int) y, color, false);
        } else if (getFxFontRenderer() != null) {
            getFxFontRenderer().drawCenteredString(matrixStack, text, x, y, color);
        }
    }

    public static void drawCustomString(BetterFontRenderer fontRenderer, MatrixStack matrixStack, String text, float x, float y, int color) {
        if (isVanillaRenderer()) {
            color =fixColorValue(color);
            drawContext.drawText(HeliosClient.MC.textRenderer, text, (int) x, (int) y, color, false);
        } else if (fontRenderer != null) {
            try {
                fontRenderer.drawString(matrixStack, text, x, y, color);
            } catch (NullPointerException ignored) {
            }
        }
    }

    public static void drawCustomCenteredString(BetterFontRenderer fontRenderer, MatrixStack matrixStack, String text, float x, float y, int color) {
        if (isVanillaRenderer()) {
            color = fixColorValue(color);
            drawContext.drawText(HeliosClient.MC.textRenderer, text, (int) x, (int) y, color, false);
        } else if (fontRenderer != null) {
            fontRenderer.drawCenteredString(matrixStack, text,x, y, color);
        }
    }
    private static int fixColorValue(int color){
        return ColorUtils.changeAlpha(color,5f,5).getRGB();
    }

    public static void setRenderer(Renderers renderer) {
        Renderer2D.renderer = renderer;
    }

    public static void setDrawContext(DrawContext drawContext) {
        Renderer2D.drawContext = drawContext;
    }

    public static List<String> wrapText(String text, int maxWidth, TextRenderer textRenderer) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            int lineWidth = textRenderer.getWidth(line + word + " ");
            if (lineWidth > maxWidth) {
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

    public static List<String> wrapText(String text, int maxWidth, BetterFontRenderer fontRenderer) {
        List<String> lines = new ArrayList<>();

        if (Math.ceil(getCustomStringWidth(text, fontRenderer)) < maxWidth) {
            lines.add(text);
            return lines;
        }

        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int currentLineWidth = 0;

        for (String word : words) {
            int wordWidth = (int) Math.ceil(getCustomStringWidth(word + " ", fontRenderer));
            if (currentLineWidth + wordWidth >= maxWidth) {
                lines.add(line.toString());
                line.setLength(0);
                currentLineWidth = 0;
            }
            line.append(word).append(" ");
            currentLineWidth += wordWidth;
        }

        if (!line.isEmpty()) {
            lines.add(line.toString());
        }

        return lines;
    }


    public static List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            int lineWidth = Math.round(Renderer2D.getStringWidth(line + word + " "));
            if (lineWidth > maxWidth) {
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

    public static void drawBottomText(MatrixStack stack,int x,BetterFontRenderer fontRenderer, String... textBottomToTop){
        float fontHeight = getCustomStringHeight(fontRenderer);

        for(int i = 1; i <= textBottomToTop.length; i++){
            String text = textBottomToTop[i - 1];
            fontRenderer.drawString(stack, text, x, drawContext.getScaledWindowHeight() - (i * fontHeight) - i * 2, -1);
        }
    }

    /**
     *
     */
    public void deleteShadowCache(){
        outlineShadowCache.clear();
        shadowCache.clear();
    }


    @SubscribeEvent
    public void renderEvent(RenderEvent renderEvent) {
        drawContext = renderEvent.getDrawContext();
    }

    public enum Direction {
        // Left_Right means from left to right. Same for others //
        LEFT_RIGHT, TOP_BOTTOM, RIGHT_LEFT, BOTTOM_TOP;

        public int[] getVertexColor(int startColor, int endColor){
            int[] array = new int[4];
            switch (this.ordinal()) {
                case 0 -> {
                    array[0] = startColor;
                    array[3] = startColor;
                    array[1] = endColor;
                    array[2] = endColor;
                }
                case 1 -> {
                    array[0] = startColor;
                    array[1] = startColor;
                    array[2] = endColor;
                    array[3] = endColor;
                }
                case 2 -> {
                    array[0] = endColor;
                    array[3] = endColor;
                    array[1] = startColor;
                    array[2] = startColor;
                }
                case 3 -> {
                    array[0] = endColor;
                    array[1] = endColor;
                    array[2] = startColor;
                    array[3] = startColor;
                }
                default -> {
                    throw new IllegalArgumentException("Direction cannot be more than 3");
                }
            }
            return array;
        }


        public Direction getOpposite(){
            return switch (this) {
                case LEFT_RIGHT -> RIGHT_LEFT;
                case RIGHT_LEFT -> LEFT_RIGHT;
                case TOP_BOTTOM -> BOTTOM_TOP;
                default -> TOP_BOTTOM;
            };
        }
    }

    public enum Renderers {
        CUSTOM,
        VANILLA
    }

    // https://github.com/Pan4ur/ThunderHack-Recode/blob/main/src/main/java/thunder/hack/utility/render/Render2DEngine.java
    public static class BlurredShadow {
        Identifier id;

        public BlurredShadow(BufferedImage bufferedImage) {
            this.id = Identifier.of(HeliosClient.MODID,"identifier/blur/" + RandomStringUtils.randomAlphanumeric(16).toLowerCase());
            registerBufferedImageTexture(id, bufferedImage);
        }

        public void bind() {
            RenderSystem.setShaderTexture(0, id);
        }
    }

    public enum RectRenderStyle {
        ROUNDED,
        PLAIN
    }
}