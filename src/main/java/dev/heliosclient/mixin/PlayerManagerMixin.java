package dev.heliosclient.mixin;

import dev.heliosclient.event.Event;
import dev.heliosclient.event.events.player.PlayerLeaveEvent;
import dev.heliosclient.managers.EventManager;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Inject(method = "remove", at = @At("TAIL"), cancellable = true)
    private void onDisconnect(ServerPlayerEntity player, CallbackInfo ci) {
        Event event = new PlayerLeaveEvent(player);
        if (EventManager.postEvent(event).isCanceled())
            ci.cancel();
    }
}

