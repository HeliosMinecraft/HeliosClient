package dev.heliosclient.module.modules.world;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.block.CancelBlockBreakingEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;

public class AbortBreaking extends Module_ {
    public AbortBreaking() {
        super("AbortBreaking", "Allows you to abort breaking without loosing progress", Categories.WORLD);
    }

    @SubscribeEvent
    public void onCancelBlockBreaking(CancelBlockBreakingEvent event) {
        event.setCanceled(true);
    }
}
