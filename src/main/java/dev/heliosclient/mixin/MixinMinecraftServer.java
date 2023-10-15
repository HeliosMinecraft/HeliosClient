package dev.heliosclient.mixin;

import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.managers.EventManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    @Inject(method = "tick", at = @At(value = "HEAD"), cancellable = true)
    public void onTick(CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        if (server != null) {

            TickEvent event = new TickEvent.WORLD(server);
            EventManager.postEvent(event);
            if (event.isCanceled()) {
                ci.cancel();
            }
        }
    }
}
