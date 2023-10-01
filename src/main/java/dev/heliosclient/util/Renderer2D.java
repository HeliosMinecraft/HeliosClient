package dev.heliosclient.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.heliosclient.HeliosClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

public class Renderer2D extends DrawContext {

    public Renderer2D(MinecraftClient client, VertexConsumerProvider.Immediate vertexConsumers) {
        super(client, vertexConsumers);
    }

    public static void drawRectangle(DrawContext drawContext, float x, float y, float width, float height, int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        Matrix4f matrix4f = drawContext.getMatrices().peek().getPositionMatrix();
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



    public static void drawRoundedRectangle(DrawContext drawContext, float x, float y, float width, float height, int radius, int color) {
        // Draw the main rectangle
        drawRectangle(drawContext, x + radius, y + radius, width - 2 * radius, height - 2 * radius, color);

        // Draw rectangles at the sides
        drawRectangle(drawContext, x + radius, y, width - 2 * radius, radius, color); // top
        drawRectangle(drawContext, x + radius, y + height - radius, width - 2 * radius, radius, color); // bottom
        drawRectangle(drawContext, x, y + radius, radius, height - 2 * radius, color); // left
        drawRectangle(drawContext, x + width - radius, y + radius, radius, height - 2 * radius, color); // right

        // Draw circles at the corners
        drawFilledQuadrant(drawContext, x + radius, y + radius, radius, color,2);
        drawFilledQuadrant(drawContext, x + width - radius, y + radius, radius, color,1);
        drawFilledQuadrant(drawContext, x + radius, y + height - radius, radius, color,3);
        drawFilledQuadrant(drawContext, x + width - radius, y + height - radius, radius, color,4);
    }

    public static void drawRoundedRectangle(DrawContext drawContext, float x, float y, boolean TL, boolean TR, boolean BL, boolean BR, float width, float height, int radius, int color) {
        // Draw the main rectangle
        drawRectangle(drawContext, x + radius, y + radius, width - 2 * radius, height - 2 * radius, color);

        // Draw rectangles at the sides
        drawRectangle(drawContext, x + radius, y, width - 2 * radius, radius, color); // top
        drawRectangle(drawContext, x + radius, y + height - radius, width - 2 * radius, radius, color); // bottom
        drawRectangle(drawContext, x, y + radius, radius, height - 2 * radius, color); // left
        drawRectangle(drawContext, x + width - radius, y + radius, radius, height - 2 * radius, color); // right

        if (TL) {
            drawFilledQuadrant(drawContext, x + radius, y + radius, radius, color,2);
        } else {
            drawRectangle(drawContext, x, y, radius, radius, color);
        }
        if (TR) {
            drawFilledQuadrant(drawContext, x + width - radius, y + radius, radius, color,1);
        } else {
            drawRectangle(drawContext, x + width - radius, y, radius, radius, color);
        }
        if (BL) {
            drawFilledQuadrant(drawContext, x + radius, y + height - radius, radius, color,3);
        } else {
            drawRectangle(drawContext, x, y + height - radius, radius, radius, color);
        }
        if (BR) {
            drawFilledQuadrant(drawContext, x + width - radius, y + height - radius, radius, color,4);
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

    public static void drawFilledCircle(DrawContext drawContext, float xCenter, float yCenter, float radius, int color) {
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


        bufferBuilder.vertex(drawContext.getMatrices().peek().getPositionMatrix(), xCenter, yCenter, 0).color(red, green, blue, alpha).next();

        for (int i = 0; i <= 360; i++) {
            double x = xCenter + Math.sin(Math.toRadians(i)) * radius;
            double y = yCenter + Math.cos(Math.toRadians(i)) * radius;
            bufferBuilder.vertex(drawContext.getMatrices().peek().getPositionMatrix(), (float) x, (float) y, 0).color(red, green, blue, alpha).next();
        }

        tessellator.draw();
        RenderSystem.disableBlend();
    }
    public static void drawOutlineRoundedBox(DrawContext drawContext, float x, float y, float width, float height, float radius, float thickness, int color) {
        // Draw the rectangles for the outline
        drawRectangle(drawContext, x + radius, y, width - radius * 2, thickness, color); // Top rectangle
        drawRectangle(drawContext, x + radius, y + height - thickness, width - radius * 2, thickness, color); // Bottom rectangle
        drawRectangle(drawContext, x, y + radius, thickness, height - radius * 2, color); // Left rectangle
        drawRectangle(drawContext, x + width - thickness, y + radius, thickness, height - radius * 2, color); // Right rectangle

        // Draw the arcs at the corners for the outline
        drawArc(drawContext,x + radius,y + radius,radius ,thickness ,color ,180 ,270 ); // Top-left arc
        drawArc(drawContext,x + width - radius,y + radius,radius ,thickness ,color ,90 ,180 ); // Top-right arc
        drawArc(drawContext,x + width - radius,y + height - radius,radius ,thickness ,color ,0 ,90 ); // Bottom-right arc
        drawArc(drawContext,x + radius,y + height - radius,radius ,thickness ,color ,270 ,360 ); // Bottom-left arc
    }

    public static void drawArc(DrawContext drawContext,float xCenter,float yCenter,float radius,float thickness,int color,int startAngle,int endAngle ){
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

            bufferBuilder.vertex(drawContext.getMatrices().peek().getPositionMatrix(),(float)innerX ,(float)innerY ,0).color(red ,green ,blue ,alpha ).next();
            bufferBuilder.vertex(drawContext.getMatrices().peek().getPositionMatrix(),(float)outerX ,(float)outerY ,0).color(red ,green ,blue ,alpha ).next();
        }

        tessellator.draw();

        RenderSystem.disableBlend();
    }


    public static void drawFilledQuadrant(DrawContext drawContext, float xCenter, float yCenter, float radius, int color, int quadrant) {
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



    public static void drawQuadrant(DrawContext drawContext, float xCenter, float yCenter, float radius, int quadrant, int color) {
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

        drawArc(drawContext, xCenter, yCenter, radius,1f, color,startAngle, endAngle);
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


    public static void drawOutlineBox(DrawContext drawContext, float x, float y, float width, float height, float thickness, int color) {
        drawRectangle(drawContext, x, y, width, thickness, color);
        drawRectangle(drawContext, x, y + height - thickness, width, thickness, color);
        drawRectangle(drawContext, x, y + thickness, thickness, height - thickness * 2, color);
        drawRectangle(drawContext, x + width - thickness, y + thickness, thickness, height - thickness * 2, color);
    }
}