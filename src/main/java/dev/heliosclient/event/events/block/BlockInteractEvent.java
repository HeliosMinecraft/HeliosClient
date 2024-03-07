package dev.heliosclient.event.events.block;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

/**
 * Event fired on block interaction.
 */
@Cancelable
@LuaEvent("BlockInteractEvent")
public class BlockInteractEvent extends Event {
    private final BlockPos pos;
    private final BlockState state;

    public BlockInteractEvent(BlockPos pos, BlockState state) {
        this.pos = pos;
        this.state = state;
    }

    /**
     * @return Position of BlockInteractEvent
     */
    public BlockPos getPos() {
        return pos;
    }

    /**
     * @return State of interacted block.
     */
    public BlockState getState() {
        return state;
    }
}
