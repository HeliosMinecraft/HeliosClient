package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
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
    @Inject(method = "savePlayerData", at = @At("RETURN"))
    private void onSavePlayerData(PlayerEntity player, CallbackInfo ci) {
        EventManager.postEvent(new PlayerLeaveEvent(HeliosClient.MC.player));
    }
}
