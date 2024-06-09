package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;

public class AutoJump extends Module_ {
    public AutoJump() {
        super("AutoJump", "Jumps automatically for you", Categories.MOVEMENT);
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if (!mc.player.isOnGround() || mc.player.isSneaking()) return;

        mc.player.jump();
    }
}
