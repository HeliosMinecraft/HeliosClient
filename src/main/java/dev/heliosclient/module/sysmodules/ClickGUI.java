package dev.heliosclient.module.sysmodules;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.events.client.FontChangeEvent;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.managers.NavBarManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.module.settings.buttonsetting.ButtonSetting;
import dev.heliosclient.system.Config;
import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.ui.clickgui.navbar.NavBarItem;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.FileUtils;
import dev.heliosclient.util.InputBox;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.animation.AnimationUtils;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.fontutils.FontUtils;
import me.x150.renderer.font.FontRenderer;
import net.minecraft.client.MinecraftClient;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static dev.heliosclient.managers.FontManager.fontSize;
import static dev.heliosclient.managers.FontManager.fonts;

/**
 * Setting for ClickGUI.
 */
public class ClickGUI extends Module_ {
    public static boolean pause = false;
    public static boolean keybinds = false;
    public static SettingGroup sgUI = new SettingGroup("UI");
    public static ClickGUI INSTANCE = new ClickGUI();
    public static CycleSetting ScrollType = sgUI.add(new CycleSetting.Builder()
            .name("Scrolling System")
            .description("Scrolling for the ClickGui")
            .onSettingChange(INSTANCE)
            .value(new ArrayList<>(List.of(ScrollTypes.values())))
            .defaultListIndex(0)
            .shouldRender(() -> true)
            .build()
    );
    public static DoubleSetting CategoryHeight = sgUI.add(new DoubleSetting.Builder()
            .name("CategoryPane Height")
            .description("CategoryPane Height for the ClickGUI")
            .onSettingChange(INSTANCE)
            .value(150.0)
            .max(1000)
            .min(25)
            .roundingPlace(0)
            .shouldRender(() -> ScrollType.value == 1)
            .build()
    );
    public static DoubleSetting ScrollSpeed = sgUI.add(new DoubleSetting.Builder()
            .name("Scroll Speed")
            .description("Change your scroll speed for the ClickGUI")
            .onSettingChange(INSTANCE)
            .value(7.0)
            .min(1)
            .max(8)
            .roundingPlace(1)
            .build()
    );
    public static CycleSetting FontRenderer = sgUI.add(new CycleSetting.Builder()
            .name("Font Renderer")
            .description("Font Rendering for the client")
            .onSettingChange(INSTANCE)
            .value(new ArrayList<>(List.of("Custom", "Vanilla")))
            .defaultListIndex(0)
            .shouldRender(() -> true)
            .build()
    );
    public static CycleSetting Font = sgUI.add(new CycleSetting.Builder()
            .name("Font")
            .description("Font for the client")
            .onSettingChange(INSTANCE)
            .value(FontManager.fontNames)
            .defaultListIndex(0)
            .shouldRender(() -> FontRenderer.value == 0)
            .build()
    );
    public static DoubleSetting FontSize = sgUI.add(new DoubleSetting.Builder()
            .name("Font Size")
            .description("Change your FontSize")
            .onSettingChange(INSTANCE)
            .value(8.0)
            .min(1)
            .max(15)
            .roundingPlace(1)
            .shouldRender(() -> FontRenderer.value == 0)
            .build()
    );
    public static DoubleSetting RainbowSpeed = sgUI.add(new DoubleSetting.Builder()
            .name("Rainbow/Gradient speed")
            .description("Speed of the rainbow and gradients throughout the client")
            .onSettingChange(ClickGUI.INSTANCE)
            .value(14.0)
            .max(20)
            .min(1)
            .defaultValue(10.0)
            .roundingPlace(0)
            .build()
    );
    private final SettingGroup sgConfig = new SettingGroup("Config");
    public StringSetting configPath = sgConfig.add(new StringSetting.Builder()
            .name("Save Config Path")
            .description("Saves current config to that path. It only saves there for temporary purposes, otherwise it selects the default HeliosClient directory")
            .value(HeliosClient.CONFIG.configManager.getConfigDir().getAbsolutePath())
            .inputMode(InputBox.InputMode.ALL)
            .characterLimit(300)
            .defaultValue(HeliosClient.CONFIG.configManager.getConfigDir().getAbsolutePath())
            .build()
    );
    public DropDownSetting switchConfigs = sgConfig.add(new DropDownSetting.Builder()
            .name("Switch Module Config")
            .defaultListIndex(0)
            .description("Switch Module Configs")
            .value(HeliosClient.CONFIG.MODULE_CONFIGS)
            .onSettingChange(this)
            .build()

    );
    public ButtonSetting config = sgConfig.add(new ButtonSetting.Builder()
            .name("Configs")
            .description("Reload or save Configs")
            .build()
    );
    public SettingGroup sgTooltip = new SettingGroup("ToolTip");
    public SettingGroup sgGeneral = new SettingGroup("General");
    public CycleSetting TooltipMode = sgTooltip.add(new CycleSetting.Builder()
            .name("Tooltip mode")
            .description("Mode in what tooltips should be shown.")
            .onSettingChange(this)
            .value(new ArrayList<String>(List.of("Normal", "Fixed", "Vanilla")))
            .defaultListIndex(0)
            .shouldRender(() -> true)
            .build()
    );
    public CycleSetting TooltipPos = sgTooltip.add(new CycleSetting.Builder()
            .name("Tooltip position")
            .description("Position of fixed tooltip.")
            .onSettingChange(this)
            .value(new ArrayList<>(List.of("Top-left", "Top-right", "Bottom-left", "Bottom-right", "Center")))
            .defaultListIndex(3)
            .shouldRender(() -> TooltipMode.value == 1)
            .build()
    );
    public BooleanSetting ScreenHelp = sgGeneral.add(new BooleanSetting.Builder()
            .name("Show keybind Help")
            .description("Show keybind Help for client screens.")
            .onSettingChange(this)
            .value(true)
            .build()
    );
    public SettingGroup sgRender = new SettingGroup("Render");
    public CycleSetting ColorMode = sgRender.add(new CycleSetting.Builder()
            .name("Color Mode")
            .description("Color mode for parts of the client")
            .onSettingChange(this)
            .value(new ArrayList<>(List.of("Static", "Gradient")))
            .defaultListIndex(0)
            .build()
    );
    public RGBASetting staticColor = sgRender.add(new RGBASetting.Builder()
            .name("Color")
            .description("Simple single color for parts of the client")
            .onSettingChange(this)
            .value(new Color(ColorManager.INSTANCE.clickGuiSecondary))
            .defaultValue(new Color(ColorManager.INSTANCE.clickGuiSecondary))
            .shouldRender(() -> ColorMode.value == 0)
            .build()
    );
    public CycleSetting GradientType = sgRender.add(new CycleSetting.Builder()
            .name("Gradient Type")
            .description("Gradient type for the gradient color mode")
            .onSettingChange(this)
            .value(new ArrayList<>(List.of("Rainbow", "DaySky", "EveningSky", "NightSky", "Linear2D")))
            .defaultListIndex(0)
            .shouldRender(() -> ColorMode.value == 1)
            .build()
    );
    public RGBASetting linear2Start = sgRender.add(new RGBASetting.Builder()
            .name("Linear-Start")
            .description("Linear Color Start of Linear mode")
            .onSettingChange(this)
            .value(Color.GREEN)
            .defaultValue(Color.GREEN)
            .shouldRender(() -> GradientType.value == 4 && ColorMode.value == 1)
            .build()
    );
    public RGBASetting linear2end = sgRender.add(new RGBASetting.Builder()
            .name("Linear-End")
            .description("Linear Color End of Linear mode")
            .onSettingChange(this)
            .value(Color.YELLOW)
            .shouldRender(() -> GradientType.value == 4 && ColorMode.value == 1)
            .defaultValue(Color.YELLOW)
            .build()
    );
    public RGBASetting AccentColor = sgGeneral.add(new RGBASetting.Builder()
            .name("Accent color")
            .description("Accent color of Click GUI.")
            .onSettingChange(this)
            .value(new Color(ColorManager.INSTANCE.clickGuiSecondary))
            .defaultValue(new Color(ColorManager.INSTANCE.clickGuiSecondary))
            .build()
    );
    public RGBASetting PaneTextColor = sgGeneral.add(new RGBASetting.Builder()
            .name("Category pane text color")
            .description("Color of pane text.")
            .onSettingChange(this)
            .value(new Color(ColorManager.INSTANCE.clickGuiPaneText))
            .defaultValue(new Color(ColorManager.INSTANCE.clickGuiPaneText))
            .build()
    );
    public RGBASetting TextColor = sgGeneral.add(new RGBASetting.Builder()
            .name("Text color")
            .description("Color of text all through out the client.")
            .onSettingChange(this)
            .value(new Color(ColorManager.INSTANCE.defaultTextColor))
            .defaultValue(new Color(ColorManager.INSTANCE.defaultTextColor))
            .build()
    );
    ButtonSetting loadFonts = sgGeneral.add(new ButtonSetting.Builder()
            .name("Font")
            .build()
    );
    BooleanSetting Pause = sgGeneral.add(new BooleanSetting.Builder()
            .name("Pause game")
            .description("Pause the game when Click GUI is on.")
            .onSettingChange(this)
            .value(false)
            .build()
    );
    BooleanSetting Keybinds = sgGeneral.add(new BooleanSetting.Builder()
            .name("Show Keybinds")
            .description("Show keybinds in the Module Button.")
            .onSettingChange(this)
            .value(true)
            .build()
    );

