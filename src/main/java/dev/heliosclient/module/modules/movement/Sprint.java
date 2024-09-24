package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;

public class Sprint extends Module_ {

    SettingGroup sgGeneral = new SettingGroup("General");

    BooleanSetting strictMode = sgGeneral.add(new BooleanSetting("Strict Mode", "Only sprints when you are moving", this, true));
    BooleanSetting keepSprint = sgGeneral.add(new BooleanSetting("Keep Sprint", "Keeps sprinting even after attacking", this, false));

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
        if (mc.player.getHungerManager().getFoodLevel() <= 6.0F)
            return;

        if (strictModeCheck()) {
            mc.player.setSprinting(true);
        }
    }
    public boolean strictModeCheck() {
        if(!strictMode.value){
            return mc.currentScreen == null;
        }
        return (mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0) &&
                !mc.player.horizontalCollision &&
                !mc.player.isTouchingWater() &&
                !mc.player.isSubmergedInWater() &&
                (mc.currentScreen == null || ModuleManager.get(GuiMove.class).isActive());
    }

    public boolean shouldStopSprinting() {
        return !isActive() || !keepSprint.value;
    }
}
