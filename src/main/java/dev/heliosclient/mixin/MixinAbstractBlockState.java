package dev.heliosclient.mixin;


import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.render.Xray;
import net.minecraft.block.AbstractBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class MixinAbstractBlockState {

    @Inject(at = @At("TAIL"), method = "getAmbientOcclusionLightLevel", cancellable = true)
    private void setAmbientOcclusionLightLevel(BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (ModuleManager.get(Xray.class).isActive()) {
            cir.setReturnValue(1f);
        }
    }
}
