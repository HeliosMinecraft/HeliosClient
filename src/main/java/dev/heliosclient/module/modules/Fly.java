package dev.heliosclient.module.modules;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;

public class Fly extends Module_ {
    public Fly() {
        super("Fly", "Allows you to fly in survival mode.", Category.MOVEMENT);
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        mc.player.getAbilities().flying = true;
    }


    @Override
    public void onEnable() {
        super.onEnable();

    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (mc.player != null) {
            mc.player.getAbilities().flying = false;
        }
    }
}
