package dev.heliosclient.util;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static dev.heliosclient.HeliosClient.MC;

public class ChunkUtils {

    public static Stream<BlockEntity> getBlockEntityStreamInChunks() {
        return getLoadedChunks().stream().flatMap((chunk) -> chunk.getBlockEntities().values().stream());
    }

    public static List<WorldChunk> getLoadedChunks() {
        List<WorldChunk> chunks = new ArrayList<>();
        int radius = Math.max(2, MC.options.getClampedViewDistance() + 3);
        ChunkPos center = new ChunkPos(MC.player.getBlockPos());

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                ChunkPos pos = new ChunkPos(center.x + dx, center.z + dz);
                if (MC.world.isChunkLoaded(pos.x, pos.z)) {
                    chunks.add(MC.world.getChunk(pos.x, pos.z));
                }
            }
        }

        return chunks;
    }
}
