package dev.heliosclient.event.events.player;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;

@Cancelable
@LuaEvent("PacketEvent")
public class PacketEvent extends Event {
    public Packet<?> packet;
    public ClientConnection connection;
    public PacketEvent(Packet<?> packet, ClientConnection connection){
        this.packet = packet;
        this.connection = connection;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public ClientConnection getConnection() {
        return connection;
    }

    @Cancelable
    @LuaEvent("packetReceiveEvent")
    public static class RECEIVE extends PacketEvent {

        public RECEIVE(Packet<?> packet, ClientConnection connection) {
            super(packet,connection);
        }
    }

    @Cancelable
    @LuaEvent("packetSendEvent")
    public static class SEND extends PacketEvent {
        public SEND(Packet<?> packet, ClientConnection connection) {
            super(packet,connection);
        }
    }

    @LuaEvent("packetSentEvent")
    public static class SENT extends PacketEvent {
        public SENT(Packet<?> packet, ClientConnection connection) {
            super(packet,connection);
        }
    }
}

