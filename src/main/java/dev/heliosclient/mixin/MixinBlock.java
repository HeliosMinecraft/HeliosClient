package dev.heliosclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.movement.NoSlow;
import dev.heliosclient.module.modules.movement.Slippy;
import dev.heliosclient.module.modules.render.Xray;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Block.class)
public abstract class MixinBlock {
    @ModifyReturnValue(method = "shouldDrawSide", at = @At("RETURN"))
    private static boolean modifyDrawSide(boolean original, BlockState state, BlockView world, BlockPos pos, Direction side, BlockPos blockPos) {

        if (ModuleManager.get(Xray.class) != null && ModuleManager.get(Xray.class).isActive()) {
            return ModuleManager.get(Xray.class).shouldXray(state.getBlock());
        }

        return original;
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