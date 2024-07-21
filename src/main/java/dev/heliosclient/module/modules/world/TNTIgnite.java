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
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.player.BlockIterator;
import dev.heliosclient.util.player.InventoryUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class TNTIgnite extends Module_ {
    boolean hasSwitched = false;
    int tickTimer = 0;
    SettingGroup sgGeneral = new SettingGroup("General");

    DoubleSetting range = sgGeneral.add(new DoubleSetting.Builder()
            .name("Range")
            .onSettingChange(this)
            .defaultValue(3d)
            .range(0, 5d)
            .roundingPlace(0)
            .build()
    );
    DoubleSetting tickDelay = sgGeneral.add(new DoubleSetting.Builder()
            .name("Tick Delay")
            .description("The delay between igniting next TNT")
            .onSettingChange(this)
            .defaultValue(1d)
            .range(0, 20d)
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
    BooleanSetting swapBack = sgGeneral.add(new BooleanSetting.Builder()
            .name("SwapBack")
            .description("Swaps back to previous slot, aka Silent Swapping")
            .onSettingChange(this)
            .defaultValue(false)
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
        hasSwitched = false;
        tickTimer = 0;

        if(swapBack.value)
         InventoryUtils.swapBackHotbar();

    }

    @Override
    public void onEnable() {
        super.onEnable();
        tickTimer = (int) tickDelay.value;
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        assert mc.player != null;

        if(tickTimer >= tickDelay.value) {
            boolean offHand = false;

            if(autoSwitch.value && hasSwitched){
                hasSwitched = InventoryUtils.findItemInHotbar(Items.FLINT_AND_STEEL) == mc.player.getInventory().selectedSlot;
            }
            if (autoSwitch.value && !hasSwitched) {
                int flintAndSteelSlot = InventoryUtils.findItemInHotbar(Items.FLINT_AND_STEEL);
                if(flintAndSteelSlot == PlayerInventory.OFF_HAND_SLOT){
                    offHand = true;
                }else {
                    if (flintAndSteelSlot != -1 && mc.player.getInventory().selectedSlot != flintAndSteelSlot) {
                        hasSwitched = InventoryUtils.swapToSlot(flintAndSteelSlot, swapBack.value);
                    } else if (flintAndSteelSlot == -1) {
                        ChatUtils.sendHeliosMsg(ColorUtils.darkRed +"Flint and Steel not found in hotbar, disabling...");
                        toggle();
                        return;
                    }
                }
            }
            BlockIterator iterator = new BlockIterator(mc.player, (int) range.value, (int) (range.value - 1));

            while (iterator.hasNext()) {
                BlockPos pos = iterator.next();

                if (mc.world.getBlockState(pos).getBlock() == Blocks.TNT) {
                    boolean handHasFlintSteel =  InventoryUtils.doesAnyHandStackHas(stack -> stack.getItem() == Items.FLINT_AND_STEEL);

                    if (hasSwitched && handHasFlintSteel) {
                        mc.interactionManager.interactBlock(mc.player,offHand ? Hand.OFF_HAND : Hand.MAIN_HAND, new BlockHitResult(pos.toCenterPos(), BlockUtils.getClosestFace(pos), pos, false));
                        //If a TNT has been ignited then it's not a block anymore,
                        // so we can break out safely without worrying of looping into the same TNT again.

                        if (tickDelay.value >  0) {
                            tickTimer = 0;
                            break;
                        }
                    }

                    if(swapBack.value) {
                        InventoryUtils.swapBackHotbar();
                        hasSwitched = false;
                    }

                }
            }
        }else{
            tickTimer++;
        }
    }
}
