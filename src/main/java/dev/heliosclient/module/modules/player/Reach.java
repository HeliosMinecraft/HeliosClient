package dev.heliosclient.module.modules.player;

import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;

public class Reach extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");
    public DoubleSetting blockReach = sgGeneral.add(new DoubleSetting.Builder()
            .name("Block Reach")
            .description("Block interaction range")
            .onSettingChange(this)
            .defaultValue(4.5)
            .value(4.5)
            .min(0)
            .max(10)
            .roundingPlace(2)
            .build()
    );
    public DoubleSetting entityReach = sgGeneral.add(new DoubleSetting.Builder()
            .name("Entity Reach")
            .description("Entity interaction range")
            .onSettingChange(this)
            .defaultValue(5d)
            .value(5d)
            .min(0)
            .max(10)
            .roundingPlace(2)
            .build()
    );

    public Reach() {
        super("Reach", "Increase/Decrease your reach", Categories.PLAYER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

}