    public ClickGUI() {
        super("ClickGUI", "ClickGui related stuff.", Categories.RENDER);

        addSettingGroup(sgUI);
        addSettingGroup(sgRender);
        addSettingGroup(sgGeneral);
        addSettingGroup(sgTooltip);
        addSettingGroup(sgConfig);

        active.value = true;
        configPath.setShouldSaveOrLoad(false);

        loadFonts.addButton("Load Fonts", 0, 0, () -> {
            FontManager.INSTANCE.refresh();
            Font.setOptions(FontManager.fontNames);

            HeliosClient.LOGGER.info("Reloaded fonts successfully!");
        });

        config.addButton("Reload All Configs", 0, 0, () -> {
            HeliosClient.CONFIG.init();
            switchConfigs.options = HeliosClient.CONFIG.MODULE_CONFIGS;
            int var = switchConfigs.value;
            HeliosClient.loadConfig();
            switchConfigs.value = var;
        });
        config.addButton("Save Config", 1, 0,()-> {
            File pathFile = new File(configPath.value);
            if(!pathFile.exists() || !pathFile.isDirectory() || !FileUtils.doesFileInPathExist(configPath.value)){
                AnimationUtils.addErrorToast(ColorUtils.red + "Invalid Save Path. Path should be a valid directory", true, 2000);
                return;
            }
            HeliosClient.CONFIG.configManager.setConfigDir(pathFile);
            HeliosClient.saveConfig();
        });
        config.addButton("Load Config", 1, 1, HeliosClient::loadConfig);

        EventManager.register(this);
    }

