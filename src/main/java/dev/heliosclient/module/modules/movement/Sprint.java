package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.player.PlayerUtils;

public class Sprint extends Module_ {

    SettingGroup sgGeneral = new SettingGroup("General");

    BooleanSetting strictMode = sgGeneral.add(new BooleanSetting("Strict Mode", "Only sprints when you are moving", this, false, () -> true, true));
    BooleanSetting keepSprint = sgGeneral.add(new BooleanSetting("Keep Sprint", "Keeps sprinting even after attacking", this, false, () -> true, false));

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
        if (mc.player.getHungerManager().getFoodLevel() <= 6)
            return;

        if(strictMode.value && mc.player.forwardSpeed > 0.00f) {
            mc.player.setSprinting(true);
        }else if(!strictMode.value){
            mc.player.setSprinting(true);
        }
    }
    public boolean shouldStopSprinting() {
        return !isActive() || !keepSprint.value;
    }
}
