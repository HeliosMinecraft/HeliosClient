package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.player.PlayerMotionEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;

public class Speed extends Module_ {
    private final SettingGroup sgGeneral = new SettingGroup("General");

    DoubleSetting speed = sgGeneral.add(new DoubleSetting.Builder()
            .name("Speed")
            .description("Multiplier of speed.")
            .onSettingChange(this)
            .value(2.0)
            .defaultValue(2.0)
            .min(0.1)
            .max(10)
            .roundingPlace(1)
            .build()
    );

    public Speed() {
        super("Speed", "Allows you to move faster.", Categories.MOVEMENT);

        addSettingGroup(sgGeneral);

        addQuickSettings(sgGeneral.getSettings());
    }

    @SubscribeEvent
    public void onMotion(PlayerMotionEvent event) {
        assert mc.player != null;
    }

}
