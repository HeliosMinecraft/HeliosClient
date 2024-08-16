package dev.heliosclient.event.events.player;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;
import net.minecraft.network.packet.Packet;

@Cancelable
@LuaEvent("PacketEvent")
public class PacketEvent extends Event {
    @Cancelable
    @LuaEvent("packetReceiveEvent")
    public static class RECEIVE extends PacketEvent {
        public Packet<?> packet;

        public RECEIVE(Packet<?> packet) {
            this.packet = packet;
        }

        public Packet<?> getPacket() {
            return packet;
        }
    }

    @Cancelable
    @LuaEvent("packetSendEvent")
    public static class SEND extends PacketEvent {
        public Packet<?> packet;

        public SEND(Packet<?> packet) {
            this.packet = packet;
        }

        public Packet<?> getPacket() {
            return packet;
        }
    }

    @LuaEvent("packetSentEvent")
    public static class SENT extends PacketEvent {
        public Packet<?> packet;

        public SENT(Packet<?> packet) {
            this.packet = packet;
        }

        public Packet<?> getPacket() {
            return packet;
        }
    }
}

