package dev.heliosclient.mixin;

import dev.heliosclient.event.events.block.BlockBreakEvent;
import dev.heliosclient.event.events.player.ReachEvent;
import dev.heliosclient.managers.EventManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {

    @Inject(method = "getReachDistance", at = @At(value = "RETURN"), cancellable = true)
    private void getReach(CallbackInfoReturnable<Float> cir) {
        ReachEvent reachEvent = new ReachEvent(cir.getReturnValueF());
        EventManager.postEvent(reachEvent);
        cir.setReturnValue(reachEvent.getReach());
    }
}
