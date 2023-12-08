package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.events.block.BlockInteractEvent;
import dev.heliosclient.event.events.player.ChatMessageEvent;
import dev.heliosclient.event.events.player.PlayerLeaveEvent;
import dev.heliosclient.managers.EventManager;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler {
    @Inject(method = "onPlayerInteractBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/hit/BlockHitResult;getPos()Lnet/minecraft/util/math/Vec3d;"), cancellable = true)
    private void onPlayerInteractBlock(PlayerInteractBlockC2SPacket packet, CallbackInfo ci) {
        ServerPlayNetworkHandler handler = (ServerPlayNetworkHandler) (Object) this;
        BlockState state = handler.player.getWorld().getBlockState(packet.getBlockHitResult().getBlockPos());
        BlockInteractEvent event = new BlockInteractEvent(packet.getBlockHitResult().getBlockPos(), state);
        EventManager.postEvent(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    private void onGameMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        ChatMessageEvent event = new ChatMessageEvent(packet.chatMessage());
        EventManager.postEvent(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onDisconnected", at = @At("RETURN"), cancellable = true)
    private void onDisconnected(Text reason, CallbackInfo ci) {
        PlayerLeaveEvent event = new PlayerLeaveEvent(HeliosClient.MC.player);
        EventManager.postEvent(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
