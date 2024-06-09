package dev.heliosclient.module.modules.player;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.player.ReachEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;

public class Reach extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");
    DoubleSetting reach = sgGeneral.add(new DoubleSetting.Builder()
            .name("Reach")
            .description("Reach")
            .onSettingChange(this)
            .defaultValue(5d)
            .value(5d)
            .min(0)
            .max(10)
            .roundingPlace(2)
            .build()
    );

    public Reach() {
        super("Reach", "Increase/Decrease your reach", Categories.PLAYER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @SubscribeEvent
    public void onReach(ReachEvent event) {
        event.setReach((float) reach.value);
        event.setCanceled(true);
    }
}
