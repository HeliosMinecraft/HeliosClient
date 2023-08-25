package dev.heliosclient.mixin;

import dev.heliosclient.event.events.BlockPlaceEvent;
import dev.heliosclient.event.events.ChatMessageEvent;
import dev.heliosclient.event.EventManager;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler {
    @Inject(method = "onPlayerInteractBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/hit/BlockHitResult;getBlockPos()Lnet/minecraft/util/math/BlockPos;"))
    private void onPlayerInteractBlock(PlayerInteractBlockC2SPacket packet, CallbackInfo ci) {
        ServerPlayNetworkHandler handler = (ServerPlayNetworkHandler) (Object) this;
        BlockState state = handler.player.getWorld().getBlockState(packet.getBlockHitResult().getBlockPos());
        EventManager.postEvent(new BlockPlaceEvent(packet.getBlockHitResult().getBlockPos(), state));
    }
    @Inject(method = "onChatMessage", at = @At("HEAD"))
    private void onGameMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        String message = packet.chatMessage();
        EventManager.postEvent(new ChatMessageEvent(message));
    }
}
