package dev.heliosclient.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
public class Renderer3D extends DrawContext {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public Renderer3D(MinecraftClient client, VertexConsumerProvider.Immediate vertexConsumers) {
        super(client, vertexConsumers);
    }

    public static void drawBlockOutline(DrawContext drawContext,BlockPos blockPos, float lineWidth, int color) {
            Camera camera = mc.gameRenderer.getCamera();
            Vec3d cameraPos = camera.getPos();
            float x = (float) (blockPos.getX() - cameraPos.x);
            float y = (float) (blockPos.getY() - cameraPos.y);
            float z = (float) (blockPos.getZ() - cameraPos.z);

            Matrix4f matrix4f = drawContext.getMatrices().peek().getPositionMatrix();
            RenderSystem.enableBlend();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            GL11.glLineWidth(lineWidth);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();

            float red = (float)(color >> 16 & 255) / 255.0F;
            float green = (float)(color >> 8 & 255) / 255.0F;
            float blue = (float)(color & 255) / 255.0F;
            float alpha = (float)(color >> 24 & 255) / 255.0F;

            bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(matrix4f,x, y, z).color(red, green, blue, alpha).next();
            bufferBuilder.vertex(matrix4f,x + 1.0F, y, z).color(red, green, blue, alpha).next();
            bufferBuilder.vertex(matrix4f,x + 1.0F, y, z).color(red, green, blue, alpha).next();
            bufferBuilder.vertex(matrix4f,x + 1.0F, y + 1.0F, z).color(red, green, blue, alpha).next();
            bufferBuilder.vertex(matrix4f,x + 1.0F, y + 1.0F, z).color(red, green, blue, alpha).next();
            bufferBuilder.vertex(matrix4f,x, y + 1.0F, z).color(red, green, blue, alpha).next();
            bufferBuilder.vertex(matrix4f,x, y + 1.0F, z).color(red, green, blue,alpha).next();
            bufferBuilder.vertex(matrix4f,x,y,z).color(red ,green ,blue ,alpha ).next ();

            bufferBuilder.vertex(matrix4f,x,y,z+1.0F).color(red ,green ,blue ,alpha ).next ();
            bufferBuilder.vertex(matrix4f,x+1.0F,y,z+1.0F).color(red ,green ,blue ,alpha ).next ();
            bufferBuilder.vertex(matrix4f,x+1.0F,y,z+1.0F).color(red ,green ,blue ,alpha ).next ();
            bufferBuilder.vertex(matrix4f,x+1.0F,y+1.0F,z+1.0F).color(red ,green ,blue ,alpha ).next ();
            bufferBuilder.vertex(matrix4f,x+1.0F,y+1.0F,z+1.0F).color(red ,green ,blue ,alpha ).next ();
            bufferBuilder.vertex(matrix4f,x,y+1.0F,z+1.0F).color(red ,green ,blue ,alpha ).next ();
            bufferBuilder.vertex(matrix4f,x,y+1.0F,z+1.0F).color(red ,green ,blue,alpha ).next ();
            bufferBuilder.vertex(matrix4f,x,y,z+1.0F).color(red ,green ,blue,alpha ).next ();

            //Vertical lines
            bufferBuilder.vertex(matrix4f,x,y,z).color(red ,green ,blue,alpha ).next ();
            bufferBuilder.vertex(matrix4f,x,y,z+1.0F).color(red ,green ,blue,alpha ).next ();
            bufferBuilder.vertex(matrix4f,x,y+1.0F,z).color(red ,green ,blue,alpha ).next ();
            bufferBuilder.vertex(matrix4f,x,y+1.0F,z+1.0F).color(red ,green ,blue,alpha ).next ();
            bufferBuilder.vertex(matrix4f,x+1.0F,y,z).color(red ,green ,blue,alpha ).next ();
            bufferBuilder.vertex(matrix4f,x+1.0F,y,z+1.0F).color(red ,green ,blue,alpha ).next ();
            bufferBuilder.vertex(matrix4f,x+1.0F,y+1.0F,z).color(red ,green ,blue,alpha ).next ();
            bufferBuilder.vertex(matrix4f,x+1.0F,y+1.0F,z+1.0F).color(red ,green ,blue,alpha ).next ();

            tessellator.draw();

            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }

        public static void drawTracerLine(Entity entity, float lineWidth, int color, DrawContext drawContext) {
            Camera camera = mc.gameRenderer.getCamera();
            Vec3d cameraPos = camera.getPos();
            Vec3d entityPos = entity.getPos();

            float x = (float) (entityPos.x - cameraPos.x);
            float y = (float) (entityPos.y + entity.getHeight() / 2.0F - cameraPos.y);
            float z = (float) (entityPos.z - cameraPos.z);

            Matrix4f matrix4f = drawContext.getMatrices().peek().getPositionMatrix();
            RenderSystem.enableBlend();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            GL11.glLineWidth(lineWidth);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();

            float red = (float)(color >> 16 & 255) / 255.0F;
            float green = (float)(color >> 8 & 255) / 255.0F;
            float blue = (float)(color & 255) / 255.0F;
            float alpha = (float)(color >> 24 & 255) / 255.0F;

            bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(matrix4f,0, 0, 0).color(red, green, blue, alpha).next();
            bufferBuilder.vertex(matrix4f,x, y, z).color(red, green, blue, alpha).next();

            tessellator.draw();

            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }

