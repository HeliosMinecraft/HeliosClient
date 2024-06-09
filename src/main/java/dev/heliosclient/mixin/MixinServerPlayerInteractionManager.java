package dev.heliosclient.mixin;

import dev.heliosclient.event.events.block.BlockBreakEvent;
import dev.heliosclient.managers.EventManager;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class MixinServerPlayerInteractionManager {
    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Inject(method = "tryBreakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onBroken(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V"), cancellable = true)
    private void onTryHarvestBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = player.getWorld().getBlockState(pos);
        BlockBreakEvent event = new BlockBreakEvent(pos, state);
        EventManager.postEvent(event);
        if (event.isCanceled()) {
            cir.setReturnValue(event.isCanceled());
        }
    }
}
