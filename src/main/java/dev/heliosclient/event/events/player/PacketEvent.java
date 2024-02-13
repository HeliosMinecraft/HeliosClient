package dev.heliosclient.event.events.player;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import net.minecraft.network.packet.Packet;

@Cancelable
public class PacketEvent extends Event {
    @Cancelable
    public static class RECEIVE extends PacketEvent {
        public Packet<?> packet;

        public RECEIVE(Packet<?> packet) {
            this.packet = packet;
        }
    }

    @Cancelable
    public static class SEND extends PacketEvent {
        public Packet<?> packet;

        public SEND(Packet<?> packet) {
            this.packet = packet;
        }
    }
}

