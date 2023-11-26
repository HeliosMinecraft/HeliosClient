package dev.heliosclient.module.modules;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;

public class Fly extends Module_ {
    public Fly() {
        super("Fly", "Allows you to fly in survival mode.", Categories.MOVEMENT);
    }


    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player != null) {
            mc.player.getAbilities().flying = true;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (mc.player != null) {
            mc.player.getAbilities().flying = false;
        }
    }
}
