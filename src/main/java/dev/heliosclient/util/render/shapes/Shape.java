package dev.heliosclient.util.render.shapes;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.math.MatrixStack;

public abstract class Shape<T extends Shape<T>> {
    public float x,y;
    public BufferBuilder buffer = null;

    public Shape(){
    }

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    public T position(float x, float y) {
        this.x = x;
        this.y = y;
        return self();
    }

    protected abstract void draw(MatrixStack stack);
    public void vertex(MatrixStack stack, BufferBuilder buffer) {}

    protected void begin(VertexFormat.DrawMode drawMode, VertexFormat vertexFormat){
        buffer = Tessellator.getInstance().begin(drawMode,vertexFormat);
        RenderSystem.setShaderColor(1.0f,1.0f,1.0f,1.0f);
        RenderSystem.enableBlend();
    }

    protected void end(){
        drawBuffer();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f,1.0f,1.0f,1.0f);
    }

    protected void drawBuffer(){
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

}
