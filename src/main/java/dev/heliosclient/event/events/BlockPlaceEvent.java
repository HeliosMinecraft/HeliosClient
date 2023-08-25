package dev.heliosclient.event.events;

import dev.heliosclient.event.Event;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockPlaceEvent implements Event {
    private final BlockPos pos;
    private final BlockState state;

    public BlockPlaceEvent(BlockPos pos, BlockState state) {
        this.pos = pos;
        this.state = state;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }
}
