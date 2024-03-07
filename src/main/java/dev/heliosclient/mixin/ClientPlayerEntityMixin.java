package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.player.PlayerMotionEvent;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Module_;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Inject(method = "move", at = @At(value = "TAIL"), cancellable = true)
    public void onMove(MovementType type, Vec3d movement, CallbackInfo ci) {
        PlayerMotionEvent event = new PlayerMotionEvent(type, movement);
        EventManager.postEvent(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

  @Inject(method = "init", at = @At(value = "TAIL"))
    public void onInit(CallbackInfo ci) {
        for (Module_ m : ModuleManager.INSTANCE.getEnabledModules()) {
            m.onEnable();
        }
    }

    @Inject(method = "tick", at = @At(value = "TAIL"), cancellable = true)
    public void onTick(CallbackInfo ci) {
        if (HeliosClient.MC.player != null) {
            TickEvent event = new TickEvent.PLAYER(HeliosClient.MC.player);
            EventManager.postEvent(event);
            if (event.isCanceled()) {
                ci.cancel();
            }
        }
    }
}
