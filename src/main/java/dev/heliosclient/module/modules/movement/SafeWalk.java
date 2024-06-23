package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.player.ClipAtLedgeEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;

public class SafeWalk extends Module_ {
    public SafeWalk() {
        super("Safe Walk", "Automatically sneaks/stops near the edge of blocks to prevent you from falling", Categories.MOVEMENT);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @SubscribeEvent
    public void onClip(ClipAtLedgeEvent event) {

         event.setCanceled(true);

    }
}
