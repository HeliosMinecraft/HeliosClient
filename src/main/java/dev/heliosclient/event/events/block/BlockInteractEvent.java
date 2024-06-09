package dev.heliosclient.event.events.block;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

/**
 * Event fired on block interaction.
 */
@Cancelable
@LuaEvent("BlockInteractEvent")
public class BlockInteractEvent extends Event {
    private final BlockHitResult hitResult;
    private final Hand hand;


    public BlockInteractEvent(BlockHitResult hitResult, Hand hand) {
        this.hitResult = hitResult;
        this.hand = hand;
    }

    public Hand getHand() {
        return hand;
    }

    public BlockHitResult getHitResult() {
        return hitResult;
    }
}
