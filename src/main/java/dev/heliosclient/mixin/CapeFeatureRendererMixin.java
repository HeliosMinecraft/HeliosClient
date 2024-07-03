package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.CapeManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.misc.CapeModule;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(CapeFeatureRenderer.class)
public abstract class CapeFeatureRendererMixin extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    public CapeFeatureRendererMixin(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
    }

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;FFFFFF)V", at = @At("HEAD"), cancellable = true)
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        if (!abstractClientPlayerEntity.isInvisible() && abstractClientPlayerEntity.isPartVisible(PlayerModelPart.CAPE) && ModuleManager.get(CapeModule.class).isActive()) {

            if (!CapeModule.forEveryone() && abstractClientPlayerEntity != HeliosClient.MC.player)
                return;

            Identifier capeTexture = CapeManager.CURRENT_PLAYER_CAPE;
            ItemStack itemStack = abstractClientPlayerEntity.getEquippedStack(EquipmentSlot.CHEST);

            if (capeTexture == null) {
                capeTexture = abstractClientPlayerEntity.getSkinTextures().capeTexture();
            }

            if (!itemStack.isOf(Items.ELYTRA)) {
                matrixStack.push();
                matrixStack.translate(0.0F, 0.0F, 0.125F);

                double d = MathHelper.lerp(h, abstractClientPlayerEntity.prevCapeX, abstractClientPlayerEntity.capeX) - MathHelper.lerp(h, abstractClientPlayerEntity.prevX, abstractClientPlayerEntity.getX());
                double e = MathHelper.lerp(h, abstractClientPlayerEntity.prevCapeY, abstractClientPlayerEntity.capeY) - MathHelper.lerp(h, abstractClientPlayerEntity.prevY, abstractClientPlayerEntity.getY());
                double m = MathHelper.lerp(h, abstractClientPlayerEntity.prevCapeZ, abstractClientPlayerEntity.capeZ) - MathHelper.lerp(h, abstractClientPlayerEntity.prevZ, abstractClientPlayerEntity.getZ());
                float n = MathHelper.lerpAngleDegrees(h, abstractClientPlayerEntity.prevBodyYaw, abstractClientPlayerEntity.bodyYaw);

                double o = MathHelper.sin(n * 0.017453292F);
                double p = -MathHelper.cos(n * 0.017453292F);

                float q = (float) e * 10.0F;
                q = MathHelper.clamp(q, -6.0F, 32.0F);

                float r = (float) (d * o + m * p) * 100.0F;
                r = MathHelper.clamp(r, 0.0F, 150.0F);

                float s = (float) (d * p - m * o) * 100.0F;
                s = MathHelper.clamp(s, -20.0F, 20.0F);

                if (r < 0.0F) {
                    r = 0.0F;
                }

                float t = MathHelper.lerp(h, abstractClientPlayerEntity.prevStrideDistance, abstractClientPlayerEntity.strideDistance);
                q += MathHelper.sin(MathHelper.lerp(h, abstractClientPlayerEntity.prevHorizontalSpeed, abstractClientPlayerEntity.horizontalSpeed) * 6.0F) * 32.0F * t;

                if (abstractClientPlayerEntity.isInSneakingPose()) {
                    q += 25.0F;
                }

                if (ModuleManager.get(CapeModule.class).customPhysics.value) {
                    //New custom physics which adds speed modifier along with a small wind simulation
                    double playerSpeed = Math.sqrt(abstractClientPlayerEntity.getVelocity().x * abstractClientPlayerEntity.getVelocity().x + abstractClientPlayerEntity.getVelocity().z * abstractClientPlayerEntity.getVelocity().z);
                    float speedModifier = (float) Math.min(1, playerSpeed / 0.5);
                    // Environmental factors
                    Biome biome = abstractClientPlayerEntity.getWorld().getBiome(abstractClientPlayerEntity.getBlockPos()).value();

                    //Random guess 120 blocks
                    boolean isWindyBiome = biome.getPrecipitation(abstractClientPlayerEntity.getBlockPos()) == Biome.Precipitation.RAIN || abstractClientPlayerEntity.getY() > 120;
                    boolean isRaining = abstractClientPlayerEntity.getWorld().isRaining();

                    // Directional wind
                    float windDirection = (float) Math.sin(System.currentTimeMillis() % 20000 / 20000.0 * 2 * Math.PI);
                    float windStrength = isWindyBiome && isRaining ? 0.2F : 0.1F;
                    float windEffect = windStrength * windDirection;

                    // Add wind effect to CURRENT_PLAYER_CAPE rotation
                    matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(6.0F + r / 2.0F + q + speedModifier * 15.0F + windEffect));
                    matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(s / 2.0F));
                    matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - s / 2.0F));
                } else {
                    // Old physics
                    matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(6.0F + r / 2.0F + q));
                }


                matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(s / 2.0F));
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - s / 2.0F));

                VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntitySolid(capeTexture));
                this.getContextModel().renderCape(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV);
                matrixStack.pop();
            }
        }
        ci.cancel();
    }
}

