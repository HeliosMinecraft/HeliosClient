package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.input.KeyboardInputEvent;
import dev.heliosclient.event.events.player.ClipAtLedgeEvent;
import dev.heliosclient.event.events.player.PlayerMotionEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.player.PlayerUtils;
import net.minecraft.client.input.KeyboardInput;

public class SafeWalk extends Module_ {
    private final SettingGroup sgGeneral = new SettingGroup("General");

    public BooleanSetting packet = sgGeneral.add(new BooleanSetting.Builder()
            .name("Packet")
            .description("Sends sneaking packets for legitimacy")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    BooleanSetting onEdge = sgGeneral.add(new BooleanSetting.Builder()
            .name("On Edge")
            .description("Will only place when you are on edge near a block")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    DoubleSetting edgeThreshold = sgGeneral.add(new DoubleSetting.Builder()
            .name("Edge Distance")
            .description("Distance from the edge to scaffold")
            .onSettingChange(this)
            .defaultValue(0.12d)
            .range(0, 0.2)
            .roundingPlace(3)
            .shouldRender(()->onEdge.value)
            .build()
    );

    public SafeWalk() {
        super("Safe Walk", "Automatically sneaks/stops near the edge of blocks to prevent you from falling", Categories.MOVEMENT);
        addSettingGroup(sgGeneral);
    }

    @SubscribeEvent
    public void onKeyInput(KeyboardInputEvent event) {
        if(onEdge.value && PlayerUtils.isPlayerAtEdge(edgeThreshold.value) && mc.player.isOnGround() && !mc.player.isSneaking()) {
            event.setNone();
        }
    }

    @SubscribeEvent
    public void onClip(ClipAtLedgeEvent event) {
        if(onEdge.value)return;

        event.setCanceled(true);
    }
}
