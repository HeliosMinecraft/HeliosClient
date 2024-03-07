package dev.heliosclient.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.render.color.LineColor;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/*
Credits: BleachHack 1.19.4

Todo: Replace this later with original code or not ig
 */
public class Renderer3D {
    public static boolean renderThroughWalls = false;
    public static MinecraftClient mc = MinecraftClient.getInstance();
    /**
     * Offsets this box so that minX, minY and minZ are all zero.
     **/
    public static Box moveToZero(Box box) {
        return box.offset(getMinVec(box).negate());
    }

    /**
     * Returns the vector of the min pos of this box.
     **/
    public static Vec3d getMinVec(Box box) {
        return new Vec3d(box.minX, box.minY, box.minZ);
    }
    // -------------------- Fill + Outline Boxes --------------------

    public static void drawBoxBoth(BlockPos blockPos, QuadColor color, float lineWidth, Direction... excludeDirs) {
        drawBoxBoth(new Box(blockPos), color, lineWidth, excludeDirs);
    }

    public static void drawBoxBoth(Box box, QuadColor color, float lineWidth, Direction... excludeDirs) {
        QuadColor outlineColor = color.clone();
        outlineColor.overwriteAlpha(255);

        drawBoxBoth(box, color, outlineColor, lineWidth, excludeDirs);
    }

    public static void drawBoxBoth(BlockPos blockPos, QuadColor fillColor, QuadColor outlineColor, float lineWidth, Direction... excludeDirs) {
        drawBoxBoth(new Box(blockPos), fillColor, outlineColor, lineWidth, excludeDirs);
    }

    public static void drawBoxBoth(Box box, QuadColor fillColor, QuadColor outlineColor, float lineWidth, Direction... excludeDirs) {
        drawBoxFill(box, fillColor, excludeDirs);
        drawBoxOutline(box, outlineColor, lineWidth, excludeDirs);
    }

    // -------------------- Fill Boxes --------------------

    public static void drawBoxFill(BlockPos blockPos, QuadColor color, Direction... excludeDirs) {
        drawBoxFill(new Box(blockPos), color, excludeDirs);
    }

    public static void drawBoxFill(Box box, QuadColor color, Direction... excludeDirs) {
        if (!FrustumUtils.isBoxVisible(box)) {
            return;
        }

        setup();

        MatrixStack matrices = matrixFrom(box.minX, box.minY, box.minZ);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // Fill
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Vertexer.vertexBoxQuads(matrices, buffer, moveToZero(box), color, excludeDirs);
        tessellator.draw();

        cleanup();
    }

    // -------------------- Outline Boxes --------------------

    public static void drawBoxOutline(BlockPos blockPos, QuadColor color, float lineWidth, Direction... excludeDirs) {
        drawBoxOutline(new Box(blockPos), color, lineWidth, excludeDirs);
    }

    public static void drawBoxOutline(Box box, QuadColor color, float lineWidth, Direction... excludeDirs) {
        if (!FrustumUtils.isBoxVisible(box)) {
            return;
        }

        setup();

        MatrixStack matrices = matrixFrom(box.minX, box.minY, box.minZ);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // Outline
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(lineWidth);

        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        Vertexer.vertexBoxLines(matrices, buffer, moveToZero(box), color, excludeDirs);
        tessellator.draw();

        RenderSystem.enableCull();

        cleanup();
    }

    // -------------------- Quads --------------------

    public static void drawQuadFill(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, int cullMode, QuadColor color) {
        if (!FrustumUtils.isPointVisible(x1, y1, z1) && !FrustumUtils.isPointVisible(x2, y2, z2)
                && !FrustumUtils.isPointVisible(x3, y3, z3) && !FrustumUtils.isPointVisible(x4, y4, z4)) {
            return;
        }

        setup();

        MatrixStack matrices = matrixFrom(x1, y1, z1);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // Fill
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Vertexer.vertexQuad(matrices, buffer,
                0f, 0f, 0f,
                (float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1),
                (float) (x3 - x1), (float) (y3 - y1), (float) (z3 - z1),
                (float) (x4 - x1), (float) (y4 - y1), (float) (z4 - z1),
                cullMode, color);
        tessellator.draw();

        cleanup();
    }

