package dev.heliosclient.mixin;


import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.render.Xray;
import dev.heliosclient.util.BlockUtils;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class MixinBlockEntityRenderDispatcher {
    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", cancellable = true)
    private <E extends BlockEntity> void render(E blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (ModuleManager.get(Xray.class).isActive() && !ModuleManager.get(Xray.class).shouldXray(BlockUtils.getBlock(blockEntity.getPos()))) {
            ci.cancel();
        }

    }
}