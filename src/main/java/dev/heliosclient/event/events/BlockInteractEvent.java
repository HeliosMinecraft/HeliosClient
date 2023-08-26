package dev.heliosclient.event.events;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

@Cancelable
public class BlockInteractEvent extends Event {
    private final BlockPos pos;
    private final BlockState state;

    public BlockInteractEvent(BlockPos pos, BlockState state) {
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