    @Override
    public void onSettingChange(Setting setting) {
        super.onSettingChange(setting);

        Tooltip.tooltip.mode = TooltipMode.value;
        Tooltip.tooltip.fixedPos = TooltipPos.value;

        Renderer2D.renderer = Renderer2D.Renderers.values()[FontRenderer.value];
        pause = Pause.value;
        keybinds = Keybinds.value;

        if (setting == FontRenderer || setting == FontSize || setting == loadFonts || setting == Font) {
            fonts = FontUtils.rearrangeFontsArray(FontManager.Originalfonts, FontManager.Originalfonts[Font.value]);
            FontRenderers.fontRenderer = new FontRenderer(fonts, fontSize);
            EventManager.postEvent(new FontChangeEvent(fonts));
        }

        if (setting == FontRenderer || setting == Font) {
            FontManager.INSTANCE.registerFonts();
        }

        if (setting == switchConfigs) {
            HeliosClient.saveConfig();
            Config.MODULES = HeliosClient.CONFIG.MODULE_CONFIGS.get(switchConfigs.value).replace(".toml", "");
            HeliosClient.loadConfig();
            for (NavBarItem item : NavBarManager.INSTANCE.navBarItems) {
                if (item.name.equalsIgnoreCase("ClickGUI")) {
                    item.target = ClickGUIScreen.INSTANCE;
                }
            }
            EventManager.postEvent(new FontChangeEvent(fonts));
        }
    }


    @Override
    public void onLoad() {
        Tooltip.tooltip.mode = TooltipMode.value;
        Tooltip.tooltip.fixedPos = TooltipPos.value;
        Renderer2D.renderer = Renderer2D.Renderers.values()[FontRenderer.value];

        ColorManager.INSTANCE.clickGuiSecondaryAlpha = AccentColor.getColor().getAlpha();
        ColorManager.INSTANCE.clickGuiSecondary = AccentColor.getColor().getRGB();
        ColorManager.INSTANCE.clickGuiSecondaryRainbow = AccentColor.isRainbow();

        ColorManager.INSTANCE.defaultTextColor = TextColor.getColor().getRGB();

        ColorManager.INSTANCE.clickGuiPaneTextAlpha = PaneTextColor.getColor().getAlpha();
        ColorManager.INSTANCE.clickGuiPaneText = PaneTextColor.getColor().getRGB();
        ColorManager.INSTANCE.clickGuiPaneTextRainbow = PaneTextColor.isRainbow();

        ColorManager.INSTANCE.clickGuiPaneTextAlpha = PaneTextColor.getColor().getAlpha();
        ColorManager.INSTANCE.clickGuiPaneText = PaneTextColor.value.getRGB();
        ColorManager.INSTANCE.clickGuiPaneTextRainbow = PaneTextColor.isRainbow();

        pause = Pause.value;
        keybinds = Keybinds.value;

        fonts = FontUtils.rearrangeFontsArray(FontManager.Originalfonts, FontManager.Originalfonts[Font.value]);

        if (MinecraftClient.getInstance().getWindow() != null)
            FontManager.INSTANCE.registerFonts();

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

    @Override
    public void toggle() {
    }

    public enum ScrollTypes {
        OLD,
        NEW
    }
}
