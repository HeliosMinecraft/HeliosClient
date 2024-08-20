package dev.heliosclient.module.modules.player;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.world.Timer;
import dev.heliosclient.system.TickRate;

public class TpsSync extends Module_ {

    public TpsSync() {
        super("TpsSync", "Syncs you and your actions with the server TPS", Categories.PLAYER);
    }
    @Override
    public void onDisable() {
        super.onDisable();
        ModuleManager.get(Timer.class).setOverride(Timer.RESET);
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        Timer timer = ModuleManager.get(Timer.class);
        if (timer.isActive()) return;

        if(TickRate.INSTANCE.getTPS() > 1){
            timer.setOverride(TickRate.INSTANCE.getTPS() / 20f);
        }else{
            timer.setOverride(Timer.RESET);
        }
    }


}
