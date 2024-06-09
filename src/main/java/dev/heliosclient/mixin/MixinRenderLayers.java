package dev.heliosclient.mixin;

import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.render.Xray;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderLayers.class)
public abstract class MixinRenderLayers {
    @Inject(method = "getBlockLayer", at = @At("HEAD"), cancellable = true)
    private static void setBlockLayer(BlockState state, CallbackInfoReturnable<RenderLayer> info) {
        int alpha = (int) ModuleManager.get(Xray.class).alpha.value;
        if (ModuleManager.get(Xray.class).isActive() && !ModuleManager.get(Xray.class).shouldXray(state.getBlock()) && alpha > 0 && alpha < 255) {
            info.setReturnValue(RenderLayer.getTranslucent());
        }
    }
}