package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;

public class AutoJump extends Module_ {
    public SettingGroup sgGeneral = new SettingGroup("General");

    BooleanSetting onlyOnCollision = sgGeneral.add(new BooleanSetting.Builder()
            .name("On Horizontal Collision")
            .description("Only jumps when you collide with something")
            .onSettingChange(this)
            .value(false)
            .build()
    );


    public AutoJump() {
        super("AutoJump", "Jumps automatically for you", Categories.MOVEMENT);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if(onlyOnCollision.value && !mc.player.horizontalCollision)return;

        if (!mc.player.isOnGround() || mc.player.isSneaking()) return;

        mc.player.jump();
    }
}
