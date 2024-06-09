package dev.heliosclient.module.modules.movement;

import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;

public class NoSlow extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    public BooleanSetting items = sgGeneral.add(new BooleanSetting.Builder()
            .name("Items")
            .description("Should item use slow you down?")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    public BooleanSetting cobWebs = sgGeneral.add(new BooleanSetting.Builder()
            .name("Cobwebs")
            .description("Should cobwebs slow you down?")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    public BooleanSetting soulSand = sgGeneral.add(new BooleanSetting.Builder()
            .name("SoulSand")
            .description("Should SoulSand slow you down?")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    public BooleanSetting honeyBlock = sgGeneral.add(new BooleanSetting.Builder()
            .name("Honey Block")
            .description("Should honeyBlocks slow you down?")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    public BooleanSetting slimeBlocks = sgGeneral.add(new BooleanSetting.Builder()
            .name("Slime Block")
            .description("Should slime blocks slow you down?")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    public BooleanSetting fluidDrag = sgGeneral.add(new BooleanSetting.Builder()
            .name("Fluid drag")
            .description("Should fluid push slow you down?")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    public BooleanSetting hunger = sgGeneral.add(new BooleanSetting.Builder()
            .name("Hunger")
            .description("Should hunger push slow you down?")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );


    public NoSlow() {
        super("NoSlow", "Removes slowness due to some actions or blocks", Categories.MOVEMENT);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());

    }

    public static NoSlow get() {
        return ModuleManager.get(NoSlow.class);
    }
}
