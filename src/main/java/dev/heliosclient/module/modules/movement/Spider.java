package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.CycleSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;

import java.util.List;

public class Spider extends Module_ {
    private final SettingGroup sgGeneral = new SettingGroup("General");
    CycleSetting mode = sgGeneral.add(new CycleSetting.Builder()
            .name("Spider Mode")
            .description("Change the way player spiders")
            .value(List.of(Modes.values()))
            .onSettingChange(this)
            .addOptionToolTip("Climb like you are climbing a ladder")
            .addOptionToolTip("Use velocity updates to climb")
            .defaultListOption(Modes.Climbing)
            .build()
    );
    DoubleSetting speed = sgGeneral.add(new DoubleSetting.Builder()
            .name("Speed")
            .description("Speed while climbing using velocity")
            .min(0f)
            .max(1.5f)
            .value(0.2d)
            .defaultValue(0.2d)
            .roundingPlace(2)
            .onSettingChange(this)
            .shouldRender(() -> mode.getOption() == Modes.Velocity)
            .build()
    );
    BooleanSetting inWater = sgGeneral.add(new BooleanSetting.Builder()
            .name("In Water")
            .description("Should climb while in water")
            .onSettingChange(this)
            .value(false)
            .build()
    );

    public Spider() {
        super("Spider", "Climb walls like spiders", Categories.MOVEMENT);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if (!inWater.value && mc.player.isSubmergedInWater()) return;

        if (mc.player.getVelocity().y >= speed.value) return;

        if (mode.getOption() == Modes.Velocity && mc.player.horizontalCollision) {
            mc.player.setVelocity(0, speed.value, 0);
        }
    }

    public enum Modes {
        Climbing,
        Velocity
    }
}
