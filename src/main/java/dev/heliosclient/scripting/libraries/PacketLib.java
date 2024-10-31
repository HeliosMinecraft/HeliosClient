package dev.heliosclient.scripting.libraries;

import dev.heliosclient.HeliosClient;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

public class PacketLib extends TwoArgFunction {
    public PacketLib() {

    }

    public static void sendPacket(Packet<?> packet) {
        if (HeliosClient.MC.player != null && HeliosClient.MC.player.networkHandler != null) {
            HeliosClient.MC.player.networkHandler.sendPacket(packet);
        }
    }

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue library = tableOf();
        library.set("sendChatMessagePacket", new sendChatMessagePacket());

        env.set("packetLib", library);
        if (!env.get("package").isnil())
            env.get("package").get("loaded").set("packetLib", library);

        return library;
    }

    static class sendChatMessagePacket extends OneArgFunction {
        public LuaValue call(LuaValue message) {
            String chatMessage = message.checkjstring();
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(chatMessage);
            Packet<?> packet = new ChatMessageC2SPacket(buf);
            sendPacket(packet);

            return NIL;
        }
    }

    //Todo: Add more packets
}
