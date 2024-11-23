package dev.heliosclient.mixin;

import com.mojang.brigadier.ParseResults;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.events.client.InventoryEvent;
import dev.heliosclient.event.events.player.PlayerDeathEvent;
import dev.heliosclient.event.events.player.PlayerJoinEvent;
import dev.heliosclient.event.events.player.PlayerRespawnEvent;
import dev.heliosclient.event.events.world.ChunkDataEvent;
import dev.heliosclient.managers.CommandManager;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.chat.ChatTweaks;
import dev.heliosclient.system.UniqueID;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Unique
    private boolean ignoreSendChatMsg = false;

    @Shadow
    public abstract void sendChatMessage(String content);

    @Shadow protected abstract ParseResults<CommandSource> parse(String command);

    @Inject(method = "onGameJoin", at = @At("RETURN"), cancellable = true)
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo info) {
        PlayerEntity player = HeliosClient.MC.player;
        Event event = new PlayerJoinEvent(player, packet);
        EventManager.postEvent(event);
        if (event.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String content, CallbackInfo ci) {
        if (ignoreSendChatMsg)
            return;

        if (CommandManager.prefix != null && content.startsWith(CommandManager.prefix)) return;
        ChatTweaks tweaks = ModuleManager.get(ChatTweaks.class);
        if (!tweaks.isActive())
            return;


        String modifiedMsg = content;

        if (tweaks.prefix.value) {
            modifiedMsg = tweaks.prefixTxt.value + content;
        }
        if (tweaks.suffix.value) {
            modifiedMsg = modifiedMsg + tweaks.suffixTxt.value;
        }
        if (tweaks.antiantiSpam.value) {
            modifiedMsg = modifiedMsg + UniqueID.setLengthAndGet((int) tweaks.antispam_length.value).uniqueID;
        }

        ignoreSendChatMsg = true;
        sendChatMessage(modifiedMsg);
        ignoreSendChatMsg = false;
        ci.cancel();
    }


    @Inject(method = "onChunkData", at = @At("TAIL"))
    private void onChunkData(ChunkDataS2CPacket packet, CallbackInfo ci) {
        ChunkDataEvent event = new ChunkDataEvent(HeliosClient.MC.world.getChunk(packet.getChunkX(), packet.getChunkZ()),packet);
        EventManager.postEvent(event);
    }

    @Inject(method = "onPlayerRespawn", at = @At("RETURN"), cancellable = true)
    private void onPlayerRespawn(PlayerRespawnS2CPacket packet, CallbackInfo info) {
        PlayerEntity player = HeliosClient.MC.player;
        Event event = new PlayerRespawnEvent(player, packet);
        if (EventManager.postEvent(event).isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "onInventory", at = @At("TAIL"))
    private void onInventory(InventoryS2CPacket packet, CallbackInfo ci) {
        EventManager.postEvent(new InventoryEvent(packet));
    }

    @Inject(method = "onDeathMessage", at = @At("HEAD"), cancellable = true)
    private void onDeathMessage(DeathMessageS2CPacket packet, CallbackInfo ci) {
        if (HeliosClient.MC.player != null && packet.getEntityId() == HeliosClient.MC.player.getId()) {
            Event event = new PlayerDeathEvent(HeliosClient.MC.player);
            if (EventManager.postEvent(event).isCanceled()) {
                ci.cancel();
            }
        }
    }
}
