package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.Renderer3D;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {
    @Shadow
    @Final
    private Random random;
    @Shadow
    @Final
    private ItemRenderer itemRenderer;

    @Inject(method = "render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void render(ItemEntity itemEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        // ------ Future ItemPhysics ------- //
        // Todo: Use event and change scale. Also add custom rotations
      /*  ItemStack itemStack = itemEntity.getStack();
        int j = itemStack.isEmpty() ? 187 : Item.getRawId(itemStack.getItem()) + itemStack.getDamage();
        this.random.setSeed((long)j);
        BakedModel bakedModel = this.itemRenderer.getModel(itemStack, itemEntity.getWorld(), (LivingEntity)null, itemEntity.getId());
        boolean bl = bakedModel.hasDepth();
        int k = this.getRenderedAmount(itemStack);
        matrixStack.translate(0.0, 0.05F * bakedModel.getTransformation().getTransformation(ModelTransformationMode.GROUND).scale.y() * k, 0.0);
        float scale = bakedModel.getTransformation().ground.scale.x();
        if (!bl) {
            matrixStack.scale(scale, scale, scale);
            matrixStack.translate(0.0, -0.09375F * (k - 1) * 0.5F, 0.0);
        }
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));

        for(int u = 0; u < k; ++u) {
            matrixStack.push();
            if (u > 0 && !bl) {
                matrixStack.translate(this.random.nextFloat() * 0.3F - 0.15F, this.random.nextFloat() * 0.3F - 0.15F, this.random.nextFloat() * 0.3F - 0.15F);
            }

            this.itemRenderer.renderItem(itemStack, ModelTransformationMode.GROUND, false, matrixStack, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV, bakedModel);
            matrixStack.pop();
        }

        ci.cancel();

       */
    }

    @Unique
    private int getRenderedAmount(ItemStack stack) {
        return stack.getCount() > 48 ? 5 : stack.getCount() > 32 ? 4 : stack.getCount() > 16 ? 3 : stack.getCount() > 1 ? 2 : 1;
    }
}
