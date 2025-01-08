package dev.heliosclient.util.blocks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Iterator;

public class BlockIterator implements Iterator<BlockPos> {
    private final PlayerEntity player;
    private final World world;
    private final int horizontalRadius;
    private final int verticalRadius;
    private int x, y, z;

    BlockPos.Mutable mut = new BlockPos.Mutable();

    public BlockIterator(PlayerEntity player, int horizontalRadius, int verticalRadius) {
        this.player = player;
        this.world = player.getWorld();
        this.horizontalRadius = horizontalRadius;
        this.verticalRadius = verticalRadius;
        this.x = -horizontalRadius;
        this.y = -verticalRadius;
        this.z = -horizontalRadius;
    }

    @Override
    public boolean hasNext() {
        return y <= verticalRadius;
    }

    @Override
    public BlockPos next() {
        while (y <= world.getWorldChunk(mut).getTopYInclusive() && y >= world.getBottomY()) {
            mut.set(player.getBlockPos().add(x, y, z));
            if (world.isChunkLoaded(mut.getX() >> 4, mut.getZ() >> 4)) {
                if (++x > horizontalRadius) {
                    x = -horizontalRadius;
                    if (++z > horizontalRadius) {
                        z = -horizontalRadius;
                        y++;
                    }
                }
                return mut.toImmutable();
            } else {
                if (++x > horizontalRadius) {
                    x = -horizontalRadius;
                    if (++z > horizontalRadius) {
                        z = -horizontalRadius;
                        y++;
                    }
                }
            }
        }
        return mut.toImmutable();
    }
}
