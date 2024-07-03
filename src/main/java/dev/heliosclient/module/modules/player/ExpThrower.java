package dev.heliosclient.module.modules.player;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.player.InventoryUtils;
import dev.heliosclient.util.player.RotationUtils;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class ExpThrower extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    public BooleanSetting feetEXP = sgGeneral.add(new BooleanSetting.Builder()
            .name("FeetExp")
            .description("Throws xp on feet")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    public BooleanSetting swapBack = sgGeneral.add(new BooleanSetting.Builder()
            .name("SwapBack")
            .description("Swaps back to previous hotbar slot instantly. Effectively giving you silent xp thrower")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );

    public ExpThrower() {
        super("ExpThrower", "Automatically throws xp on activate", Categories.PLAYER);
        addSettingGroup(sgGeneral);

        addQuickSetting(feetEXP);

    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (swapBack.value)
            InventoryUtils.swapBackHotbar();
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        int slot = InventoryUtils.findItemInHotbar(Items.EXPERIENCE_BOTTLE);
        if (slot == -1) return;

        if (feetEXP.value) {
            RotationUtils.rotate(mc.player.getYaw(mc.getTickDelta()), 90, feetEXP.value, () -> {
                throwXP(slot);
            });
        } else {
            throwXP(slot);
        }
    }

    public void throwXP(int slot) {
        if (mc.player.getInventory().selectedSlot == slot) {
            mc.interactionManager.interactItem(mc.player, slot == 45 ? Hand.OFF_HAND : Hand.MAIN_HAND);
        } else {
            InventoryUtils.swapToSlot(slot, swapBack.value);
            mc.interactionManager.interactItem(mc.player, slot == 45 ? Hand.OFF_HAND : Hand.MAIN_HAND);

            if (swapBack.value)
                InventoryUtils.swapBackHotbar();
        }
    }
}
