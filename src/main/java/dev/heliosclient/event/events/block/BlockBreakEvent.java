package dev.heliosclient.event.events.block;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

/**
 * Event fired every time block gets broken.
 */
@Cancelable
@LuaEvent("BlockBreakEvent")
public class BlockBreakEvent extends Event {
    private final BlockPos pos;
    private final BlockState state;

    public BlockBreakEvent(BlockPos pos, BlockState state) {
        this.pos = pos;
        this.state = state;
    }

    /**
     * Lua code: "pos"
     *
     * @return Position of BlockBreakEvent
     */
    public BlockPos getPos() {
        return pos;
    }

    /**
     *  Lua code: "state"
     *
     * @return State of BlockBreakEvent
     */
    public BlockState getState() {
        return state;
    }
}

