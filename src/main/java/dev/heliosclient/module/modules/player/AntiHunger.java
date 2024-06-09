package dev.heliosclient.module.modules.player;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.event.events.player.PlayerJumpEvent;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.movement.AutoJump;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

public class AntiHunger extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    BooleanSetting suppressJumping = sgGeneral.add(new BooleanSetting.Builder()
            .name("Suppress Jumping")
            .description("Prevents you from jumping if your hunger is below a certain threshold to save hunger")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    DoubleSetting threshold = sgGeneral.add(new DoubleSetting.Builder()
            .name("Hunger Threshold")
            .description("Threshold for suppressing jumps")
            .onSettingChange(this)
            .defaultValue(4d)
            .value(4d)
            .min(0)
            .max(20)
            .roundingPlace(0)
            .build()
    );

    public AntiHunger() {
        super("AntiHunger", "Reduces hunger for you", Categories.PLAYER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @SubscribeEvent
    public void packetSendEvent(PacketEvent.SEND event) {

        if (mc.player == null || mc.player.hasVehicle() || mc.player.isTouchingWater() || mc.player.isSubmergedInWater())
            return;

        if (event.packet instanceof ClientCommandC2SPacket p && p.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void playerJump(PlayerJumpEvent event) {
        if (event.player == null || event.player != mc.player || mc.player.hasVehicle() || mc.player.isTouchingWater() || mc.player.isSubmergedInWater())
            return;

        if (suppressJumping.value && mc.player.getHungerManager().getFoodLevel() < threshold.value && !ModuleManager.get(AutoJump.class).isActive() && !mc.options.getAutoJump().getValue()) {
            event.setCanceled(true);
        }
    }

}