    public static void drawQuadOutline(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, float lineWidth, QuadColor color) {
        if (!FrustumUtils.isPointVisible(x1, y1, z1) && !FrustumUtils.isPointVisible(x2, y2, z2)
                && !FrustumUtils.isPointVisible(x3, y3, z3) && !FrustumUtils.isPointVisible(x4, y4, z4)) {
            return;
        }

        setup();

        MatrixStack matrices = matrixFrom(x1, y1, z1);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        int[] colors = color.getAllColors();

        // Outline
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(lineWidth);

        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        Vertexer.vertexLine(matrices, buffer, 0f, 0f, 0f, (float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1), LineColor.gradient(colors[0], colors[1]));
        Vertexer.vertexLine(matrices, buffer, (float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1), (float) (x3 - x1), (float) (y3 - y1), (float) (z3 - z1), LineColor.gradient(colors[1], colors[2]));
        Vertexer.vertexLine(matrices, buffer, (float) (x3 - x1), (float) (y3 - y1), (float) (z3 - z1), (float) (x4 - x1), (float) (y4 - y1), (float) (z4 - z1), LineColor.gradient(colors[2], colors[3]));
        Vertexer.vertexLine(matrices, buffer, (float) (x4 - x1), (float) (y4 - y1), (float) (z4 - z1), 0f, 0f, 0f, LineColor.gradient(colors[3], colors[0]));
        tessellator.draw();

        RenderSystem.enableCull();
        cleanup();
    }

    // -------------------- Lines --------------------

