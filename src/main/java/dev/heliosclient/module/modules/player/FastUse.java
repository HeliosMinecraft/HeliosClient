package dev.heliosclient.module.modules.player;

import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class FastUse extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    public BooleanSetting avoidFireworks = sgGeneral.add(new BooleanSetting.Builder()
            .name("Avoid Fireworks")
            .description("Prevents FastUse from removing firework cooldown to prevent useless spam")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );

    public DoubleSetting coolDown = sgGeneral.add(new DoubleSetting.Builder()
            .name("Use Cooldown")
            .description("Use cooldown (in ticks)")
            .onSettingChange(this)
            .defaultValue(0d)
            .value(0d)
            .min(0)
            .max(4)
            .roundingPlace(0)
            .build()
    );


    public FastUse() {
        super("FastUse", "Reduces/removes item use cooldowns", Categories.PLAYER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    public int getCoolDown(ItemStack stack) {
        if (avoidFireworks.value && stack.getItem() == Items.FIREWORK_ROCKET) {
            return 4;
        }

        return (int) coolDown.value;
    }
}
