package dev.heliosclient.event.events.block;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Cancelable
public class PostAttackBlockEvent extends Event {
    private final BlockPos pos;
    private final Direction dir;


    public PostAttackBlockEvent(BlockPos pos, Direction dir) {
        this.pos = pos;
        this.dir = dir;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Direction getDir() {
        return dir;
    }
}
