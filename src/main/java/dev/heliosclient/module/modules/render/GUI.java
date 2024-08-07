package dev.heliosclient.module.modules.render;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.GradientManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.render.Renderer2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GUI extends Module_ {
    public SettingGroup sgColors = new SettingGroup("Colors");
    public CycleSetting ColorMode = sgColors.add(new CycleSetting.Builder()
            .name("Color Mode")
            .description("Color mode for parts of the client")
            .onSettingChange(this)
            .value(new ArrayList<>(List.of("Static", "Gradient")))
            .defaultListIndex(0)
            .build()
    );
    public RGBASetting staticColor = sgColors.add(new RGBASetting.Builder()
            .name("Color")
            .description("Simple single color for parts of the client")
            .onSettingChange(this)
            .value(new Color(ColorManager.INSTANCE.clickGuiSecondary))
            .defaultValue(new Color(ColorManager.INSTANCE.clickGuiSecondary))
            .shouldRender(() -> ColorMode.value == 0)
            .build()
    );
    public GradientSetting gradientType = sgColors.add(new GradientSetting.Builder()
            .name("Gradient")
            .description("Choose a gradient from the following.")
            .onSettingChange(this)
            .defaultValue("Rainbow")
            .shouldRender(() -> ColorMode.value == 1)
            .build()
    );
    /*
    public CycleSetting GradientType = sgColors.add(new CycleSetting.Builder()
            .name("Gradient Type")
            .description("Gradient type for the gradient color mode")
            .onSettingChange(this)
            .value(GradientManager.getAllGradientsNames().stream().toList())
            .defaultListIndex(0)
            .shouldRender(() -> ColorMode.value == 1)
            .build()
    );

     */
    public RGBASetting linear2Start = sgColors.add(new RGBASetting.Builder()
            .name("Linear-Start")
            .description("Linear Color Start of Linear mode")
            .onSettingChange(this)
            .value(Color.GREEN)
            .defaultValue(Color.GREEN)
            .shouldRender(() -> gradientType.isLinear2D() && ColorMode.value == 1)
            .build()
    );
    public RGBASetting linear2end = sgColors.add(new RGBASetting.Builder()
            .name("Linear-End")
            .description("Linear Color End of Linear mode")
            .onSettingChange(this)
            .value(Color.YELLOW)
            .shouldRender(() -> gradientType.isLinear2D() && ColorMode.value == 1)
            .defaultValue(Color.YELLOW)
            .build()
    );
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
    public RGBASetting clickGUIPrimary = sgColors.add(new RGBASetting.Builder()
            .name("ClickGUI Primary")
            .description("Primary color of the click gui followed across the client")
            .onSettingChange(this)
            .defaultValue(new Color(17, 18, 19, 100))
            .build()
    );
    public BooleanSetting categoryBorder = sgColors.add(new BooleanSetting.Builder()
            .name("Category Border")
            .description("Render the border around category")
            .value(false)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting syncCategoryIconColor = sgColors.add(new BooleanSetting.Builder()
            .name("Apply same to category Icons")
            .description("Renders the category icons with the same colors as chosen for GUI.")
            .value(false)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting displayModuleCount = sgColors.add(new BooleanSetting.Builder()
            .name("Display Module counts")
            .description("Displays module count in category pane for each category.")
            .value(false)
            .onSettingChange(this)
            .build()
    );

    public GUI() {
        super("GUI", "The HeliosClient GUI settings.", Categories.RENDER);
        active.value = true;

        addSettingGroup(sgColors);
        addQuickSettings(sgColors.getSettings());

    }

    @Override
    public void toggle() {
    }

    @Override
    public void onLoad() {
        showInModulesList.value = false;

        ColorManager.INSTANCE.onTick(null);
    }
}
