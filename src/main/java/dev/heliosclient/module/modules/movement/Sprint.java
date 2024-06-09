package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;

public class Sprint extends Module_ {

    SettingGroup sgGeneral = new SettingGroup("General");

    BooleanSetting strictMode = sgGeneral.add(new BooleanSetting("Strict Mode", "Only sprints when you are moving", this, false, () -> true, true));

    public Sprint() {
        super("Sprint", "Automatically sprints for you", Categories.MOVEMENT);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player != null)
            mc.player.setSprinting(false);
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if (strictMode.value && mc.player.forwardSpeed != 0.0f && mc.player.getHungerManager().getFoodLevel() <= 6.5 && mc.player.sidewaysSpeed != 0.0f)
            return;

        mc.player.setSprinting(true);
    }
}
