package dev.heliosclient.util.render;

import dev.heliosclient.managers.CapeManager;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.player.DisplayPreviewEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
;import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.*;

public class DisplayPreviewEntityRenderer extends LivingEntityRenderer<LivingEntity, PlayerEntityModel<LivingEntity>> {
    private final ElytraEntityModel<LivingEntity> elytra;
    private final EntityRendererFactory.Context ctx;
    MinecraftClient mc = MinecraftClient.getInstance();


    public DisplayPreviewEntityRenderer(EntityRendererFactory.Context ctx, boolean slim) {
        super(ctx, new PlayerEntityModel<>(ctx.getPart(slim ? EntityModelLayers.PLAYER_SLIM : EntityModelLayers.PLAYER), slim), 0.5f);
        this.ctx = ctx;
        this.elytra = new ElytraEntityModel<>(ctx.getModelLoader().getModelPart(EntityModelLayers.ELYTRA));
    }

    public void render(DisplayPreviewEntity entity, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, float mouseX, float mouseY) {
        setModelPose();
        matrixStack.push();

        model.child = false;

        matrixStack.scale(0.9375f, 0.9375f, 0.9375f);
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getYaw()));
        matrixStack.scale(-1.0f, -1.0f, 1.0f);
        matrixStack.translate(0.0f, -1.501f, 0.0f);


        float limbDistance = MathHelper.lerp(tickDelta, entity.getLastLimbDistance(), entity.getLimbDistance());
        float limbAngle = entity.getLimbAngle() - entity.getLimbDistance() * (1.0f - tickDelta);

        if (limbDistance > 1.0f) {
            limbDistance = 1.0f;
        }

        setAngles(limbAngle, limbDistance,false);

        // Calculate rotation based on head mouse position
        float mouseXRotation = (mouseX - (float) MinecraftClient.getInstance().getWindow().getScaledWidth() / 2 + matrixStack.peek().getPositionMatrix().m03()) * 0.015f;
        float mouseYRotation = (mouseY - (float) MinecraftClient.getInstance().getWindow().getScaledHeight() / 2 + matrixStack.peek().getPositionMatrix().m13()) * 0.015f;

        model.head.yaw = entity.getYaw() - mouseXRotation * 0.15f;
        model.head.pitch = mouseYRotation * 0.15f;

        if (entity.isShowBody()) {
            RenderLayer renderLayer = this.model.getLayer(entity.skinTexture.texture());
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
            int overlay = OverlayTexture.packUv(OverlayTexture.getU(0f), OverlayTexture.getV(false));
            model.render(matrixStack, vertexConsumer, light, overlay,1.0f,1.0f,1.0f,1.0f);
        }

        if (!entity.isShowElytra()) {
            if (CapeManager.getCurrentCapeTexture() == null) return;

            matrixStack.push();
            matrixStack.translate(0.0f, 0.0f, 0.125f);

            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(3.0f));
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));

            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getArmorCutoutNoCull(CapeManager.getCurrentCapeTexture()));
            ctx.getPart(EntityModelLayers.PLAYER).getChild("cloak")
                    .render(matrixStack, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
            matrixStack.pop();
        } else {
            Identifier identifier = CapeManager.getCurrentElytraTexture();
            matrixStack.push();
            matrixStack.translate(0.0f, 0.0f, 0.125f);

            this.model.copyStateTo(this.elytra);

            VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumerProvider, RenderLayer.getArmorCutoutNoCull(identifier), false,false);
            this.elytra.render(matrixStack, vertexConsumer, light, OverlayTexture.DEFAULT_UV,1.0f,1.0f,1.0f,1.0f);
            matrixStack.pop();
        }

        // Render the nametag
        renderLabelIfPresent(matrixStack, entity.profile.getName());

        matrixStack.pop();
    }

    private void renderLabelIfPresent(MatrixStack matrices,String text){
        int i = "deadmau5".equals(text) ? -10 : 0;
        matrices.push();
        matrices.scale(0.03F, 0.03F, 0.03F);
        Renderer2D.drawRoundedRectangle(matrices.peek().getPositionMatrix(),-FontRenderers.Mid_fxfontRenderer.getStringWidth(text)/2.0f - 2,i - 37, FontRenderers.Mid_fxfontRenderer.getStringWidth(text) + 4,4 + FontRenderers.Mid_fxfontRenderer.getStringHeight(Renderer2D.TEXT),3.0f, Color.darkGray.getRGB());
        FontRenderers.Mid_fxfontRenderer.drawString(matrices,text,-FontRenderers.Mid_fxfontRenderer.getStringWidth(text)/2.0f,i - 35,-1);
        matrices.pop();
    }

    private void setAngles(float f, float g, boolean legs) {
        model.body.yaw = 0.0f;
        model.rightArm.pivotZ = 0.0f;
        model.rightArm.pivotX = -5.0f;
        model.leftArm.pivotZ = 0.0f;
        model.leftArm.pivotX = 5.0f;

        model.rightArm.yaw = 0.0f;
        model.leftArm.yaw = 0.0f;

        model.body.pitch = 0.0f;
        model.rightLeg.pivotZ = 0.1f;
        model.leftLeg.pivotZ = 0.1f;
        model.rightLeg.pivotY = 12.0f;
        model.leftLeg.pivotY = 12.0f;
        model.head.pivotY = 0.0f;
        model.body.pivotY = 0.0f;
        model.leftArm.pivotY = 2.0f;
        model.rightArm.pivotY = 2.0f;

        model.rightArm.pitch = MathHelper.cos(f * 0.6662f + 3.1415927f) * 2.0f * g * 0.5f;
        model.leftArm.pitch = MathHelper.cos(f * 0.6662f) * 2.0f * g * 0.5f;
        if (legs) {
            model.rightLeg.pitch = MathHelper.cos(f * 0.6662f) * 1.4f * g;
            model.leftLeg.pitch = MathHelper.cos(f * 0.6662f + 3.1415927f) * 1.4f * g;
        }else{
            model.rightLeg.pitch = 0;
            model.leftLeg.pitch = 0;
        }
        model.rightArm.roll = 0.0f;
        model.leftArm.roll = 0.0f;
        model.rightLeg.roll = 0.0f;
        model.leftLeg.roll = 0.0f;
        model.rightLeg.yaw = 0.0f;
        model.leftLeg.yaw = 0.0f;

        model.hat.copyTransform(model.head);
        model.leftPants.copyTransform(model.leftLeg);
        model.rightPants.copyTransform(model.rightLeg);
        model.leftSleeve.copyTransform(model.leftArm);
        model.rightSleeve.copyTransform(model.rightArm);
        model.jacket.copyTransform(model.body);
    }

    private void setModelPose() {
        PlayerEntityModel<LivingEntity> playerEntityModel = this.getModel();
        playerEntityModel.setVisible(true);
        playerEntityModel.hat.visible = mc.options.isPlayerModelPartEnabled(PlayerModelPart.HAT);
        playerEntityModel.jacket.visible = mc.options.isPlayerModelPartEnabled(PlayerModelPart.JACKET);
        playerEntityModel.leftPants.visible = mc.options.isPlayerModelPartEnabled(PlayerModelPart.LEFT_PANTS_LEG);
        playerEntityModel.rightPants.visible = mc.options.isPlayerModelPartEnabled(PlayerModelPart.RIGHT_PANTS_LEG);
        playerEntityModel.leftSleeve.visible = mc.options.isPlayerModelPartEnabled(PlayerModelPart.LEFT_SLEEVE);
        playerEntityModel.rightSleeve.visible = mc.options.isPlayerModelPartEnabled(PlayerModelPart.RIGHT_SLEEVE);
    }

    @Override
    public Identifier getTexture(LivingEntity entity) {
        return DefaultSkinHelper.getTexture();
    }
}

