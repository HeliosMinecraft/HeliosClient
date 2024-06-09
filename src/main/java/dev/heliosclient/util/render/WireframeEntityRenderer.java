package dev.heliosclient.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.render.CrystalESP;
import dev.heliosclient.util.render.color.LineColor;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import static dev.heliosclient.util.render.Renderer3D.cleanup;
import static dev.heliosclient.util.render.Renderer3D.matrixFrom;


//Inspired from MeteorClient WireframeEntityRenderer. :D (Couldn't work out the vertices for rendering)
public class WireframeEntityRenderer {
    private static final MatrixStack stack = new MatrixStack();
    //Quad vertices
    private static final Vector4f pos1 = new Vector4f();
    private static final Vector4f pos2 = new Vector4f();
    private static final Vector4f pos3 = new Vector4f();
    private static final Vector4f pos4 = new Vector4f();
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static MatrixStack skeleton_stack = new MatrixStack();
    //Entity pos
    private static Vec3d pos;
    private static LineColor lineColor;
    private static QuadColor sideColor;
    private static float lineWidth;
    private static boolean lines, sides;
    private static boolean skeleton_player;

    public static void render(Entity entity, double scale, QuadColor sideColor, LineColor lineColor, float lineWidth, boolean sides, boolean lines, boolean skeleton_player) {
        WireframeEntityRenderer.sideColor = sideColor;
        WireframeEntityRenderer.lineColor = lineColor;
        WireframeEntityRenderer.lineWidth = lineWidth;
        WireframeEntityRenderer.sides = sides;
        WireframeEntityRenderer.lines = lines;
        WireframeEntityRenderer.skeleton_player = skeleton_player;

        pos = Renderer3D.getInterpolatedPosition(entity);

        stack.push();
        stack.scale((float) scale, (float) scale, (float) scale);

        EntityRenderer<?> entityRenderer = mc.getEntityRenderDispatcher().getRenderer(entity);

        // LivingEntityRenderer
        if (entityRenderer instanceof LivingEntityRenderer renderer) {
            LivingEntity livingEntity = (LivingEntity) entity;
            EntityModel model = renderer.getModel();

            // PlayerEntityRenderer
            if (entityRenderer instanceof PlayerEntityRenderer r) {
                PlayerEntityModel<AbstractClientPlayerEntity> playerModel = r.getModel();

                playerModel.sneaking = entity.isInSneakingPose();
                BipedEntityModel.ArmPose mainHandPose = PlayerEntityRenderer.getArmPose((AbstractClientPlayerEntity) entity, Hand.MAIN_HAND);
                BipedEntityModel.ArmPose offHandPos = PlayerEntityRenderer.getArmPose((AbstractClientPlayerEntity) entity, Hand.OFF_HAND);

                if (mainHandPose.isTwoHanded())
                    offHandPos = livingEntity.getOffHandStack().isEmpty() ? BipedEntityModel.ArmPose.EMPTY : BipedEntityModel.ArmPose.ITEM;

                if (livingEntity.getMainArm() == Arm.RIGHT) {
                    playerModel.rightArmPose = mainHandPose;
                    playerModel.leftArmPose = offHandPos;
                } else {
                    playerModel.rightArmPose = offHandPos;
                    playerModel.leftArmPose = mainHandPose;
                }
            }

            model.handSwingProgress = livingEntity.getHandSwingProgress(mc.getTickDelta());
            model.riding = livingEntity.hasVehicle();
            model.child = livingEntity.isBaby();

            float bodyYaw = MathHelper.lerpAngleDegrees(mc.getTickDelta(), livingEntity.prevBodyYaw, livingEntity.bodyYaw);
            float yaw = bodyYaw - livingEntity.getYaw(mc.getTickDelta());


            float pitch = livingEntity.getPitch(mc.getTickDelta());

            float animationProgress = renderer.getAnimationProgress(livingEntity, mc.getTickDelta());
            float limbDistance = 0;
            float limbAngle = 0;

            //limbs position and rotation
            if (!livingEntity.hasVehicle() && livingEntity.isAlive()) {
                limbDistance = livingEntity.limbAnimator.getSpeed(mc.getTickDelta());
                limbAngle = livingEntity.limbAnimator.getPos(mc.getTickDelta());

                if (limbDistance > 1) limbDistance = 1;
            }

            //Simulate model animation and angles to apply to the entity model Parts
            model.animateModel(livingEntity, limbAngle, limbDistance, mc.getTickDelta());
            model.setAngles(livingEntity, limbAngle, limbDistance, animationProgress, -yaw, pitch);

            //See LivingEntityRenderer#render
            renderer.setupTransforms(livingEntity, stack, animationProgress, bodyYaw, mc.getTickDelta());
            stack.scale(-1, -1, 1);
            renderer.scale(livingEntity, stack, mc.getTickDelta());
            stack.translate(0, -1.501F, 0);

            if (model instanceof AnimalModel m) {
                //Player / any other biped entity wireframe rendering.
                if (model instanceof BipedEntityModel mo) {
                    if (entity instanceof PlayerEntity && WireframeEntityRenderer.skeleton_player) {
                        Renderer3D.renderThroughWalls();
                        float lerpBody = MathHelper.lerpAngleDegrees(mc.getTickDelta(), livingEntity.prevBodyYaw, livingEntity.bodyYaw);

                        BufferBuilder buffer = prepare(lineWidth);

                        Vec3d entityPos = Renderer3D.getInterpolatedPosition(livingEntity);
                        boolean sneaking = livingEntity.isSneaking();
                        boolean swimming = livingEntity.isInSwimmingPose();

                        //Translate to entity pos
                        skeleton_stack = matrixFrom(entityPos.x, entityPos.y, entityPos.z);

                        //Body yaw rotation
                        skeleton_stack.multiply(RotationAxis.NEGATIVE_Y.rotation((float) ((lerpBody + 180) * Math.PI / 180.0F)));
                        if (swimming) skeleton_stack.translate(0, 0.35F, 0);
                        if (swimming || livingEntity.isFallFlying())
                            skeleton_stack.multiply(RotationAxis.NEGATIVE_X.rotation((float) ((90 + pitch) * Math.PI / 180.0F)));
                        if (swimming) skeleton_stack.translate(0, -0.95F, 0);


                        //Spine
                        Vertexer.vertexLine(skeleton_stack, buffer, 0, sneaking ? 0.6F : 0.7F, sneaking ? 0.23F : 0, 0, sneaking ? 1.05F : 1.4F, 0, lineColor);

                        //Shoulders
                        Vertexer.vertexLine(skeleton_stack, buffer, -0.37F, sneaking ? 1.05F : 1.35F, 0, 0.37F, sneaking ? 1.05F : 1.35F, 0, lineColor);

                        //Waist
                        Vertexer.vertexLine(skeleton_stack, buffer, -0.15F, sneaking ? 0.6F : 0.7F, sneaking ? 0.23F : 0, 0.15F, sneaking ? 0.6F : 0.7F, sneaking ? 0.23F : 0, lineColor);

                        // Head
                        skeleton_stack.push();
                        skeleton_stack.translate(0, sneaking ? 1.05F : 1.4F, 0);
                        rotateStack(skeleton_stack, mo.head);
                        Vertexer.vertexLine(skeleton_stack, buffer, 0, 0, 0, 0, 0.15F, 0, lineColor);
                        skeleton_stack.pop();

                        // Right Leg

                        skeleton_stack.push();
                        skeleton_stack.translate(0.15F, sneaking ? 0.6F : 0.7F, sneaking ? 0.23F : 0);
                        rotateStack(skeleton_stack, mo.rightLeg);
                        Vertexer.vertexLine(skeleton_stack, buffer, 0, 0, 0, 0, -0.6F, 0, lineColor);
                        skeleton_stack.pop();

                        // Left Leg

                        skeleton_stack.push();
                        skeleton_stack.translate(-0.15F, sneaking ? 0.6F : 0.7F, sneaking ? 0.23F : 0);
                        rotateStack(skeleton_stack, mo.leftLeg);
                        Vertexer.vertexLine(skeleton_stack, buffer, 0, 0, 0, 0, -0.6F, 0, lineColor);
                        skeleton_stack.pop();

                        // Right Arm

                        skeleton_stack.push();
                        skeleton_stack.translate(0.37F, sneaking ? 1.05F : 1.35F, 0);
                        rotateStack(skeleton_stack, mo.rightArm);
                        Vertexer.vertexLine(skeleton_stack, buffer, 0, 0, 0, 0, -0.55F, 0, lineColor);
                        skeleton_stack.pop();

                        // Left Arm

                        skeleton_stack.push();
                        skeleton_stack.translate(-0.37F, sneaking ? 1.05F : 1.35F, 0);
                        rotateStack(skeleton_stack, mo.leftArm);
                        Vertexer.vertexLine(skeleton_stack, buffer, 0, 0, 0, 0, -0.55F, 0, lineColor);
                        skeleton_stack.pop();

                        //End buffer
                        end();


                        Renderer3D.stopRenderingThroughWalls();
                    } else {
                        renderModelPart(mo.head);
                        renderModelPart(mo.body);
                        renderModelPart(mo.leftArm);
                        renderModelPart(mo.rightArm);
                        renderModelPart(mo.leftLeg);
                        renderModelPart(mo.rightLeg);
                    }
                }
            } else if (model instanceof SinglePartEntityModel m) {
                renderModelPart(m.getPart());
            } else if (model instanceof CompositeEntityModel m) {
                m.getParts().forEach(modelPart -> renderModelPart((ModelPart) modelPart));
            }
        }

        //Minecraft code modified, see EndCrystalEntityRenderer#render

        if (entityRenderer instanceof EndCrystalEntityRenderer renderer) {
            EndCrystalEntity crystalEntity = (EndCrystalEntity) entity;
            CrystalESP esp = ModuleManager.get(CrystalESP.class);

            stack.push();
            float h = EndCrystalEntityRenderer.getYOffset(crystalEntity, mc.getTickDelta());


            float j = ((float) crystalEntity.endCrystalAge + mc.getTickDelta()) * 3.0F;
            stack.push();
            if (esp.isActive())
                stack.scale((float) (2.0F * esp.scaleOutside.value), (float) (2.0F * esp.scaleOutside.value), (float) (2.0F * esp.scaleOutside.value));
            else stack.scale(2.0F, 2.0F, 2.0F);
            stack.translate(0.0D, -0.5D, 0.0D);
            if (crystalEntity.shouldShowBottom()) {
                renderModelPart(renderer.bottom);
            }

            stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
            stack.translate(0.0D, 1.5F + h / 2.0F, 0.0D);
            stack.multiply(new Quaternionf().setAngleAxis(60.0F, EndCrystalEntityRenderer.SINE_45_DEGREES, 0.0F, EndCrystalEntityRenderer.SINE_45_DEGREES));
            if (!esp.isActive() || esp.renderFrameOutSide.value) {
                renderModelPart(renderer.frame);
            }
            if (esp.isActive())
                stack.scale((float) esp.scaleInside.value, (float) esp.scaleInside.value, (float) esp.scaleInside.value);
            stack.scale(0.875F, 0.875F, 0.875F);
            stack.multiply(new Quaternionf().setAngleAxis(60.0F, EndCrystalEntityRenderer.SINE_45_DEGREES, 0.0F, EndCrystalEntityRenderer.SINE_45_DEGREES));
            stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
            if (!esp.isActive() || esp.renderFrameInside.value) {
                renderModelPart(renderer.frame);
            }
            stack.scale(0.875F, 0.875F, 0.875F);
            stack.multiply(new Quaternionf().setAngleAxis(60.0F, EndCrystalEntityRenderer.SINE_45_DEGREES, 0.0F, EndCrystalEntityRenderer.SINE_45_DEGREES));
            stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
            if (!esp.isActive() || esp.renderCore.value) {
                renderModelPart(renderer.core);
            }
            stack.pop();
            stack.pop();
        }
        stack.pop();
    }

