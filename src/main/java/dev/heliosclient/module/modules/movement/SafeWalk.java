package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.player.ClipAtLedgeEvent;
import dev.heliosclient.event.events.player.PlayerMotionEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.player.MovementUtils;
import net.minecraft.util.math.Vec2f;

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
            .description("Will automatically prevent you from walking over the edge, like sneaking, but without sneaking")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    DoubleSetting edgeThreshold = sgGeneral.add(new DoubleSetting.Builder()
            .name("Edge Distance")
            .description("Distance from the edge to safe-walk")
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
    public void onPlayerMove(PlayerMotionEvent event) {
        if(onEdge.value && MovementUtils.isPressingMovementButton() && mc.player.isOnGround()) {
            Vec2f vec = MovementUtils.performSafeMovement(event.getMovement().x, event.getMovement().z,edgeThreshold.value);
            event.modifyMovement().heliosClient$setXZ(vec.x,vec.y);
        }
    }

    @SubscribeEvent
    public void onClip(ClipAtLedgeEvent event) {
        if(onEdge.value)return;

        event.setCanceled(true);
    }
}
