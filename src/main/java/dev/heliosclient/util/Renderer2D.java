package dev.heliosclient.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.RenderEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.util.fontutils.fxFontRenderer;
import me.x150.renderer.font.FontRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Renderer2D implements Listener {

    public static DrawContext drawContext;

    public static Renderers renderer = Renderers.CUSTOM;
    @SubscribeEvent
    public void renderEvent(RenderEvent renderEvent){
        drawContext = renderEvent.getDrawContext();
    }

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
        bufferBuilder.vertex(matrix4f,x + width,y ,0.0F).color(red ,green ,blue ,alpha ).next();
        bufferBuilder.vertex(matrix4f,x ,y ,0.0F).color(red ,green ,blue ,alpha ).next();

        tessellator.draw();

        RenderSystem.disableBlend();
    }



    public static void drawRoundedRectangle(Matrix4f matrix4f, float x, float y, float width, float height, float radius, int color) {
        // Draw the main rectangle
        drawRectangle(matrix4f, x + radius, y + radius, width - 2 * radius, height - 2 * radius, color);

        // Draw rectangles at the sides
        drawRectangle(matrix4f, x + radius, y, width - 2 * radius, radius, color); // top
        drawRectangle(matrix4f, x + radius, y + height - radius, width - 2 * radius, radius, color); // bottom
        drawRectangle(matrix4f, x, y + radius, radius, height - 2 * radius, color); // left
        drawRectangle(matrix4f, x + width - radius, y + radius, radius, height - 2 * radius, color); // right

        // Draw quadrants at the corners
        drawFilledQuadrant(matrix4f, x + radius, y + radius, radius, color,2);
        drawFilledQuadrant(matrix4f, x + width - radius, y + radius, radius, color,1);
        drawFilledQuadrant(matrix4f, x + radius, y + height - radius, radius, color,3);
        drawFilledQuadrant(matrix4f, x + width - radius, y + height - radius, radius, color,4);
    }

    public static void drawRoundedRectangle(Matrix4f matrix4f, float x, float y, boolean TL, boolean TR, boolean BL, boolean BR, float width, float height, int radius, int color) {
        // Draw the main rectangle
        drawRectangle(matrix4f, x + radius, y + radius, width - 2 * radius, height - 2 * radius, color);

        // Draw rectangles at the sides
        drawRectangle(matrix4f, x + radius, y, width - 2 * radius, radius, color); // top
        drawRectangle(matrix4f, x + radius, y + height - radius, width - 2 * radius, radius, color); // bottom
        drawRectangle(matrix4f, x, y + radius, radius, height - 2 * radius, color); // left
        drawRectangle(matrix4f, x + width - radius, y + radius, radius, height - 2 * radius, color); // right

        if (TL) {
            drawFilledQuadrant(matrix4f, x + radius, y + radius, radius, color,2);
        } else {
            drawRectangle(matrix4f, x, y, radius, radius, color);
        }
        if (TR) {
            drawFilledQuadrant(matrix4f, x + width - radius, y + radius, radius, color,1);
        } else {
            drawRectangle(matrix4f, x + width - radius, y, radius, radius, color);
        }
        if (BL) {
            drawFilledQuadrant(matrix4f, x + radius, y + height - radius, radius, color,3);
        } else {
            drawRectangle(matrix4f, x, y + height - radius, radius, radius, color);
        }
        if (BR) {
            drawFilledQuadrant(matrix4f, x + width - radius, y + height - radius, radius, color,4);
        } else {
            drawRectangle(matrix4f, x + width - radius, y + height - radius, radius, radius, color);
        }
    }


    public static void drawCircle(Matrix4f matrix4f, int xCenter, int yCenter, int radius, int color) {
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
            bufferBuilder.vertex(matrix4f, (float) x, (float) y, 0).color(red, green, blue, alpha).next();
        }

        tessellator.draw();
    }

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

        Matrix4f model = matrix4f;

        for (float angle = startAngle; angle <= endAngle; angle += 1.0F) {
            float x1 = x + MathHelper.cos(angle * 0.017453292F) * radius;
            float y1 = y + MathHelper.sin(angle * 0.017453292F) * radius;
            float x2 = x + MathHelper.cos((angle + 1.0F) * 0.017453292F) * radius;
            float y2 = y + MathHelper.sin((angle + 1.0F) * 0.017453292F) * radius;

            bufferBuilder.vertex(model, x, y, 0).color(red, green, blue, alpha).next();
            bufferBuilder.vertex(model, x1, y1, 0).color(red, green, blue, alpha).next();
            bufferBuilder.vertex(model, x2, y2, 0).color(red, green, blue, alpha).next();
        }
        tessellator.draw();
        RenderSystem.disableBlend();
    }

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
    public static void drawOutlineRoundedBox(Matrix4f matrix4f, float x, float y, float width, float height, float radius, float thickness, int color) {
        // Draw the rectangles for the outline
        drawRectangle(matrix4f, x + radius, y, width - radius * 2, thickness, color); // Top rectangle
        drawRectangle(matrix4f, x + radius, y + height - thickness, width - radius * 2, thickness, color); // Bottom rectangle
        drawRectangle(matrix4f, x, y + radius, thickness, height - radius * 2, color); // Left rectangle
        drawRectangle(matrix4f, x + width - thickness, y + radius, thickness, height - radius * 2, color); // Right rectangle

        // Draw the arcs at the corners for the outline
        drawArc(matrix4f,x + radius,y + radius,radius ,thickness ,color ,180 ,270 ); // Top-left arc
        drawArc(matrix4f,x + width - radius,y + radius,radius ,thickness ,color ,90 ,180 ); // Top-right arc
        drawArc(matrix4f,x + width - radius,y + height - radius,radius ,thickness ,color ,0 ,90 ); // Bottom-right arc
        drawArc(matrix4f,x + radius,y + height - radius,radius ,thickness ,color ,270 ,360 ); // Bottom-left arc
    }

    public static void drawArc(Matrix4f matrix4f,float xCenter,float yCenter,float radius,float thickness,int color,int startAngle,int endAngle ){
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP ,VertexFormats.POSITION_COLOR);
        RenderSystem.enableBlend();

        for(int i =startAngle;i <=endAngle;i++){
            double innerX = xCenter + Math.sin(Math.toRadians(i)) * (radius-thickness);
            double innerY = yCenter + Math.cos(Math.toRadians(i)) * (radius-thickness);
            double outerX = xCenter + Math.sin(Math.toRadians(i)) * radius;
            double outerY = yCenter + Math.cos(Math.toRadians(i)) * radius;

            bufferBuilder.vertex(matrix4f,(float)innerX ,(float)innerY ,0).color(red ,green ,blue ,alpha ).next();
            bufferBuilder.vertex(matrix4f,(float)outerX ,(float)outerY ,0).color(red ,green ,blue ,alpha ).next();
        }

        tessellator.draw();

        RenderSystem.disableBlend();
    }


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
            startAngle = 0;
            endAngle = 90;
        }

        drawArc(matrix4f, xCenter, yCenter, radius,1f, color,startAngle, endAngle);
    }

    public static void drawLine(Matrix4f matrix4f, int x1, int y1, int x2, int y2, int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.defaultBlendFunc();

        bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, x1, y1, 0).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(matrix4f, x2, y2, 0).color(red, green, blue, alpha).next();

        tessellator.draw();
        RenderSystem.disableBlend();
    }

    public static void drawFilledTriangle(Matrix4f matrix4f, int x1, int y1, int x2, int y2, int x3, int y3, int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix4f, x1, y1, 0).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(matrix4f, x2, y2, 0).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(matrix4f, x3, y3, 0).color(red, green, blue, alpha).next();

        tessellator.draw();
    }


    public static void drawOutlineBox(Matrix4f matrix4f, float x, float y, float width, float height, float thickness, int color) {
        drawRectangle(matrix4f, x, y, width, thickness, color);
        drawRectangle(matrix4f, x, y + height - thickness, width, thickness, color);
        drawRectangle(matrix4f, x, y + thickness, thickness, height - thickness * 2, color);
        drawRectangle(matrix4f, x + width - thickness, y + thickness, thickness, height - thickness * 2, color);
    }
    public static void drawGradient(Matrix4f matrix4f, float x, float y, float width, float height, int startColor, int endColor) {
        float startRed = (float) (startColor >> 16 & 255) / 255.0F;
        float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
        float startBlue = (float) (startColor & 255) / 255.0F;
        float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;

        float endRed = (float) (endColor >> 16 & 255) / 255.0F;
        float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
        float endBlue = (float) (endColor & 255) / 255.0F;
        float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;

        ;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix4f, x, y + height, 0.0F).color(startRed, startGreen, startBlue, startAlpha).next();
        bufferBuilder.vertex(matrix4f, x + width, y + height, 0.0F).color(startRed, startGreen, startBlue, startAlpha).next();
        bufferBuilder.vertex(matrix4f,x + width,y ,0.0F).color(endRed ,endGreen ,endBlue ,endAlpha ).next();
        bufferBuilder.vertex(matrix4f,x ,y ,0.0F).color(endRed ,endGreen ,endBlue ,endAlpha ).next();

        tessellator.draw();

        RenderSystem.disableBlend();
    }
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
            float t = (float)(i - quadrant * 90) / 90.0f;
            float red = startRed * (1 - t) + endRed * t;
            float green = startGreen * (1 - t) + endGreen * t;
            float blue = startBlue * (1 - t) + endBlue * t;
            float alpha = startAlpha * (1 - t) + endAlpha * t;

            bufferBuilder.vertex(matrix4f, (float)x, (float)y, 0).color(red, green, blue, alpha).next();
        }

        tessellator.draw();
        RenderSystem.disableBlend();
    }
    public static void drawRoundedGradientRectangle(Matrix4f matrix, Color color1, Color color2, Color color3, Color color4, float x, float y, float width, float height, float radius) {
        RenderSystem.enableBlend();
        RenderSystem.colorMask(false, false, false, true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, false);
        RenderSystem.colorMask(true, true, true, true);

        drawRoundedRectangle(matrix, x, y, width, height, (int) radius, color1.getRGB());
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.blendFunc(GL11.GL_DST_ALPHA, GL11.GL_ONE_MINUS_DST_ALPHA);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix, x + width, y + height, 0.0F).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha()).next();
        bufferBuilder.vertex(matrix, x, y + height, 0.0F).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(color4.getRed(), color4.getGreen(), color4.getBlue(), color4.getAlpha()).next();
        bufferBuilder.vertex(matrix, x + width, y, 0.0F).color(color3.getRed(), color3.getGreen(), color3.getBlue(), color3.getAlpha()).next();

        Tessellator.getInstance().draw();
        RenderSystem.disableBlend();

        RenderSystem.defaultBlendFunc();
    }

    private static final String TEXT = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-=_+|{};<>?~`,./;'[] ";

    public static float getStringWidth(String text){
        if(isVanillaRenderer()) {
            return HeliosClient.MC.textRenderer.getWidth(text);
        }
        return getFontRenderer() != null ? getFontRenderer().getStringWidth(text) : 0;
    }

    public static float getStringWidth(){
        return getStringWidth(TEXT);
    }

    public static float getFxStringWidth(String text){
        if(isVanillaRenderer()) {
            return HeliosClient.MC.textRenderer.getWidth(text);
        }
        return getFxFontRenderer() != null ? getFxFontRenderer().getStringWidth(text) : 0;
    }

    public static float getFxStringWidth(){
        return getFxStringWidth(TEXT);
    }

    public static float getStringHeight(String text){
        if(isVanillaRenderer()) {
            return HeliosClient.MC.textRenderer.fontHeight;
        }
        return getFontRenderer() != null ? getFontRenderer().getStringHeight(text) : 0;
    }

    public static float getStringHeight(){
        return getStringHeight(TEXT);
    }

    public static float getFxStringHeight(String text){
        if(isVanillaRenderer()) {
            return HeliosClient.MC.textRenderer.fontHeight;
        }
        return getFxFontRenderer() != null ? getFxFontRenderer().getStringHeight(text) : 0;
    }

    public static float getFxStringHeight(){
        return getFxStringHeight(TEXT);
    }

    public static boolean isVanillaRenderer() {
        return renderer == Renderers.VANILLA && drawContext!=null && HeliosClient.MC.textRenderer!=null;
    }
    public static fxFontRenderer getFxFontRenderer(){
        return FontManager.fxfontRenderer;
    }
    public static FontRenderer getFontRenderer(){
        return  FontManager.fontRenderer;
    }
    public static void drawString(MatrixStack matrixStack, String text, float x, float y, int color){
        if(isVanillaRenderer()) {
            drawContext.drawText(HeliosClient.MC.textRenderer,text, (int) x, (int) y,color,false);
        }
        else if(getFontRenderer()!=null) {
            getFontRenderer().drawString(matrixStack, text, x, y, 256 - ColorUtils.getRed(color), 256 - ColorUtils.getGreen(color), 256 - ColorUtils.getBlue(color), 256 - ColorUtils.getAlpha(color));
        }
    }
    public static void drawCenteredString(MatrixStack matrixStack, String text, float x, float y, int color){
        if(isVanillaRenderer()) {
            drawContext.drawText(HeliosClient.MC.textRenderer,text, (int) x, (int) y,color,false);
        }
        else if(getFontRenderer()!=null) {
            getFontRenderer().drawCenteredString(matrixStack, text, x, y, 256 - ColorUtils.getRed(color), 256 - ColorUtils.getGreen(color), 256 - ColorUtils.getBlue(color), 256 - ColorUtils.getAlpha(color));
        }
    }
    public static void drawFixedString(MatrixStack matrixStack, String text, float x, float y, int color){
        if(isVanillaRenderer()) {
            drawContext.drawText(HeliosClient.MC.textRenderer,text, (int) x, (int) y,color,false);
        }
        else if(getFxFontRenderer()!=null) {
            getFxFontRenderer().drawString(matrixStack, text, x, y, color);
        }
    }
    public static void drawFixedCenteredString(MatrixStack matrixStack, String text, float x, float y, int color){
        if(isVanillaRenderer()) {
            drawContext.drawText(HeliosClient.MC.textRenderer,text, (int) x, (int) y,color,false);
        }
        else if(getFxFontRenderer()!=null) {
            getFxFontRenderer().drawCenteredString(matrixStack, text, x, y, color);
        }
    }

    public enum Renderers{
        CUSTOM,
        VANILLA
    }

    public static void setRenderer(Renderers renderer) {
        Renderer2D.renderer = renderer;
    }

    public static void setDrawContext(DrawContext drawContext) {
        Renderer2D.drawContext = drawContext;
    }
}