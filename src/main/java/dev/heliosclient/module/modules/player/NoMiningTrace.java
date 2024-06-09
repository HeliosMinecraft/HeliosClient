package dev.heliosclient.module.modules.player;

import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;
import net.minecraft.item.PickaxeItem;

public class NoMiningTrace extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("Settings");

    BooleanSetting onlyWithPickaxe = sgGeneral.add(new BooleanSetting("Only With pickaxes", "Only works when you are holding a pickaxe", this, true));

    public NoMiningTrace() {
        super("NoMiningTrace", "Allows you to mine through entities", Categories.PLAYER);
        addSettingGroup(sgGeneral);
        addQuickSetting(onlyWithPickaxe);
    }

    public boolean shouldRemoveTrace() {
        if (!isActive()) {
            return false;
        }
        if (onlyWithPickaxe.value) {
            return mc.player.getMainHandStack().getItem() instanceof PickaxeItem || mc.player.getOffHandStack().getItem() instanceof PickaxeItem;
        }

        return true;
    }
}
