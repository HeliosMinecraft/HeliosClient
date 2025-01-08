package dev.heliosclient.module.modules.player;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.DropDownSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.settings.lists.ItemListSetting;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.blocks.BlockUtils;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.player.InventoryUtils;
import dev.heliosclient.util.player.PlayerUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;

import java.util.List;

public class AutoEat extends Module_ {
    boolean isEating;
    int bestFoodSlot = -1;


    SettingGroup sgGeneral = new SettingGroup("General");
    DropDownSetting eatReason = sgGeneral.add(new DropDownSetting.Builder()
            .name("Eat Reason")
            .description("The reason to start eating")
            .onSettingChange(this)
            .value(List.of(EatReason.values()))
            .defaultListOption(EatReason.Any)
            .addOptionToolTip("Starts eating if hunger drops below a certain threshold")
            .addOptionToolTip("Starts eating if health drops below a certain threshold")
            .addOptionToolTip("Starts eating if health and hunger, both drop below a certain threshold")
            .addOptionToolTip("Starts eating if either hunger or health drop below a certain threshold")
            .addOptionToolTip("Always munches on food")
            .build()
    );
    DoubleSetting healthThreshold = sgGeneral.add(new DoubleSetting.Builder()
            .name("Health Threshold")
            .description("Starts eating if your health drops below this level")
            .onSettingChange(this)
            .defaultValue(19d)
            .value(19d)
            .min(0)
            .max(32)
            .roundingPlace(0)
            .shouldRender(() -> eatReason.getOption() != EatReason.Hunger && eatReason.getOption() != EatReason.Always)
            .build()
    );
    DoubleSetting hungerThreshold = sgGeneral.add(new DoubleSetting.Builder()
            .name("Hunger Threshold")
            .description("Starts eating if your hunger drops below this level")
            .onSettingChange(this)
            .defaultValue(4d)
            .value(4d)
            .min(0)
            .max(20)
            .roundingPlace(0)
            .shouldRender(() -> eatReason.getOption() != EatReason.Health && eatReason.getOption() != EatReason.Always)
            .build()
    );
    BooleanSetting gapplePriority = sgGeneral.add(new BooleanSetting.Builder()
            .name("Golden Apple priority")
            .description("Eats enchanted golden apple or regular golden apple first instead of others")
            .onSettingChange(this)
            .defaultValue(true)
            .value(true)
            .build()
    );
    ItemListSetting blackListedFood = sgGeneral.add(new ItemListSetting.Builder()
            .name("BlackListed Food")
            .iSettingChange(this)
            .filter(item -> item.getComponents().contains(DataComponentTypes.FOOD))
            .items(Items.POISONOUS_POTATO,Items.ROTTEN_FLESH,Items.CHORUS_FRUIT,Items.PUFFERFISH, Items.SPIDER_EYE,Items.SUSPICIOUS_STEW)
            .build()
    );


    public AutoEat() {
        super("AutoEat", "Automatically eats food in hotbar when needed", Categories.PLAYER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        bestFoodSlot = -1;
        isEating = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if (mc.player.isCreative()) return;
        if (!isEating) {
            bestFoodSlot = findFoodSlot();
            if (bestFoodSlot == -1) {
                return;
            }
        }

        if (shouldEat()) {
            boolean swapped = InventoryUtils.swapToSlot(bestFoodSlot, true);
            if (!swapped) {
                ChatUtils.sendHeliosMsg(ColorUtils.red + "Could not swap to food slot!");
                return;
            }
            if (!mc.player.isUsingItem()) {
                PlayerUtils.doRightClick();
            }

            mc.options.useKey.setPressed(true);
            isEating = true;
        }

        // If we are eating, and we should not eat more, then stop eating or if the slot has no items, then be forced to switch to another slot
        if ((isEating && !shouldEat()) || mc.player.getInventory().getStack(bestFoodSlot) == null) {
            isEating = false;
            mc.options.useKey.setPressed(false);

            if(bestFoodSlot != InventoryUtils.OFFHAND)
               InventoryUtils.swapBackHotbar();
        }
    }

    public boolean shouldAvoid(Item item) {
        return blackListedFood.getSelectedEntries().contains(item);
    }

    public boolean shouldEat() {
        boolean eat = false;

        //We will open a container/inventory instead of eating so its better not to click
        if(mc.crosshairTarget instanceof BlockHitResult hr && BlockUtils.isClickable(mc.world.getBlockState(hr.getBlockPos()).getBlock())){
            return false;
        }

        switch ((EatReason) eatReason.getOption()) {
            case Health -> eat = mc.player.getHealth() + mc.player.getAbsorptionAmount() < healthThreshold.value;
            case Hunger -> eat = mc.player.getHungerManager().getFoodLevel() <= hungerThreshold.value;
            case Both ->
                    eat = mc.player.getHungerManager().getFoodLevel() <= hungerThreshold.value && mc.player.getHealth() + mc.player.getAbsorptionAmount() <= healthThreshold.value;
            case Any ->
                    eat = mc.player.getHungerManager().getFoodLevel() <= hungerThreshold.value || mc.player.getHealth() + mc.player.getAbsorptionAmount() <= healthThreshold.value;
            case Always -> eat = true;
        }

        return bestFoodSlot != -1 && eat;
    }

    public int findFoodSlot() {
        int maxHunger = 0;
        int bestSlot = -1;
        for (int slot = 0; slot < 9; slot++) {
            if (mc.player.getInventory().getStack(slot) == null) continue;

            Item item = mc.player.getInventory().getStack(slot).getItem();
            FoodComponent foodC = item.getComponents().get(DataComponentTypes.FOOD);
            if (foodC == null) continue;

            if (gapplePriority.value && item == Items.ENCHANTED_GOLDEN_APPLE) {
                return slot;
            } else if (gapplePriority.value && item == Items.GOLDEN_APPLE) {
                return slot;
            }

            if (foodC.nutrition() > maxHunger) {
                if (shouldAvoid(item)) continue;

                bestSlot = slot;
                maxHunger = foodC.nutrition();
            }
        }

        //Offhand
        Item item = mc.player.getInventory().getStack(InventoryUtils.OFFHAND).getItem();
        FoodComponent component = item.getComponents().get(DataComponentTypes.FOOD);
        if (component != null && component.nutrition() > maxHunger && !shouldAvoid(item)) {
            return InventoryUtils.OFFHAND;
        }

        return bestSlot;
    }


    public enum EatReason {
        Hunger,
        Health,
        Both,
        Any,
        Always
    }
}
