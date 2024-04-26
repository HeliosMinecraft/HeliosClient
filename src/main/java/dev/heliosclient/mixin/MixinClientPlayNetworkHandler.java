package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.events.player.PlayerDeathEvent;
import dev.heliosclient.event.events.player.PlayerJoinEvent;
import dev.heliosclient.event.events.player.PlayerRespawnEvent;
import dev.heliosclient.managers.EventManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {


    @Inject(method = "onGameJoin", at = @At("RETURN"), cancellable = true)
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo info) {
        PlayerEntity player = HeliosClient.MC.player;
        Event event = new PlayerJoinEvent(player,packet);
        EventManager.postEvent(event);
        if (event.isCanceled()) {
            info.cancel();
        }
    }


    @Inject(method = "onPlayerRespawn", at = @At("RETURN"), cancellable = true)
    private void onPlayerRespawn(PlayerRespawnS2CPacket packet, CallbackInfo info) {
        PlayerEntity player = HeliosClient.MC.player;
        Event event = new PlayerRespawnEvent(player,packet);
        if (EventManager.postEvent(event).isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "onDeathMessage", at = @At("HEAD"), cancellable = true)
    private void onDeathMessage(DeathMessageS2CPacket packet, CallbackInfo ci) {
        if (HeliosClient.MC.player != null) {
            Event event = new PlayerDeathEvent(HeliosClient.MC.player);
            if (EventManager.postEvent(event).isCanceled()) {
                ci.cancel();
            }
        }
    }
}
