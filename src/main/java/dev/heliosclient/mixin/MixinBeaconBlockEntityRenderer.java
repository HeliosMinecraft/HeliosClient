package dev.heliosclient.mixin;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.render.NoRender;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntityRenderer.class)
public abstract class MixinBeaconBlockEntityRenderer {
    @Inject(method = "render(Lnet/minecraft/block/entity/BeaconBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V", at = @At("HEAD"), cancellable = true)
    private void $onRender(BeaconBlockEntity beaconBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j, CallbackInfo info) {
        if ( NoRender.get().noBeaconBeam.value && NoRender.get().isActive()) info.cancel();
    }
}
