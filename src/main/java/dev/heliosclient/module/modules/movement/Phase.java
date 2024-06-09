package dev.heliosclient.module.modules.movement;

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

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player != null) {
            mc.player.noClip = false;
        }
    }
}
