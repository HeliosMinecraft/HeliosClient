package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.player.PostMovementUpdatePlayerEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.player.PlayerUtils;

public class TickShift extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    DoubleSetting ticksToShift = sgGeneral.add(new DoubleSetting.Builder()
            .name("Ticks to shift")
            .onSettingChange(this)
            .range(0, 1200)
            .roundingPlace(0)
            .defaultValue(10d)
            .build()
    );

    public TickShift() {
        super("TickShift", "Magically shifts you a number of ticks while you are standing", Categories.MOVEMENT);
        addSettingGroup(sgGeneral);
    }

    @SubscribeEvent
    public void postMovement(PostMovementUpdatePlayerEvent event) {
        if (PlayerUtils.isMoving(mc.player)) {
            ChatUtils.sendHeliosMsg("You are supposed to be standing still. Disabling...");
            toggle();
            return;
        }


        event.setCanceled(true);
        event.setNumberOfTicks(((int) ticksToShift.value));

        ChatUtils.sendHeliosMsg("TickShifted, disabling...");
        toggle();

    }
}
