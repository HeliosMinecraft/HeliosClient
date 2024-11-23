package dev.heliosclient.module.modules.movement;

import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;

public class TridentTweaker extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    public BooleanSetting alwaysRiptide = sgGeneral.add(new BooleanSetting.Builder()
            .name("Always Riptide")
            .description("Allows the trident to act as with riptide even if it doesn't have it")
            .onSettingChange(this)
            .value(false)
            .defaultValue(false)
            .build()
    );
    public BooleanSetting outOfWater = sgGeneral.add(new BooleanSetting.Builder()
            .name("Riptide out of water")
            .description("Allows the trident's riptide enchantment to work out of rain/water")
            .onSettingChange(this)
            .value(true)
            .defaultValue(false)
            .build()
    );
    public DoubleSetting velocityBoost = sgGeneral.add(new DoubleSetting.Builder()
            .name("Velocity Boost")
            .description("Boosts trident's riptide velocity")
            .onSettingChange(this)
            .value(1.0d)
            .defaultValue(1.0d)
            .min(0.1)
            .max(5.0)
            .roundingPlace(1)
            .build()
    );

    public TridentTweaker() {
        super("TridentTweaker", "Tweaks various settings of the trident", Categories.MOVEMENT);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }
}
