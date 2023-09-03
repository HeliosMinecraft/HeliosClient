package dev.heliosclient.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

public class Renderer2D extends DrawContext {

    public Renderer2D(MinecraftClient client, VertexConsumerProvider.Immediate vertexConsumers) {
        super(client, vertexConsumers);
    }

    public static void fill(DrawContext drawContext, int x1, int y1, int x2, int y2, int color) {
        int i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }

        float f = (float) (color >> 24 & 255) / 255.0F;
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float j = (float) (color & 255) / 255.0F;
        Matrix4f matrix4f = drawContext.getMatrices().peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.defaultBlendFunc();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, (float) x1, (float) y2, 0.0F).color(g, h, j, f).next();
        bufferBuilder.vertex(matrix4f, (float) x2, (float) y2, 0.0F).color(g, h, j, f).next();
        bufferBuilder.vertex(matrix4f, (float) x2, (float) y1, 0.0F).color(g, h, j, f).next();
        bufferBuilder.vertex(matrix4f, (float) x1, (float) y1, 0.0F).color(g, h, j, f).next();
        tessellator.draw();
        RenderSystem.disableBlend();
    }

    public static void drawRectangle(DrawContext drawContext, int x, int y, int width, int height, int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        Matrix4f matrix4f = drawContext.getMatrices().peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.defaultBlendFunc();

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix4f, x, y + height, 0.0F).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(matrix4f, x + width, y + height, 0.0F).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(matrix4f, x + width, y, 0.0F).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(matrix4f, x, y, 0.0F).color(red, green, blue, alpha).next();
        tessellator.draw();
        RenderSystem.disableBlend();
    }

    public static void drawRoundedRectangle(DrawContext drawContext, int x, int y, int width, int height, int radius, int color) {
        drawRectangle(drawContext, x + radius, y, width - radius * 2, height, color);
        drawRectangle(drawContext, x, y + radius, width, height - radius * 2, color);

        // Draw circles at the corners
        drawFilledCircle(drawContext, x + radius, y + radius, radius, color);
        drawFilledCircle(drawContext, x + width - radius, y + radius, radius, color);
        drawFilledCircle(drawContext, x + width - radius, y + height - radius, radius, color);
        drawFilledCircle(drawContext, x + radius, y + height - radius, radius, color);
    }

    public static void drawRoundedRectangle(DrawContext drawContext, int x, int y, boolean TL, boolean TR, boolean BL, boolean BR, int width, int height, int radius, int color) {
        drawRectangle(drawContext, x + radius, y, width - radius * 2, height, color);
        drawRectangle(drawContext, x, y + radius, width, height - radius * 2, color);

        if (TL) {
            drawFilledCircle(drawContext, x + radius, y + radius, radius, color);
        } else {
            drawRectangle(drawContext, x, y, radius, radius, color);
        }
        if (TR) {
            drawFilledCircle(drawContext, x + width - radius, y + radius, radius, color);
        } else {
            drawRectangle(drawContext, x + width - radius, y, radius, radius, color);
        }
        if (BL) {
            drawFilledCircle(drawContext, x + radius, y + height - radius, radius, color);
        } else {
            drawRectangle(drawContext, x, y + height - radius, radius, radius, color);
        }
        if (BR) {
            drawFilledCircle(drawContext, x + width - radius, y + height - radius, radius, color);
        } else {
            drawRectangle(drawContext, x + width - radius, y + height - radius, radius, radius, color);
        }
    }


    public static void drawCircle(DrawContext drawContext, int xCenter, int yCenter, int radius, int color) {
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
            bufferBuilder.vertex(drawContext.getMatrices().peek().getPositionMatrix(), (float) x, (float) y, 0).color(red, green, blue, alpha).next();
        }

        tessellator.draw();
    }

    public static void drawFilledArc(DrawContext drawContext, float x, float y, float radius, float startAngle, float endAngle, int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;


        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        RenderSystem.enableBlend();

        Matrix4f model = drawContext.getMatrices().peek().getPositionMatrix();

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

    public static void drawFilledCircle(DrawContext drawContext, int xCenter, int yCenter, int radius, int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        RenderSystem.enableBlend();


        bufferBuilder.vertex(drawContext.getMatrices().peek().getPositionMatrix(), xCenter, yCenter, 0).color(red, green, blue, alpha).next();

        for (int i = 0; i <= 360; i++) {
            double x = xCenter + Math.sin(Math.toRadians(i)) * radius;
            double y = yCenter + Math.cos(Math.toRadians(i)) * radius;
            bufferBuilder.vertex(drawContext.getMatrices().peek().getPositionMatrix(), (float) x, (float) y, 0).color(red, green, blue, alpha).next();
        }

        tessellator.draw();
        RenderSystem.disableBlend();
    }

    public static void drawFilledQuadrant(DrawContext drawContext, int xCenter, int yCenter, int radius, int color, int quadrant) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        RenderSystem.enableBlend();

        bufferBuilder.vertex(drawContext.getMatrices().peek().getPositionMatrix(), xCenter, yCenter, 0).color(red, green, blue, alpha).next();

        for (int i = quadrant * 90; i <= quadrant * 90 + 90; i++) {
            double x = xCenter + Math.sin(Math.toRadians(i)) * radius;
            double y = yCenter + Math.cos(Math.toRadians(i)) * radius;
            bufferBuilder.vertex(drawContext.getMatrices().peek().getPositionMatrix(), (float) x, (float) y, 0).color(red, green, blue, alpha).next();
        }

        tessellator.draw();
        RenderSystem.disableBlend();

    }

    public static void drawTriangle(DrawContext drawContext, int x1, int y1, int x2, int y2, int x3, int y3, int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(drawContext.getMatrices().peek().getPositionMatrix(), x1, y1, 0).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(drawContext.getMatrices().peek().getPositionMatrix(), x2, y2, 0).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(drawContext.getMatrices().peek().getPositionMatrix(), x3, y3, 0).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(drawContext.getMatrices().peek().getPositionMatrix(), x1, y1, 0).color(red, green, blue, alpha).next();

        tessellator.draw();
    }

    public static void drawArc(DrawContext drawContext, int xCenter, int yCenter, int radius, double startAngle, double endAngle, int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        for (double i = startAngle; i <= endAngle; i++) {
            double x = xCenter + Math.sin(Math.toRadians(i)) * radius;
            double y = yCenter + Math.cos(Math.toRadians(i)) * radius;
            bufferBuilder.vertex(drawContext.getMatrices().peek().getPositionMatrix(), (float) x, (float) y, 0).color(red, green, blue, alpha).next();
        }

        tessellator.draw();
    }


    public static void drawQuadrant(DrawContext drawContext, int xCenter, int yCenter, int radius, int quadrant, int color) {
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

        drawArc(drawContext, xCenter, yCenter, radius, startAngle, endAngle, color);
    }

    public static void drawLine(DrawContext drawContext, int x1, int y1, int x2, int y2, int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        Matrix4f matrix4f = drawContext.getMatrices().peek().getPositionMatrix();
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

    public static void drawFilledTriangle(DrawContext drawContext, int x1, int y1, int x2, int y2, int x3, int y3, int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(drawContext.getMatrices().peek().getPositionMatrix(), x1, y1, 0).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(drawContext.getMatrices().peek().getPositionMatrix(), x2, y2, 0).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(drawContext.getMatrices().peek().getPositionMatrix(), x3, y3, 0).color(red, green, blue, alpha).next();

        tessellator.draw();
    }


    public static void drawOutlineBox(DrawContext drawContext, int x, int y, int width, int height, int thickness, int color) {
        drawRectangle(drawContext, x, y, width, thickness, color);
        drawRectangle(drawContext, x, y + height - thickness, width, thickness, color);
        drawRectangle(drawContext, x, y + thickness, thickness, height - thickness * 2, color);
        drawRectangle(drawContext, x + width - thickness, y + thickness, thickness, height - thickness * 2, color);
    }
}