package dev.heliosclient.module.modules.player;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.mixin.AccessorClientPlayerInteractionManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.player.InventoryUtils;
import net.minecraft.util.math.BlockPos;

public class AutoTool extends Module_ {
    boolean hasSwapped = false;

    SettingGroup sgGeneral = new SettingGroup("General");
    BooleanSetting swapBack = sgGeneral.add(new BooleanSetting.Builder()
            .name("Swap back")
            .description("Swaps back to the slot you were orignally on when you stop breaking a block")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    BooleanSetting antiBreak = sgGeneral.add(new BooleanSetting.Builder()
            .name("AntiBreak")
            .description("Doesnt use a item if its about to break")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );

    public AutoTool() {
        super("AutoTool", "Switches to the fastest and best tool in the hotbar", Categories.PLAYER);
        addSettingGroup(sgGeneral);

        addQuickSetting(swapBack);

    }

    @Override
    public void onDisable() {
        super.onDisable();
        hasSwapped = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if (mc.player.isCreative() || mc.player.isSpectator())
            return;

        if (mc.interactionManager.isBreakingBlock()) {
            BlockPos blockBreakingPos = ((AccessorClientPlayerInteractionManager) mc.interactionManager).getCurrentBreakingBlockPos();
            int bestToolSlot = InventoryUtils.getFastestTool(mc.world.getBlockState(blockBreakingPos), antiBreak.value);

            if (bestToolSlot != -1) {
                InventoryUtils.swapToSlot(bestToolSlot, swapBack.value);
                hasSwapped = true;
            }
        } else if (swapBack.value && hasSwapped) {
            InventoryUtils.swapBackHotbar();
            hasSwapped = false;
        }
    }
}
