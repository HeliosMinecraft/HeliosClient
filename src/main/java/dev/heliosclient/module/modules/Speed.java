package dev.heliosclient.module.modules;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.PlayerMotionEvent;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingBuilder;

public class Speed extends Module_ {
    private final SettingBuilder sgGeneral = new SettingBuilder("General");

    DoubleSetting speed = sgGeneral.add(new DoubleSetting.Builder()
            .name("Speed")
            .description("Multiplier of speed.")
            .module(this)
            .value(2.0)
            .min(0.1)
            .max(10)
            .roundingPlace(1)
            .build()
    );

    public Speed() {
        super("Speed", "Allows you to move faster.", Category.MOVEMENT);

        settingBuilders.add(sgGeneral);
        quickSettingsBuilder.add(sgGeneral);
    }

    @SubscribeEvent
    public void onMotion(PlayerMotionEvent event) {
        assert mc.player != null;
        mc.player.addVelocity(event.getMovement());
    }

}
