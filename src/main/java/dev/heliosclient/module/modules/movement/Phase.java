package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;

public class Phase extends Module_ {
    public Phase() {
        super("Phase", "Placeholder module for phase", Categories.MOVEMENT);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player != null) {
            mc.player.noClip = true;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        mc.player.noClip = true;
        mc.player.getAbilities().allowFlying = true;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player != null) {
            mc.player.noClip = false;
            mc.player.getAbilities().allowFlying = false;
        }
    }
}
