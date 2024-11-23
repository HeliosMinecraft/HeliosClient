package dev.heliosclient.module.modules.world;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.block.BlockBreakEvent;
import dev.heliosclient.event.events.block.BlockInteractEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.network.NetworkUtils;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;

public class AntiGhostBlocks extends Module_ {

    private final SettingGroup sgGeneral = new SettingGroup("General");

    private final BooleanSetting placing = sgGeneral.add(new BooleanSetting.Builder()
            .name("Placing Check")
            .description("Checks placed blocks with the server")
            .defaultValue(true)
            .build()
    );
    private final BooleanSetting breaking = sgGeneral.add(new BooleanSetting.Builder()
            .name("Breaking Check")
            .description("Checks for ghost blocks when breaking")
            .defaultValue(true)
            .build()
    );

    public AntiGhostBlocks() {
        super("AntiGhostBlocks", "Attempts to prevent ghost blocks", Categories.WORLD);

        addSettingGroup(sgGeneral);
    }

    @SubscribeEvent
    public void onInteractBlock(BlockInteractEvent event) {
        if (placing.value && !mc.isInSingleplayer()) {
            event.cancel();
            NetworkUtils.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(event.getHand(), event.getHitResult(), id));
        }
    }

    @SubscribeEvent
    public void onBreakBlock(BlockBreakEvent event) {
        if (mc.isInSingleplayer() || !breaking.value) return;

        event.setCanceled(true);

        BlockState blockState = mc.world.getBlockState(event.getPos());
        blockState.getBlock().onBreak(mc.world, event.getPos(), blockState, mc.player);
    }

}
