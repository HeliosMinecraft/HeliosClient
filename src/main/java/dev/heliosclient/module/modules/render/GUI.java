package dev.heliosclient.module.modules.render;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Category;
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
    public CycleSetting GradientType = sgColors.add(new CycleSetting.Builder()
            .name("Gradient Type")
            .description("Gradient type for the gradient color mode")
            .onSettingChange(this)
            .value(new ArrayList<>(List.of("Rainbow", "DaySky", "EveningSky", "NightSky", "Linear2D")))
            .defaultListIndex(0)
            .shouldRender(() -> ColorMode.value == 1)
            .build()
    );
    public RGBASetting linear2Start = sgColors.add(new RGBASetting.Builder()
            .name("Linear-Start")
            .description("Linear Color Start of Linear mode")
            .onSettingChange(this)
            .value(Color.GREEN)
            .defaultValue(Color.GREEN)
            .shouldRender(() -> GradientType.value == 4 && ColorMode.value == 1)
            .build()
    );
    public RGBASetting linear2end = sgColors.add(new RGBASetting.Builder()
            .name("Linear-End")
            .description("Linear Color End of Linear mode")
            .onSettingChange(this)
            .value(Color.YELLOW)
            .shouldRender(() -> GradientType.value == 4 && ColorMode.value == 1)
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
    public BooleanSetting categoryBorder = sgColors.add(new BooleanSetting.Builder()
            .name("Category Border")
            .description("Render the border around category")
            .value(false)
            .onSettingChange(this)
            .build()
    );
    private static GUI INSTANCE = new GUI();


    protected GUI() {
        super("GUI", "The HeliosClient GUI settings.", Categories.RENDER);
        active.value = true;

        GradientType.alsoRender(renderContext -> {
            Renderer2D.drawRoundedGradientRectangle(renderContext.drawContext().getMatrices().peek().getPositionMatrix(),ColorManager.INSTANCE.primaryGradientStart,ColorManager.INSTANCE.primaryGradientEnd,ColorManager.INSTANCE.primaryGradientEnd,ColorManager.INSTANCE.primaryGradientStart, renderContext.x() + 170,renderContext.y() + 2,20,15,2);
        });

        addSettingGroup(sgColors);
    }

    @Override
    public void toggle() {
    }

    @Override
    public void onLoad() {
        showInModulesList.value = false;
        if (ColorMode.value == 0) {
            ColorManager.INSTANCE.primaryGradientStart = staticColor.getColor();
            ColorManager.INSTANCE.primaryGradientEnd = staticColor.getColor();
        }
        if (ColorMode.value == 1) {
            switch (GradientType.value) {
                case 0 -> {
                    ColorManager.INSTANCE.primaryGradientStart = ColorUtils.getRainbowColor();
                    ColorManager.INSTANCE.primaryGradientEnd = ColorUtils.getRainbowColor2();
                }
                case 1, 2, 3 -> {
                    ColorManager.INSTANCE.primaryGradientStart = ColorUtils.getRainbowColor();
                    ColorManager.INSTANCE.primaryGradientEnd = ColorUtils.getRainbowColor();
                }
                case 4 -> {
                    ColorManager.INSTANCE.primaryGradientStart = linear2Start.getColor();
                    ColorManager.INSTANCE.primaryGradientEnd = linear2end.getColor();
                }
            }
        }
    }

    public static GUI get(){
        return INSTANCE;
    }
}
