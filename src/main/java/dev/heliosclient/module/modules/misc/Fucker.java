package dev.heliosclient.module.modules.misc;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.util.BlockUtils;
import dev.heliosclient.util.EntityUtils;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.BlockPos;

/**
 * PlaceHolder / Incomplete
 */
public class Fucker extends Module_ {
    public Fucker() {
        super("Fucker", "Breaks beds for now", Categories.MISC);
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        BlockPos bedPos = EntityUtils.getNearestBed(mc.world, mc.player, (int) mc.interactionManager.getReachDistance());

        if (bedPos == null) return;

        BlockUtils.breakBlock(bedPos, true);
    }
}
