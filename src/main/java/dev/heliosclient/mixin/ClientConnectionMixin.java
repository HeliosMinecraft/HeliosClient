package dev.heliosclient.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.managers.CommandManager;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.util.ChatUtils;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static <T extends PacketListener> void onHandlePacket(Packet<T> packet, PacketListener listener, CallbackInfo info) {
        if (packet instanceof BundleS2CPacket bundle) {
            for (Iterator<Packet<ClientPlayPacketListener>> i = bundle.getPackets().iterator(); i.hasNext(); ) {
                if (EventManager.postEvent(new PacketEvent.RECEIVE(i.next())).isCanceled()) i.remove();
            }
        } else if (EventManager.postEvent(new PacketEvent.RECEIVE(packet)).isCanceled()) info.cancel();
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void send(Packet<?> packet, CallbackInfo ci) {
        if (EventManager.postEvent(new PacketEvent.SEND(packet)).isCanceled()) ci.cancel();

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
