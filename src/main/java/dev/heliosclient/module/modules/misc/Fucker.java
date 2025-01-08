package dev.heliosclient.module.modules.misc;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.util.blocks.BlockUtils;
import dev.heliosclient.util.entity.EntityUtils;
import net.minecraft.util.math.BlockPos;

/**
 * PlaceHolder / Incomplete
 */
public class Fucker extends Module_ {
    public Fucker() {
        super("Fucker", "Does not break anything", Categories.MISC);
    }

    @SubscribeEvent
    public void onTick(TickEvent.CLIENT.PRE event) {
        if(!HeliosClient.shouldUpdate()) return;

        BlockPos bedPos = EntityUtils.getNearestBed(mc.world, mc.player, (int) mc.player.getBlockInteractionRange());

        if (bedPos == null) return;

        BlockUtils.breakBlock(bedPos, true);
    }
}
