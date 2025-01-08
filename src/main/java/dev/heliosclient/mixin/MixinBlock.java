package dev.heliosclient.mixin;

import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.movement.NoSlow;
import dev.heliosclient.module.modules.movement.Slippy;
import dev.heliosclient.module.modules.render.Xray;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Block.class)
public abstract class MixinBlock {
    @Inject(method = "shouldDrawSide", at = @At("RETURN"), cancellable = true)
    private static void modifyDrawSide(BlockState state, BlockState otherState, Direction side, CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.get(Xray.class).isActive()) {
            cir.setReturnValue(ModuleManager.get(Xray.class).shouldXray(state.getBlock()));
            cir.cancel();
        }
    }

    @Inject(method = "getSlipperiness", at = @At("RETURN"), cancellable = true)
    private void addSlipperiness(CallbackInfoReturnable<Float> cir) {
        if (ModuleManager.get(Slippy.class) != null && ModuleManager.get(Slippy.class).isActive()) {
            cir.setReturnValue(0.989F);
        }
    }

    @SuppressWarnings("all")
    @Inject(method = "getVelocityMultiplier", at = @At("RETURN"), cancellable = true)
    private void addVelocityMultiplier(CallbackInfoReturnable<Float> cir) {
        if (ModuleManager.get(Slippy.class) != null && ModuleManager.get(Slippy.class).isActive()) {
            cir.setReturnValue(1.05F);
        }
        NoSlow ns = NoSlow.get();
        Block block = (Block) (Object) this;
        if (ns.slimeBlocks.value && ns.isActive() && block == Blocks.SLIME_BLOCK) {
            cir.setReturnValue(1.0f);
        }
        if (ns.honeyBlock.value && ns.isActive() && block == Blocks.HONEY_BLOCK) {
            cir.setReturnValue(1.0f);
        }
        if (ns.soulSand.value && ns.isActive() && block == Blocks.SOUL_SAND) {
            cir.setReturnValue(1.0f);
        }
        if (ns.cobWebs.value && ns.isActive() && block == Blocks.SOUL_SAND) {
            cir.setReturnValue(1.0f);
        }
    }
}