package dev.heliosclient.module.modules.player;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.DropDownSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.player.InventoryUtils;
import dev.heliosclient.util.player.PlayerUtils;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

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
            .name("Enchanted Golden apple priority")
            .description("Eats enchanted golden apple or regular golden apple first instead of others")
            .onSettingChange(this)
            .defaultValue(true)
            .value(true)
            .build()
    );
    BooleanSetting avoidPoisonous = sgGeneral.add(new BooleanSetting.Builder()
            .name("Avoid poisonous")
            .description("Avoids eating poisonous, chorus fruit or rotted food")
            .onSettingChange(this)
            .defaultValue(true)
            .value(true)
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
            InventoryUtils.swapBackHotbar();
        }
    }

    public boolean shouldAvoid(Item item) {
        return avoidPoisonous.value && (item == Items.CHORUS_FRUIT ||
                item == Items.POISONOUS_POTATO ||
                item == Items.PUFFERFISH ||
                item == Items.CHICKEN ||
                item == Items.ROTTEN_FLESH ||
                item == Items.SPIDER_EYE ||
                item == Items.SUSPICIOUS_STEW);
    }

    public boolean shouldEat() {
        boolean eat = false;
        switch ((EatReason) eatReason.getOption()) {
            case Health -> eat = mc.player.getHealth() < healthThreshold.value;
            case Hunger -> eat = mc.player.getHungerManager().getFoodLevel() <= hungerThreshold.value;
            case Both ->
                    eat = mc.player.getHungerManager().getFoodLevel() <= hungerThreshold.value && mc.player.getHealth() <= healthThreshold.value;
            case Any ->
                    eat = mc.player.getHungerManager().getFoodLevel() <= hungerThreshold.value || mc.player.getHealth() <= healthThreshold.value;
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
            FoodComponent foodC = item.getFoodComponent();
            if (foodC == null) continue;

            if (gapplePriority.value && item == Items.ENCHANTED_GOLDEN_APPLE) {
                return slot;
            } else if (gapplePriority.value && item == Items.GOLDEN_APPLE) {
                return slot;
            }

            if (foodC.getHunger() > maxHunger) {
                if (shouldAvoid(item)) continue;

                bestSlot = slot;
                maxHunger = foodC.getHunger();
            }
        }

        //Offhand
        Item item = mc.player.getInventory().getStack(45).getItem();
        FoodComponent component = item.getFoodComponent();
        if (component != null && item.getFoodComponent().getHunger() > maxHunger && !shouldAvoid(item)) {
            return 45;
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
