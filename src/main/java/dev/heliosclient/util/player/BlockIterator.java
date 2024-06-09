package dev.heliosclient.util.player;

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
        BlockPos nextPos = player.getBlockPos().add(x, y, z);
        while (!world.isChunkLoaded(nextPos.getX() >> 4, nextPos.getZ() >> 4)) {
            if (++x > horizontalRadius) {
                x = -horizontalRadius;
                if (++z > horizontalRadius) {
                    z = -horizontalRadius;
                    y++;
                }
            }
            nextPos = player.getBlockPos().add(x, y, z);
        }
        if (++x > horizontalRadius) {
            x = -horizontalRadius;
            if (++z > horizontalRadius) {
                z = -horizontalRadius;
                y++;
            }
        }
        return nextPos;
    }
}
