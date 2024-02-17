package dev.heliosclient.module.modules.render;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.ColorSetting;
import dev.heliosclient.module.settings.RGBASetting;
import dev.heliosclient.module.settings.SettingGroup;

public class GUI extends Module_ {
    public SettingGroup sgColors = new SettingGroup("Colors");
    public RGBASetting categoryPaneColors = sgColors.add(new RGBASetting.Builder()
            .name("CategoryPane Color")
            .description("Color of the category pane header")
            .value(ColorManager.INSTANCE.ClickGuiPrimary())
            .onSettingChange(this)
            .build()
    );
    public RGBASetting buttonColor = sgColors.add(new RGBASetting.Builder()
            .name("ModuleButton Color")
            .description("Color of the module button")
            .value(ColorManager.INSTANCE.ClickGuiPrimary())
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting categoryBorder = sgColors.add(new BooleanSetting.Builder()
            .name("Category Border")
            .description("Render the border around category")
            .value(false)
            .onSettingChange(this)
            .build()
    );


    public GUI() {
        super("GUI", "The HeliosClient GUI settings.", Categories.RENDER);
        active.value = true;

        addSettingGroup(sgColors);
    }

    @Override
    public void toggle() {
    }

    @Override
    public void onLoad() {
        showInModulesList.value = false;
    }
}
