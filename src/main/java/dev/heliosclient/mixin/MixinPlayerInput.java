package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.events.player.PlayerJumpEvent;
import dev.heliosclient.managers.EventManager;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInput.class)
public abstract class MixinPlayerInput {
    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    protected void onJump(CallbackInfoReturnable<Boolean> cir) {
        if(HeliosClient.MC.player != null) {
            PlayerJumpEvent event = new PlayerJumpEvent(HeliosClient.MC.player);
            EventManager.postEvent(event);
            if (event.isCanceled()) cir.cancel();
        }
    }
}
