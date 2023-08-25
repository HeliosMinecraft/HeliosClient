package dev.heliosclient.event.events;

import dev.heliosclient.event.Event;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockBreakEvent implements Event {
    private final BlockPos pos;
    private final BlockState state;

    public BlockBreakEvent(BlockPos pos, BlockState state) {
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

