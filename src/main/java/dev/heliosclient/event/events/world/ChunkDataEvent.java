package dev.heliosclient.event.events.world;

import dev.heliosclient.event.Event;
import net.minecraft.world.chunk.Chunk;

public class ChunkDataEvent extends Event {
    private final Chunk chunk;

    public ChunkDataEvent(Chunk chunk) {
        this.chunk = chunk;
    }

    public Chunk getChunk() {
        return chunk;
    }
}
