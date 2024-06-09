package dev.heliosclient.module.modules.player;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;

public class Rotation extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    public BooleanSetting yaw = sgGeneral.add(new BooleanSetting.Builder()
            .name("Yaw")
            .description("Locks your yaw")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );

    public DoubleSetting yawAngle = sgGeneral.add(new DoubleSetting.Builder()
            .name("Yaw angle")
            .description("Angle to lock your yaw in degrees")
            .onSettingChange(this)
            .defaultValue(0d)
            .value(0d)
            .min(0)
            .max(360)
            .roundingPlace(0)
            .shouldRender(() -> yaw.value)
            .build()
    );
    public BooleanSetting pitch = sgGeneral.add(new BooleanSetting.Builder()
            .name("Pitch")
            .description("Locks your pitch")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );

    public DoubleSetting pitchAngle = sgGeneral.add(new DoubleSetting.Builder()
            .name("Pitch angle")
            .description("Angle to lock your Pitch in degrees")
            .onSettingChange(this)
            .defaultValue(0d)
            .value(0d)
            .min(-90)
            .max(90)
            .roundingPlace(0)
            .shouldRender(() -> pitch.value)
            .build()
    );


    public Rotation() {
        super("Rotation", "Lock your pitch and yaw to certain angle", Categories.PLAYER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }


    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (yaw.value) {
            mc.player.setYaw((float) yawAngle.value);
            mc.player.setBodyYaw((float) yawAngle.value);
            mc.player.setHeadYaw((float) yawAngle.value);
        }
        if (pitch.value) {
            mc.player.setPitch((float) pitchAngle.value);
        }
    }
}
