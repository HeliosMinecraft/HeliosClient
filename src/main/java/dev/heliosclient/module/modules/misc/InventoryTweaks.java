package dev.heliosclient.module.modules.misc;

import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;

public class InventoryTweaks extends Module_ {
    private final SettingGroup sgGeneral = new SettingGroup("General");
    private final SettingGroup sgAutomation = new SettingGroup("Automation");

    public BooleanSetting showStealDiscardButtons = sgAutomation.add(new BooleanSetting.Builder()
            .name("Show Steal/Discard Buttons")
            .description("Shows steal and discard buttons in container screens")
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    public DoubleSetting stealDiscardDelay = sgAutomation.add(new DoubleSetting.Builder()
            .name("StealDiscardDelay")
            .description("Delay between moving each item in milliseconds")
            .min(0)
            .max(1500)
            .roundingPlace(0)
            .defaultValue(250d)
            .onSettingChange(this)
            .build()
    );

    public InventoryTweaks() {
        super("InventoryTweaks","Tweaks various parts of your inventory", Categories.MISC);
        addSettingGroup(sgGeneral);
        addSettingGroup(sgAutomation);
    }
}
