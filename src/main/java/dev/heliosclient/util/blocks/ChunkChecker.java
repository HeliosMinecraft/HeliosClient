package dev.heliosclient.util.blocks;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.function.BiConsumer;

public record ChunkChecker(World world, Chunk chunk, BiConsumer<World, BlockPos> biConsumer) implements Runnable {

    @Override
    public void run() {
        Heightmap heightmap = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE);
        int maxY;
        for (int x = chunk.getPos().getStartX(); x <= chunk.getPos().getEndX(); x++) {
            for (int z = chunk.getPos().getStartZ(); z <= chunk.getPos().getEndZ(); z++) {
                maxY = heightmap.get(x - chunk.getPos().getStartX(), z - chunk.getPos().getStartZ());
                for (int y = world.getBottomY(); y < maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    biConsumer.accept(world, pos);
                }
            }
        }
    }
}
