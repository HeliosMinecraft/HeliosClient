package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.player.ClipAtLedgeEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;

public class SafeWalk extends Module_ {
    private final SettingGroup sgGeneral = new SettingGroup("General");

    public BooleanSetting packet = sgGeneral.add(new BooleanSetting.Builder()
            .name("Packet")
            .description("Sends sneaking packets for legitimacy")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );

    public SafeWalk() {
        super("Safe Walk", "Automatically sneaks/stops near the edge of blocks to prevent you from falling", Categories.MOVEMENT);
        addSettingGroup(sgGeneral);
    }

    @SubscribeEvent
    public void onClip(ClipAtLedgeEvent event) {
        event.setCanceled(true);
    }
}
