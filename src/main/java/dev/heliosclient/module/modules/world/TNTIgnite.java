package dev.heliosclient.module.modules.world;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.BlockUtils;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.player.BlockIterator;
import dev.heliosclient.util.player.InventoryUtils;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class TNTIgnite extends Module_ {
    boolean shouldSwitch = false;
    SettingGroup sgGeneral = new SettingGroup("General");

    DoubleSetting range = sgGeneral.add(new DoubleSetting.Builder()
            .name("Range")
            .onSettingChange(this)
            .defaultValue(3d)
            .range(0, 5d)
            .roundingPlace(0)
            .build()
    );
    BooleanSetting autoSwitch = sgGeneral.add(new BooleanSetting.Builder()
            .name("AutoSwitch")
            .description("Automatically switches to a flint and steel in hotbar")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );


    public TNTIgnite() {
        super("TNT-Ignite", "Automatically ignites nearby TNTs", Categories.WORLD);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());

    }

    @Override
    public void onDisable() {
        super.onDisable();
        shouldSwitch = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        BlockIterator iterator = new BlockIterator(mc.player, (int) range.value, (int) (range.value - 1));

        if (autoSwitch.value && shouldSwitch) {
            int flintAndSteelSlot = InventoryUtils.findItemInHotbar(Items.FLINT_AND_STEEL);

            if (flintAndSteelSlot != -1 && mc.player.getInventory().selectedSlot != flintAndSteelSlot) {
                mc.player.getInventory().selectedSlot = flintAndSteelSlot;
                mc.interactionManager.syncSelectedSlot();
                shouldSwitch = false;
            } else if (flintAndSteelSlot == -1) {
                ChatUtils.sendHeliosMsg("Flint and steel not found in hotbar, disabling...");
                toggle();
                return;
            }
        }

        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();

            if (mc.world.getBlockState(pos).getBlock() == Blocks.TNT) {
                if (shouldSwitch) {
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(pos.toCenterPos(), BlockUtils.getClosestFace(pos), pos, false));
                }
                shouldSwitch = true;
            }
        }
    }

}
