package dev.heliosclient.module.modules.player;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.player.PlayerDeathEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import net.minecraft.client.gui.screen.DeathScreen;

public class AutoRespawn extends Module_ {
    public AutoRespawn() {
        super("AutoRespawn", "Automatically respawns on death", Categories.PLAYER);
    }


    @SubscribeEvent
    public void onDeath(PlayerDeathEvent event) {
        mc.player.requestRespawn();

        //Prevents the respawn screen from staying up.
        mc.execute(() -> mc.setScreen(null));
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if (mc.currentScreen instanceof DeathScreen) {
            mc.player.requestRespawn();

            //Prevents the respawn screen from staying up.
            mc.execute(() -> mc.setScreen(null));
        }
    }
}
