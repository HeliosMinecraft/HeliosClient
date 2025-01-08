package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.CapeManager;
import dev.heliosclient.module.modules.misc.CapeModule;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = CapeFeatureRenderer.class,priority = 1001)
public abstract class CapeFeatureRendererMixin extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {

    @Shadow protected abstract boolean hasCustomModelForLayer(ItemStack stack, EquipmentModel.LayerType layerType);

    @Shadow @Final private BipedEntityModel<PlayerEntityRenderState> model;

    public CapeFeatureRendererMixin(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
        super(context);
    }
    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/PlayerEntityRenderState;FF)V", at = @At("HEAD"), cancellable = true)
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, PlayerEntityRenderState playerEntityRenderState, float f, float g, CallbackInfo ci) {
        if (!playerEntityRenderState.invisible && playerEntityRenderState.capeVisible) {
            Identifier capeTexture = CapeManager.getCurrentCapeTexture();

            if (capeTexture != null && !CapeModule.forEveryone() && playerEntityRenderState.id != HeliosClient.MC.player.getId())
                return;

            if (capeTexture == null) {
                capeTexture = playerEntityRenderState.skinTextures.capeTexture();
            }

            if (capeTexture != null) {
                if (!this.hasCustomModelForLayer(playerEntityRenderState.equippedChestStack, EquipmentModel.LayerType.WINGS)) {
                    matrixStack.push();
                    if (this.hasCustomModelForLayer(playerEntityRenderState.equippedChestStack, EquipmentModel.LayerType.HUMANOID)) {
                        matrixStack.translate(0.0F, -0.053125F, 0.06875F);
                    }

                    VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntitySolid(capeTexture));
                    this.getContextModel().copyTransforms(this.model);
                    this.model.setAngles(playerEntityRenderState);
                    this.model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV);
                    matrixStack.pop();
                }
            }
        }
        ci.cancel();
    }

}

