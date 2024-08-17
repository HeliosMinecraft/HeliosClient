package dev.heliosclient.module.modules.player;

import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;

public class NoBreakDelay extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    public DoubleSetting breakDelay = sgGeneral.add(new DoubleSetting.Builder()
            .name("Break Delay")
            .description("Modify your break cooldown")
            .range(0d, 5d)
            .defaultValue(0d)
            .value(0d)
            .roundingPlace(0)
            .build()
    );

    public NoBreakDelay() {
        super("NoBreakDelay", "Modifies your breaking cooldown", Categories.PLAYER);
        addSettingGroup(sgGeneral);
        addQuickSetting(breakDelay);
    }
}