    public static void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, LineColor color, float width) {
        if (!FrustumUtils.isPointVisible(x1, y1, z1) && !FrustumUtils.isPointVisible(x2, y2, z2)) {
            return;
        }

        setup();

        MatrixStack matrices = matrixFrom(x1, y1, z1);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // Line
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(width);

        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        Vertexer.vertexLine(matrices, buffer, 0f, 0f, 0f, (float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1), color);
        tessellator.draw();

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        cleanup();
    }

    public static void drawLine(Vec3d start, Vec3d end, LineColor color, float width) {
        if (!FrustumUtils.isPointVisible(start.x, start.y, start.z) && !FrustumUtils.isPointVisible(end.x, end.y, end.z)) {
            return;
        }

        setup();

        MatrixStack matrices = matrixFrom(start.x, start.y, start.z);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // Line
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(width);

        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        Vertexer.vertexLine(matrices, buffer, 0f, 0f, 0f, (float) (end.x - start.x), (float) (end.y - start.y), (float) (end.z - start.z), color);
        tessellator.draw();

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        cleanup();
    }

    public static void drawText(Text text, double x, double y, double z, double scale, boolean shadow) {
        drawText(text, x, y, z, 0, 0, scale, shadow);
    }

    /**
     * Draws text in the world.
     **/
    public static void drawText(Text text, double x, double y, double z, double offX, double offY, double scale, boolean fill) {
        MatrixStack matrices = matrixFrom(x, y, z);

        Camera camera = mc.gameRenderer.getCamera();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        matrices.translate(offX, offY, 0);
        matrices.scale(-0.025f * (float) scale, -0.025f * (float) scale, 1);

        int halfWidth = mc.textRenderer.getWidth(text) / 2;

        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());

        if (fill) {
            int opacity = (int) (MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F) * 255.0F) << 24;
            mc.textRenderer.draw(text, -halfWidth, 0f, 553648127, false, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.NORMAL, opacity, 0xf000f0);
            immediate.draw();
        } else {
            matrices.push();
            matrices.translate(1, 1, 0);
            mc.textRenderer.draw(text.copy(), -halfWidth, 0f, 0x202020, false, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.NORMAL, 0, 0xf000f0);
            immediate.draw();
            matrices.pop();
        }

        mc.textRenderer.draw(text, -halfWidth, 0f, -1, false, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.NORMAL, 0, 0xf000f0);
        immediate.draw();

        RenderSystem.disableBlend();
    }

    public static void renderItem(ItemStack itemStack, Vec3d position) {
        if (HeliosClient.MC.world == null) return;


        MatrixStack matrices = matrixFrom(position.x, position.y, position.z);

        matrices.translate(position.x, position.y, position.z);
        matrices.scale(0.5F, 0.5F, 0.5F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        DiffuseLighting.disableGuiDepthLighting();


        MinecraftClient.getInstance().getItemRenderer().renderItem(itemStack, ModelTransformationMode.GROUND, 0xF000F0, OverlayTexture.DEFAULT_UV, matrices, mc.getBufferBuilders().getEntityVertexConsumers(), HeliosClient.MC.world, 0);

        DiffuseLighting.enableGuiDepthLighting();
        mc.getBufferBuilders().getEntityVertexConsumers().draw();

        RenderSystem.disableBlend();
    }
    public static void drawCircleAroundEntity(MatrixStack matrix, Entity entity, float radius, int color, int points) {
        setup();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

        double x = entity.prevX + (entity.getX() - entity.prevX) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = entity.prevY + (entity.getY() - entity.prevY) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY();
        double z = entity.prevZ + (entity.getZ() - entity.prevZ) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
        matrix.push();
        matrix.translate(x, y, z);

        Matrix4f matrix4 = matrix.peek().getPositionMatrix();
        for (int i = 0; i <= points; i++) {
            double angle = 2 * Math.PI * i / points;
            float xCoord = (float) (radius * Math.cos(angle));
            float zCoord = (float) (radius * Math.sin(angle));

            bufferBuilder.vertex(matrix4, xCoord, 0.0f, zCoord)
                    .color(color)
                    .next();
        }

        tessellator.draw();
        cleanup();
        matrix.translate(-x, -y, -z);
        matrix.pop();
    }

    // -------------------- Utils --------------------

    public static MatrixStack matrixFrom(double x, double y, double z) {
        MatrixStack matrices = new MatrixStack();

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

        matrices.translate(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z);

        return matrices;
    }

    public static Vec3d getInterpolationOffset(Entity e) {
        if (MinecraftClient.getInstance().isPaused()) {
            return Vec3d.ZERO;
        }

        double tickDelta = MinecraftClient.getInstance().getTickDelta();
        return new Vec3d(
                e.getX() - MathHelper.lerp(tickDelta, e.lastRenderX, e.getX()),
                e.getY() - MathHelper.lerp(tickDelta, e.lastRenderY, e.getY()),
                e.getZ() - MathHelper.lerp(tickDelta, e.lastRenderZ, e.getZ()));
    }

    public static void setup() {
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(renderThroughWalls ? GL11.GL_ALWAYS : GL11.GL_LEQUAL);
    }
    public static void renderThroughWalls(){
        renderThroughWalls = true;
    }
    public static void stopRenderingThroughWalls(){
        renderThroughWalls = false;
    }

    public static void cleanup() {
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static Vec3d getEyeTracer() {
        return new Vec3d(0, 0, 75)
                .rotateX(-(float) Math.toRadians(HeliosClient.MC.gameRenderer.getCamera().getPitch()))
                .rotateY(-(float) Math.toRadians(HeliosClient.MC.gameRenderer.getCamera().getYaw()))
                .add(HeliosClient.MC.cameraEntity.getEyePos());
    }

    /**
     * Creates a Box object from the coordinates of two points in 3D space.
     *
     * @param x1, y1, z1 The coordinates of the first point.
     * @param x2, y2, z2 The coordinates of the second point.
     * @return A Box object representing the box bounded by the two points.
     */
    public static Box coordinatesToBox(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new Box(x1, y1, z1, x2, y2, z2);
    }

    /**
     * Creates a Box object from a BlockPos object.
     *
     * @param blockPos The BlockPos object.
     * @return A Box object with the same coordinates as the BlockPos object.
     */
    public static Box blockPosToBox(BlockPos blockPos) {
        return new Box(blockPos);
    }

    /**
     * Creates a Box object from two Vec3d objects.
     *
     * @param vec1, vec2 The Vec3d objects.
     * @return A Box object representing the box bounded by the two Vec3d objects.
     */
    public static Box vec3dToBox(Vec3d vec1, Vec3d vec2) {
        return new Box(vec1, vec2);
    }

    /**
     * Creates a Box object from a Vec3d object and a size parameter.
     *
     * @param vec  The Vec3d object representing the center of the box.
     * @param size The size of the box.
     * @return A Box object representing a box centered at the Vec3d object with the given size.
     */
    public static Box vec3dToBox(Vec3d vec, double size) {
        double halfSize = size / 2.0;
        return new Box(
                vec.x - halfSize, vec.y - halfSize, vec.z - halfSize,
                vec.x + halfSize, vec.y + halfSize, vec.z + halfSize
        );
    }
}
