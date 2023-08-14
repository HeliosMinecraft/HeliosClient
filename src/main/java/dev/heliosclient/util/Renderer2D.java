package dev.heliosclient.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;

public class Renderer2D {
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

        float f = (float)(color >> 24 & 255) / 255.0F;
        float g = (float)(color >> 16 & 255) / 255.0F;
        float h = (float)(color >> 8 & 255) / 255.0F;
        float j = (float)(color & 255) / 255.0F;
        Matrix4f matrix4f = drawContext.getMatrices().peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.defaultBlendFunc();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, (float)x1, (float)y2, 0.0F).color(g,h,j,f).next();
        bufferBuilder.vertex(matrix4f,(float)x2,(float)y2 ,0.0F).color(g,h,j,f).next();
        bufferBuilder.vertex(matrix4f,(float)x2,(float)y1 ,0.0F).color(g,h,j,f).next();
        bufferBuilder.vertex(matrix4f,(float)x1,(float)y1 ,0.0F).color(g,h,j,f).next();
        tessellator.draw();
        RenderSystem.disableBlend();
    }
}
