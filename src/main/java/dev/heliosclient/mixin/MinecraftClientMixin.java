package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.events.player.PlayerJoinEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.managers.EventManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.client.MinecraftClient.getInstance;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    public ClientWorld world;

    @Inject(method = "tick", at = @At(value = "HEAD"), cancellable = true)
    public void onTick(CallbackInfo ci) {
        TickEvent ClientTick = new TickEvent.CLIENT(getInstance());
        EventManager.postEvent(ClientTick);

        TickEvent DefaultTick = new TickEvent();
        EventManager.postEvent(DefaultTick);

        if (ClientTick.isCanceled() || DefaultTick.isCanceled()) {
            ci.cancel();
        }
    }


    @Inject(at = @At("TAIL"), method = "scheduleStop")
    public void onShutdown(CallbackInfo ci) {
        // FUCK LAG
        HeliosClient.INSTANCE.saveConfig();
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"), cancellable = true)
    private void onDisconnect(Screen screen, CallbackInfo info) {
        if (world != null) {
            PlayerEntity player = HeliosClient.MC.player;
            Event event = new PlayerJoinEvent(player);
            EventManager.postEvent(event);
            if (event.isCanceled()) {
                info.cancel();
            }
        }
    }
}
