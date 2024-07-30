package dev.heliosclient.module.modules.world;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.block.BlockBreakEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;
import net.minecraft.block.BlockState;

public class AntiGhostBlocks extends Module_ {

    private final SettingGroup sgGeneral = new SettingGroup("General");

    private final BooleanSetting breaking = sgGeneral.add(new BooleanSetting.Builder()
            .name("Breaking Check")
            .description("Checks for ghost blocks while breaking ")
            .defaultValue(true)
            .build()
    );

    public AntiGhostBlocks() {
        super("AntiGhostBlocks", "Attempts to prevent ghost blocks", Categories.WORLD);

        addSettingGroup(sgGeneral);
    }

    @SubscribeEvent
    public void onBreakBlock(BlockBreakEvent event) {
        if (mc.isInSingleplayer() || !breaking.value) return;

        event.setCanceled(true);

        BlockState blockState = mc.world.getBlockState(event.getPos());
        blockState.getBlock().onBreak(mc.world, event.getPos(), blockState, mc.player);
    }

}