    public static void drawLineFromPlayer(DrawContext drawContext, Vec3d targetPos, int color, float thickness,float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        if (player == null) return;

        Vec3d playerPos = player.getPos();
        Vec3d playerCenter = new Vec3d(playerPos.x, playerPos.y + player.getEyeHeight(player.getPose()), playerPos.z);

        // Calculate the line start and end positions
        Vec3d start = playerCenter.subtract(player.getCameraPosVec(tickDelta));
        Vec3d end = targetPos.subtract(player.getCameraPosVec(tickDelta));

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        // Draw the line
        drawContext.getMatrices().push();
      //  drawContext.getMatrices().translate(start.x, start.y, start.z);
        RenderSystem.lineWidth(thickness);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(start.x, start.y, start.z)
                .color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, 1F)
                .next();
        bufferBuilder.vertex(end.x - start.x, end.y - start.y, end.z - start.z)
                .color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, 1F)
                .next();

        tessellator.draw();

        drawContext.getMatrices().pop();
    }

    public static void drawFilledBox(DrawContext drawContext,BlockPos blockPos, int color) {
            Camera camera=mc.gameRenderer.getCamera ();
            Vec3d cameraPos=camera.getPos ();

            double x=blockPos.getX ()-cameraPos.x;
            double y=blockPos.getY ()-cameraPos.y;
            double z=blockPos.getZ ()-cameraPos.z;

            MatrixStack matrixStack= drawContext.getMatrices();
            matrixStack.translate(x,y,z);
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);

            Matrix4f matrix4f=matrixStack.peek().getPositionMatrix();

            Tessellator tessellator=Tessellator.getInstance ();
            BufferBuilder bufferBuilder=tessellator.getBuffer ();

            float red=(float)(color>>16&255)/255.0F;
            float green=(float)(color>>8&255)/255.0F;
            float blue=(float)(color&255)/255.0F;
            float alpha=(float)(color>>24&255)/255.0F;

            bufferBuilder.begin(VertexFormat.DrawMode.QUADS ,VertexFormats.POSITION_COLOR);

            //Down
            bufferBuilder.vertex(matrix4f,1.0f,0.0f,1.0f).color(red ,green ,blue ,alpha ).next ();
            bufferBuilder.vertex(matrix4f,1.0f,0.0f,0.0f).color(red ,green ,blue ,alpha ).next ();
            bufferBuilder.vertex(matrix4f,0.0f,0.0f,1.0f).color(red ,green ,blue ,alpha ).next ();
            bufferBuilder.vertex(matrix4f, 0.0f, 0.0f, 0.0f).color(red, green, blue,alpha).next();

            // Up
            bufferBuilder.vertex(matrix4f, 1.0f, 1.0f, 1.0f).color(red ,green ,blue ,alpha ).next ();
            bufferBuilder.vertex(matrix4f ,1.0f ,1.0f ,0.0f ).color(red ,green ,blue ,alpha ).next ();
            bufferBuilder.vertex(matrix4f ,0.0f ,1.0f ,1.0f ).color(red ,green ,blue ,alpha ).next ();
            bufferBuilder.vertex(matrix4f ,0.0f ,1.0f ,0.0f ).color(red ,green ,blue,alpha ).next ();

            // North
            bufferBuilder.vertex(matrix4f, 1.0f, 1.0f, 1.0f).color(red, green, blue, alpha).next();
            bufferBuilder.vertex(matrix4f, 1.0f, 0.0f, 1.0f).color(red, green, blue, alpha).next();
            bufferBuilder.vertex(matrix4f, 0.0f, 0.0f, 1.0f).color(red, green, blue, alpha).next();
            bufferBuilder.vertex(matrix4f, 0.0f, 1.0f, 1.0f).color(red ,green ,blue ,alpha ).next ();

            //South
            bufferBuilder.vertex(matrix4f ,1.0f ,1.0f ,0.0f ).color(red ,green ,blue,alpha ).next ();
            bufferBuilder.vertex(matrix4f ,1.0f ,0.0f ,0.0f ).color(red ,green ,blue,alpha ).next ();
            bufferBuilder.vertex(matrix4f ,0.0f ,0.0f ,0.0f ).color(red ,green ,blue,alpha ).next ();
            bufferBuilder.vertex(matrix4f ,0.0f ,1.0f ,0.0f ).color(red ,green ,blue,alpha ).next ();

            // East
            bufferBuilder.vertex(matrix4f, 1.0f, 1.0f, 1.0f).color(red, green, blue, alpha).next();
            bufferBuilder.vertex(matrix4f, 1.0f, 0.0f, 1.0f).color(red, green, blue, alpha).next();
            bufferBuilder.vertex(matrix4f, 1.0f, 0.0f, 0.0f).color(red, green, blue,alpha).next();
            bufferBuilder.vertex(matrix4f ,1.0f ,1.0f ,0.0f ).color(red ,green ,blue,alpha ).next ();

            //West
            bufferBuilder.vertex(matrix4f ,0.0f ,1.0f ,1.0f ).color(red ,green ,blue,alpha ).next ();
            bufferBuilder.vertex(matrix4f ,0.0f ,0.0f ,1.0f ).color(red ,green ,blue,alpha ).next ();
            bufferBuilder.vertex(matrix4f ,0.0f ,0.0f ,0.0f ).color(red ,green ,blue,alpha ).next ();
            bufferBuilder.vertex(matrix4f ,0.0f ,1.0f ,0.0f ).color(red ,green ,blue,alpha ).next ();

            tessellator.draw();
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }
}
