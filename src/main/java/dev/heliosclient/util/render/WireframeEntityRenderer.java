package dev.heliosclient.util.render;

import dev.heliosclient.util.render.color.LineColor;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;


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
    private static boolean player_skeleton;

    public static void render(Entity entity, double scale, QuadColor sideColor, LineColor lineColor, float lineWidth, boolean sides, boolean lines, boolean player_skeleton) {
        WireframeEntityRenderer.sideColor = sideColor;
        WireframeEntityRenderer.lineColor = lineColor;
        WireframeEntityRenderer.lineWidth = lineWidth;
        WireframeEntityRenderer.sides = sides;
        WireframeEntityRenderer.lines = lines;
        WireframeEntityRenderer.player_skeleton = player_skeleton;

        float tickDelta = mc.getRenderTickCounter().getTickDelta(false);

        pos = Renderer3D.getInterpolatedPosition(entity);
        var entityRenderer = (EntityRenderer<Entity, EntityRenderState>) mc.getEntityRenderDispatcher().getRenderer(entity);
        var state = entityRenderer.getAndUpdateRenderState(entity,tickDelta);

        stack.push();
        stack.scale((float) scale, (float) scale, (float) scale);
        entityRenderer.render(state,stack,WireframeVertexConsumerProvider.INSTANCE,-1);
        stack.pop();
    }
    public static class WireframeVertexConsumerProvider implements VertexConsumerProvider {
        static WireframeVertexConsumerProvider INSTANCE = new WireframeVertexConsumerProvider();
        @Override
        public VertexConsumer getBuffer(RenderLayer layer) {
            return WireframeVertexConsumer.INSTANCE;
        }
    }
    public static class WireframeVertexConsumer implements VertexConsumer {
        static WireframeVertexConsumer INSTANCE = new WireframeVertexConsumer();
        private final List<Vector3f> vertices = new ArrayList<>();

        @Override
        public VertexConsumer vertex(float x, float y, float z) {
            vertices.add(new Vector3f(x,y,z));
            if(vertices.size() == 4){
                float x1 = vertices.get(0).x;
                float y1 = vertices.get(0).y;
                float z1 = vertices.get(0).z;

                float x2 = vertices.get(1).x;
                float y2 = vertices.get(1).y;
                float z2 = vertices.get(1).z;

                float x3 = vertices.get(2).x;
                float y3 = vertices.get(2).y;
                float z3 = vertices.get(2).z;

                float x4 = vertices.get(3).x;
                float y4 = vertices.get(3).y;
                float z4 = vertices.get(3).z;

                Vertexer.vertexLine(stack,this,x1,y1,z1,x2,y2,z2,lineColor);
                Vertexer.vertexLine(stack,this,x2,y2,z2,x3,y3,z3,lineColor);
                Vertexer.vertexLine(stack,this,x3,y3,z3,x4,y4,z4,lineColor);
                Vertexer.vertexLine(stack,this,x4,y4,z4,x1,y1,z1,lineColor);

                vertices.clear();
            }
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer texture(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer overlay(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer light(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            return this;
        }

    }

    /*
    public static void render(Entity entity, double scale, QuadColor sideColor, LineColor lineColor, float lineWidth, boolean sides, boolean lines, boolean player_skeleton) {
        WireframeEntityRenderer.sideColor = sideColor;
        WireframeEntityRenderer.lineColor = lineColor;
        WireframeEntityRenderer.lineWidth = lineWidth;
        WireframeEntityRenderer.sides = sides;
        WireframeEntityRenderer.lines = lines;
        WireframeEntityRenderer.player_skeleton = player_skeleton;

        float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
        
        pos = Renderer3D.getInterpolatedPosition(entity);

        stack.push();
        stack.scale((float) scale, (float) scale, (float) scale);

        EntityRenderer<?,?> entityRenderer = mc.getEntityRenderDispatcher().getRenderer(entity);
        

        // LivingEntityRenderer
        if (entityRenderer instanceof LivingEntityRenderer<?,?,?> renderer) {
            LivingEntity livingEntity = (LivingEntity) entity;
            EntityModel<?> model = renderer.getModel();
            EntityRenderState renderState = renderer.createRenderState();
            renderer.updateRenderState(r,renderState,tickDelta);
            renderer.getAndUpdateRenderState(entity,tickDelta);

            // PlayerEntityRenderer
            if (entityRenderer instanceof PlayerEntityRenderer r) {
                PlayerEntityModel playerModel = r.getModel();

                renderState.sneaking = entity.isInSneakingPose();
                BipedEntityModel.ArmPose mainHandPose = PlayerEntityRenderer.getArmPose((AbstractClientPlayerEntity) entity, Hand.MAIN_HAND);
                BipedEntityModel.ArmPose offHandPos = PlayerEntityRenderer.getArmPose((AbstractClientPlayerEntity) entity, Hand.OFF_HAND);

                if (mainHandPose.isTwoHanded())
                    offHandPos = livingEntity.getOffHandStack().isEmpty() ? BipedEntityModel.ArmPose.EMPTY : BipedEntityModel.ArmPose.ITEM;

                if (livingEntity.getMainArm() == Arm.RIGHT) {

                    mainHandPose;
                    playerModel.leftArmPose = offHandPos;
                } else {
                    playerModel.rightArmPose = offHandPos;
                    playerModel.leftArmPose = mainHandPose;
                }
            }

            model.handSwingProgress = livingEntity.getHandSwingProgress(tickDelta);
            model.riding = livingEntity.hasVehicle();
            model.child = livingEntity.isBaby();

            float bodyYaw = MathHelper.lerpAngleDegrees(tickDelta, livingEntity.prevBodyYaw, livingEntity.bodyYaw);
            float yaw = bodyYaw - livingEntity.getYaw(tickDelta);


            float pitch = livingEntity.getPitch(tickDelta);

            float animationProgress = renderer.getAnimationProgress(livingEntity, tickDelta);
            float limbDistance = 0;
            float limbAngle = 0;

            //limbs position and rotation
            if (!livingEntity.hasVehicle() && livingEntity.isAlive()) {
                limbDistance = livingEntity.limbAnimator.getSpeed(tickDelta);
                limbAngle = livingEntity.limbAnimator.getPos(tickDelta);

                if (limbDistance > 1) limbDistance = 1;
            }

            //Simulate model animation and angles to apply to the entity model Parts
            model.animateModel(livingEntity, limbAngle, limbDistance, tickDelta);
            model.setAngles(livingEntity, limbAngle, limbDistance, animationProgress, -yaw, pitch);

            //See LivingEntityRenderer#render
            renderer.setupTransforms(livingEntity, stack, animationProgress, bodyYaw, tickDelta);
            stack.scale(-1, -1, 1);
            renderer.scale(livingEntity, stack, tickDelta);
            stack.translate(0, -1.501F, 0);

            if (model instanceof MobbMode m) {
                //Player / any other biped entity wireframe rendering.
                if (model instanceof BipedEntityModel mo) {
                    if (entity instanceof PlayerEntity && WireframeEntityRenderer.player_skeleton) {
                        Renderer3D.renderThroughWalls();
                        float lerpBody = MathHelper.lerpAngleDegrees(tickDelta, livingEntity.prevBodyYaw, livingEntity.bodyYaw);

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
            float h = EndCrystalEntityRenderer.getYOffset(tickDelta);


            float j = ((float) crystalEntity.endCrystalAge + tickDelta) * 3.0F;
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
    private static void renderModelParts(EntityModel<?> model, MatrixStack stack, VertexConsumerProvider buffer) {
        for (ModelPart part : model.getParts()) {
            renderModelPart(part, stack, buffer);
        }
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
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
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


     */
}
