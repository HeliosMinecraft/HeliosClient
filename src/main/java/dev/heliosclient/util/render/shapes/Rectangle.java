package dev.heliosclient.util.render.shapes;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.awt.*;

public class Rectangle extends Shape<Rectangle>{
    private float width, height, outlineThickness;
    private int v1Color, v2Color, v3Color, v4Color;
    private boolean outline = false;

    public Rectangle size(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }
    public Rectangle outline(boolean outline, float outlineThickness) {
        this.outline = outline;
        this.outlineThickness = outlineThickness;
        return this;
    }
    public Rectangle reset(){
        this.width = 0;
        this.height = 0;
        this.outline = false;
        this.outlineThickness = 0.0f;
        return color(-1);
    }

    public Rectangle color(Color color) {
        return color(color.getRGB());
    }

    public Rectangle color(int color) {
        this.v1Color = color;
        this.v2Color = color;
        this.v3Color = color;
        this.v4Color = color;
        return this;
    }

    public Rectangle color(Color startColor, Color endColor, Renderer2D.Direction direction) {
        return color(startColor.getRGB(), endColor.getRGB(), direction);
    }

    public Rectangle color(int startColor, int endColor, Renderer2D.Direction direction) {
        int[] array = direction.getVertexColor(startColor,endColor);
        return color(array[0],array[1],array[2],array[3]);
    }

    public Rectangle color(int tl, int tr, int br, int bl) {
        this.v1Color = tl;
        this.v2Color = tr;
        this.v3Color = br;
        this.v4Color = bl;
        return this;
    }

    @Override
    public void vertex(MatrixStack stack,BufferBuilder buffer) {
        Matrix4f matrix = stack.peek().getPositionMatrix();

        if(outline){
            this.fill(matrix, x, y, width, outlineThickness, v1Color, v2Color,v2Color,v1Color);
            this.fill(matrix, x, y + height - outlineThickness, width, outlineThickness, v4Color, v3Color,v3Color,v4Color);
            this.fill(matrix, x, y + outlineThickness, outlineThickness, height - outlineThickness, v1Color,v1Color,v4Color,v4Color);
            this.fill(matrix, x + width - outlineThickness, y + outlineThickness, outlineThickness, height - outlineThickness, v2Color,v2Color, v3Color,v3Color);
        } else {
            buffer.vertex(matrix, x, y + height, 0.0F).color(v4Color);
            buffer.vertex(matrix, x + width, y + height, 0.0F).color(v3Color);
            buffer.vertex(matrix, x + width, y, 0.0F).color(v2Color);
            buffer.vertex(matrix, x, y, 0.0F).color(v1Color);
        }
    }

    @Override
    public void draw(MatrixStack stack) {
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        vertex(stack,buffer);
        end();
    }

    private void fill(Matrix4f matrix,float x, float y,float width, float height, int v1Color, int v2Color, int v3Color, int v4Color){
        buffer.vertex(matrix, x, y + height, 0.0F).color(v4Color);
        buffer.vertex(matrix, x + width, y + height, 0.0F).color(v3Color);
        buffer.vertex(matrix, x + width, y, 0.0F).color(v2Color);
        buffer.vertex(matrix, x, y, 0.0F).color(v1Color);
    }
}
