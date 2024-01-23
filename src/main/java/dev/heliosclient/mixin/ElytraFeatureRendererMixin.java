package dev.heliosclient.mixin;

import dev.heliosclient.managers.CapeManager;
import dev.heliosclient.managers.ModuleManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ElytraFeatureRenderer.class)
public abstract class ElytraFeatureRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {
    @Shadow
    @Final
    private static final Identifier SKIN = new Identifier("textures/entity/elytra.png");
    @Shadow
    @Final
    private ElytraEntityModel<T> elytra;


    public ElytraFeatureRendererMixin(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Inject(method = "render*", at = @At("HEAD"), cancellable = true)
    private void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        if (ModuleManager.capeModule.isActive() && ModuleManager.capeModule.elytra.value) {

            ItemStack itemStack = livingEntity.getEquippedStack(EquipmentSlot.CHEST);
            if (itemStack.isOf(Items.ELYTRA)) {
                Identifier capeTexture;

                if (livingEntity instanceof AbstractClientPlayerEntity playerEntity) {
                    SkinTextures skinTextures = playerEntity.getSkinTextures();
                    if (skinTextures.elytraTexture() != null) {
                        capeTexture = skinTextures.elytraTexture();
                    } else if (CapeManager.shouldPlayerHaveCape(playerEntity) && playerEntity.isPartVisible(PlayerModelPart.CAPE)) {
                        capeTexture = CapeManager.getElytraTexture(playerEntity);
                    } else {
                        capeTexture = SKIN;
                    }
                } else {
                    capeTexture = SKIN;
                }

                matrixStack.push();
                matrixStack.translate(0.0F, 0.0F, 0.125F);
                this.getContextModel().copyStateTo(this.elytra);
                this.elytra.setAngles(livingEntity, f, g, j, k, l);
                VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumerProvider, RenderLayer.getArmorCutoutNoCull(capeTexture), false, itemStack.hasGlint());
                this.elytra.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
                matrixStack.pop();
            }
            ci.cancel();
        }
    }
}
