package dev.heliosclient.module.modules.world;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.blocks.BlockIterator;
import dev.heliosclient.util.timer.TimerUtils;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.util.math.BlockPos;

public class BetterPortals extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    BooleanSetting portalGodMode = sgGeneral.add(new BooleanSetting.Builder()
            .name("Portal God mode")
            .description("Attempts the portal god mode exploit")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );

    TimerUtils packetConfirmTimer = new TimerUtils(true);

    public BetterPortals() {
        super("BetterPortals", "Allows you to use GUIs while in portals", Categories.WORLD);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.SEND event) {
        if (!portalGodMode.value) return;
        if (event.getPacket() instanceof TeleportConfirmC2SPacket && packetConfirmTimer.getElapsedTimeMS() < 5000) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if (!portalGodMode.value) return;

        BlockIterator iterator = new BlockIterator(mc.player, 2, 2);

        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();
            if (mc.world.getBlockState(pos).getBlock() == Blocks.NETHER_PORTAL)
                packetConfirmTimer.resetTimer();
        }
    }


}
