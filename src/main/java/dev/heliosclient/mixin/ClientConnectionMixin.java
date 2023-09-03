package dev.heliosclient.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.heliosclient.command.CommandManager;
import dev.heliosclient.util.ChatUtils;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void send(Packet<?> packet, CallbackInfo ci) {
        // Call commands if the prefix is sent
        if (packet instanceof ChatMessageC2SPacket && ((ChatMessageC2SPacket) packet).chatMessage().startsWith(CommandManager.get().getPrefix())) {
            try {
                CommandManager.get().dispatch(((ChatMessageC2SPacket) packet).chatMessage().substring(CommandManager.get().getPrefix().length()));
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
                ChatUtils.sendMsg(e.getMessage());
            }
            ci.cancel();
        }
    }
}
