package dev.heliosclient.module.modules.render;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.ui.clickgui.gui.PolygonMeshPatternRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GUI extends Module_ {
    public SettingGroup sgColors = new SettingGroup("Colors");
    public SettingGroup sgVisuals = new SettingGroup("Visuals");

    public CycleSetting ColorMode = sgColors.add(new CycleSetting.Builder()
            .name("Color Mode")
            .description("Color mode for GUI and parts of the client")
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
    public BooleanSetting textHighlight = sgColors.add(new BooleanSetting.Builder()
            .name("Module text highlight when active")
            .description("If this is on, then instead of the whole button, only the text will be highlighted with a glow if the module is on.")
            .value(false)
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
    public BooleanSetting syncCategoryIconColor = sgColors.add(new BooleanSetting.Builder()
            .name("Apply same to category Icons")
            .description("Renders the category icons with the same colors as chosen for GUI.")
            .value(false)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting displayModuleCount = sgVisuals.add(new BooleanSetting.Builder()
            .name("Display Module counts")
            .description("Displays module count in category pane for each category.")
            .value(false)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting bounceAnimation = sgVisuals.add(new BooleanSetting.Builder()
            .name("Animate while opening/closing")
            .description("Animates the GUI during opening and closing.")
            .value(false)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting background = sgVisuals.add(new BooleanSetting.Builder()
            .name("Background")
            .description("Draws a very faint background behind the clickGUI with the clickGUI primary color")
            .value(false)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting coolVisuals = sgVisuals.add(new BooleanSetting.Builder()
            .name("Render Cool Polygon mesh")
            .description("Renders lines and connecting dots that look good but may drain performance")
            .value(false)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting inChatHud = sgVisuals.add(new BooleanSetting.Builder()
            .name("Render the polygon mesh in chat hud")
            .value(false)
            .onSettingChange(this)
            .shouldRender(()->coolVisuals.value)
            .build()
    );
    public DoubleSetting polygonPoints = sgVisuals.add(new DoubleSetting.Builder()
            .name("Polygon Points")
            .description("Number of points")
            .min(10)
            .max(400)
            .value(75d)
            .defaultValue(75)
            .roundingPlace(0)
            .shouldRender(()->coolVisuals.value)
            .onSettingChange(this)
            .build()
    );
    public DoubleSetting dotRadius = sgVisuals.add(new DoubleSetting.Builder()
            .name("Dot Radius")
            .description("Radius of the dots")
            .min(0.1f)
            .max(3)
            .defaultValue(1.5d)
            .roundingPlace(1)
            .shouldRender(()->coolVisuals.value)
            .onSettingChange(this)
            .build()
    );
    public DoubleSetting lineMaxLength = sgVisuals.add(new DoubleSetting.Builder()
            .name("Line Max Length")
            .description("Max length of the lines to join to different points")
            .min(20f)
            .max(500)
            .defaultValue(75d)
            .roundingPlace(0)
            .shouldRender(()->coolVisuals.value)
            .onSettingChange(this)
            .build()
    );
    public DoubleSetting mouseForce = sgVisuals.add(new DoubleSetting.Builder()
            .name("Force Multiplier of the mouse")
            .description("The force multiplier applied by the mouse on the mesh so they move faster")
            .min(0)
            .max(5)
            .defaultValue(1.3)
            .roundingPlace(1)
            .shouldRender(()->coolVisuals.value)
            .onSettingChange(this)
            .build()
    );

    public GUI() {
        super("GUI", "The HeliosClient GUI settings.", Categories.RENDER);
        active.value = true;

        addSettingGroup(sgColors);
        addSettingGroup(sgVisuals);
        addQuickSettings(sgColors.getSettings());
        addQuickSettings(sgVisuals.getSettings());
    }

    @Override
    public void toggle() {
    }

    @Override
    public void onSettingChange(Setting<?> setting) {
        super.onSettingChange(setting);

            PolygonMeshPatternRenderer.INSTANCE.maskGradient = gradientType.get();
            PolygonMeshPatternRenderer.INSTANCE.radius = (float) dotRadius.value;
            PolygonMeshPatternRenderer.INSTANCE.MAX_DISTANCE = (float) lineMaxLength.value;
            PolygonMeshPatternRenderer.INSTANCE.MOUSE_FORCE_MULTIPLIER = mouseForce.getFloat();

            if(setting == polygonPoints){
                PolygonMeshPatternRenderer.INSTANCE.setNumOfPoints(polygonPoints.getInt());
            }
    }

    @Override
    public void onLoad() {
        showInModulesList.value = false;

        ColorManager.INSTANCE.onTick(null);

        PolygonMeshPatternRenderer.INSTANCE.maskGradient = gradientType.get();
        PolygonMeshPatternRenderer.INSTANCE.radius = (float) dotRadius.value;
        PolygonMeshPatternRenderer.INSTANCE.MAX_DISTANCE = (float) lineMaxLength.value;
        PolygonMeshPatternRenderer.INSTANCE.MOUSE_FORCE_MULTIPLIER = mouseForce.getFloat();
        PolygonMeshPatternRenderer.INSTANCE.setNumOfPoints(polygonPoints.getInt());
    }

    public static boolean coolVisuals(){
        return ModuleManager.get(GUI.class).coolVisuals.value;
    }
    public static boolean coolVisualsChatHud(){
        return ModuleManager.get(GUI.class).coolVisuals.value && ModuleManager.get(GUI.class).inChatHud.value;
    }
}
