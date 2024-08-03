package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;

public class AutoSneak extends Module_ {

    private final SettingGroup sgGeneral = new SettingGroup("General");

    BooleanSetting inWater = sgGeneral.add(new BooleanSetting.Builder()
            .name("In Water")
            .description("Should sneak while in water")
            .onSettingChange(this)
            .value(false)
            .build()
    );
    public BooleanSetting packet = sgGeneral.add(new BooleanSetting.Builder()
            .name("Packet")
            .description("Packet mode for sneaking instead of regular vanilla")
            .onSettingChange(this)
            .value(false)
            .build()
    );

    public AutoSneak() {
        super("AutoSneak", "Sneaks automatically while on", Categories.MOVEMENT);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player != null) {
            mc.options.sneakKey.setPressed(false);
            mc.player.setSneaking(false);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if (!inWater.value && (mc.player.isTouchingWater() || mc.player.isSubmergedInWater())) return;

        if(packet.value) return;

        mc.options.sneakKey.setPressed(true);
    }
}