    private static void renderModelPart(ModelPart part) {
        if (!part.visible || (part.cuboids.isEmpty() && part.children.isEmpty()))
            return;

        stack.push();
        part.rotate(stack);
        for (ModelPart.Cuboid cuboid : part.cuboids) {
            Matrix4f matrix = stack.peek().getPositionMatrix();

            for (ModelPart.Quad quad : cuboid.sides) {

                //Vertices transform position
                pos1.set(quad.vertices[0].pos.x / 16, quad.vertices[0].pos.y / 16, quad.vertices[0].pos.z / 16, 1);
                pos1.mul(matrix);

                pos2.set(quad.vertices[1].pos.x / 16, quad.vertices[1].pos.y / 16, quad.vertices[1].pos.z / 16, 1);
                pos2.mul(matrix);

                pos3.set(quad.vertices[2].pos.x / 16, quad.vertices[2].pos.y / 16, quad.vertices[2].pos.z / 16, 1);
                pos3.mul(matrix);

                pos4.set(quad.vertices[3].pos.x / 16, quad.vertices[3].pos.y / 16, quad.vertices[3].pos.z / 16, 1);
                pos4.mul(matrix);

                // Render
                if (sides) {
                    Renderer3D.drawQuadFill(
                            pos.x + pos1.x, pos.y + pos1.y, pos.z + pos1.z,
                            pos.x + pos2.x, pos.y + pos2.y, pos.z + pos2.z,
                            pos.x + pos3.x, pos.y + pos3.y, pos.z + pos3.z,
                            pos.x + pos4.x, pos.y + pos4.y, pos.z + pos4.z,
                            1,
                            sideColor
                    );
                }

                if (lines) {
                    BufferBuilder buffer = prepare(lineWidth);

                    drawLineInternal(buffer, pos.x + pos1.x, pos.y + pos1.y, pos.z + pos1.z, pos.x + pos2.x, pos.y + pos2.y, pos.z + pos2.z, lineColor);
                    drawLineInternal(buffer, pos.x + pos2.x, pos.y + pos2.y, pos.z + pos2.z, pos.x + pos3.x, pos.y + pos3.y, pos.z + pos3.z, lineColor);
                    drawLineInternal(buffer, pos.x + pos3.x, pos.y + pos3.y, pos.z + pos3.z, pos.x + pos4.x, pos.y + pos4.y, pos.z + pos4.z, lineColor);
                    drawLineInternal(buffer, pos.x + pos1.x, pos.y + pos1.y, pos.z + pos1.z, pos.x + pos1.x, pos.y + pos1.y, pos.z + pos1.z, lineColor);

                    end();
                }
            }
        }

        for (ModelPart child : part.children.values()) renderModelPart(child);

        stack.pop();
    }

    private static void rotateStack(MatrixStack matrix, ModelPart modelPart) {
        if (modelPart.yaw != 0.0F) {
            matrix.multiply(RotationAxis.POSITIVE_Y.rotation(-modelPart.yaw));
        }

        if (modelPart.roll != 0.0F) {
            matrix.multiply(RotationAxis.POSITIVE_Z.rotation(modelPart.roll));
        }

        if (modelPart.pitch != 0.0F) {
            matrix.multiply(RotationAxis.POSITIVE_X.rotation(-modelPart.pitch));
        }
    }

    private static BufferBuilder prepare(float width) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // Line
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(width);

        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

        return buffer;
    }

    private static void end() {
        Tessellator tessellator = Tessellator.getInstance();

        tessellator.draw();

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        cleanup();
    }

    private static void drawLineInternal(BufferBuilder buffer, double x1, double y1, double z1, double x2, double y2, double z2, LineColor color) {
        if (!FrustumUtils.isPointVisible(x1, y1, z1) && !FrustumUtils.isPointVisible(x2, y2, z2)) {
            return;
        }

        MatrixStack matrices = matrixFrom(x1, y1, z1);
        Vertexer.vertexLine(matrices, buffer, 0f, 0f, 0f, (float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1), color);
    }

}
