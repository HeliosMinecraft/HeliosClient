package dev.heliosclient.scripting.libraries;

import dev.heliosclient.HeliosClient;
import net.minecraft.network.packet.Packet;
import org.luaj.vm2.LuaValue;
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

        env.set("packetLib", library);
        if (!env.get("package").isnil())
            env.get("package").get("loaded").set("packetLib", library);

        return library;
    }

    //Todo: Add more packets
}
