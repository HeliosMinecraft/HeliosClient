package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.EventManager;
import dev.heliosclient.event.events.PlayerLeaveEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.WorldSaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldSaveHandler.class)
public class MixinWorldSaveHandler {
    @Inject(method = "savePlayerData", at = @At("RETURN"), cancellable = true)
    private void onSavePlayerData(PlayerEntity player, CallbackInfo ci) {
        PlayerLeaveEvent event = new PlayerLeaveEvent(HeliosClient.MC.player);
        EventManager.postEvent(event);
        if (event.isCanceled()){
            ci.cancel();
        }
    }
}
