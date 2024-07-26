package dev.heliosclient.event.events.world;

import dev.heliosclient.event.Event;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.world.chunk.Chunk;

public class ChunkDataEvent extends Event {
    private final Chunk chunk;
    private final ChunkDataS2CPacket packet;

    public ChunkDataEvent(Chunk chunk, ChunkDataS2CPacket packet) {
        this.chunk = chunk;
        this.packet = packet;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public ChunkDataS2CPacket getPacket() {
        return packet;
    }
}
