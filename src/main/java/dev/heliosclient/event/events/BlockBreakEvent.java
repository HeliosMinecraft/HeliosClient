package dev.heliosclient.event.events;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

/**
 * Event fired every time block gets broken.
 */
@Cancelable
public class BlockBreakEvent extends Event {
    private final BlockPos pos;
    private final BlockState state;

    public BlockBreakEvent(BlockPos pos, BlockState state) {
        this.pos = pos;
        this.state = state;
    }

    /**
     * @return Position of BlockBreakEvent
     */
    public BlockPos getPos() {
        return pos;
    }

    /**
     * @return State of BlockBreakEvent
     */
    public BlockState getState() {
        return state;
    }
}

