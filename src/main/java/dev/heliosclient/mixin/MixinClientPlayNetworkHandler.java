package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.*;
import dev.heliosclient.event.events.PlayerDeathEvent;
import dev.heliosclient.event.events.PlayerJoinEvent;
import dev.heliosclient.event.events.PlayerLeaveEvent;
import dev.heliosclient.event.events.PlayerRespawnEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onGameJoin", at = @At("RETURN"))
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo info) {
        MixinClientPlayNetworkHandler handler = (MixinClientPlayNetworkHandler) this;
        PlayerEntity player = handler.client.player;
        EventManager.postEvent(new PlayerJoinEvent(player));
    }
    @Inject(method = "onDisconnect", at = @At("RETURN"))
    private void onDisconnect(DisconnectS2CPacket packet, CallbackInfo info) {
        MixinClientPlayNetworkHandler handler = (MixinClientPlayNetworkHandler) this;
        PlayerEntity player = handler.client.player;
        EventManager.postEvent(new PlayerLeaveEvent(player));
    }
    @Inject(method = "onPlayerRespawn", at = @At("RETURN"))
    private void onPlayerRespawn(PlayerRespawnS2CPacket packet, CallbackInfo info) {
        MixinClientPlayNetworkHandler handler = (MixinClientPlayNetworkHandler) this;
        PlayerEntity player = handler.client.player;
        EventManager.postEvent(new PlayerRespawnEvent(player));
    }

    @Inject(method = "onDeathMessage", at = @At("HEAD"))
    private void onDeathMessage(DeathMessageS2CPacket packet, CallbackInfo ci) {
        if(HeliosClient.MC.player!=null) {
            EventManager.postEvent(new PlayerDeathEvent(HeliosClient.MC.player));
        }
    }
}
