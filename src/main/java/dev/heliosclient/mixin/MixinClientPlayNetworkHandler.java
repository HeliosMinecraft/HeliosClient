package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.Event;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.event.events.PlayerJoinEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
    @Shadow
    @Final
    private MinecraftClient client;


    @Inject(method = "onGameJoin", at = @At("RETURN"), cancellable = true)
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo info) {
        MixinClientPlayNetworkHandler handler = this;
        PlayerEntity player = handler.client.player;
        Event event = new PlayerJoinEvent(player);
        EventManager.postEvent(event);
        if (event.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "onDisconnect", at = @At("RETURN"), cancellable = true)
    private void onDisconnect(DisconnectS2CPacket packet, CallbackInfo info) {
        MixinClientPlayNetworkHandler handler = this;
        PlayerEntity player = handler.client.player;
        Event event = new PlayerJoinEvent(player);
        EventManager.postEvent(event);
        if (event.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "onPlayerRespawn", at = @At("RETURN"), cancellable = true)
    private void onPlayerRespawn(PlayerRespawnS2CPacket packet, CallbackInfo info) {
        MixinClientPlayNetworkHandler handler = this;
        PlayerEntity player = handler.client.player;
        Event event = new PlayerJoinEvent(player);
        EventManager.postEvent(event);
        if (event.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "onDeathMessage", at = @At("HEAD"), cancellable = true)
    private void onDeathMessage(DeathMessageS2CPacket packet, CallbackInfo ci) {
        if (HeliosClient.MC.player != null) {
            Event event = new PlayerJoinEvent(HeliosClient.MC.player);
            EventManager.postEvent(event);
            if (event.isCanceled()) {
                ci.cancel();
            }
        }
    }
}
