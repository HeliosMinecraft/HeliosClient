package dev.heliosclient.module.sysmodules;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.events.client.FontChangeEvent;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.module.settings.buttonsetting.ButtonSetting;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.FileUtils;
import dev.heliosclient.util.InputBox;
import dev.heliosclient.util.animation.AnimationUtils;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.fontutils.FontUtils;
import dev.heliosclient.util.render.Renderer2D;
import me.x150.renderer.font.FontRenderer;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static dev.heliosclient.managers.FontManager.fontSize;
import static dev.heliosclient.managers.FontManager.fonts;

/**
 * Settings for the client and ClickGUI.
 */
public class ClickGUI extends Module_ {
    public static boolean pause = false;
    public static boolean keybinds = false;
    private final SettingGroup sgConfig = new SettingGroup("Config");
    public SettingGroup sgMisc = new SettingGroup("Misc");
    public SettingGroup sgTooltip = new SettingGroup("ToolTip");
    public SettingGroup sgGeneral = new SettingGroup("General");
    public SettingGroup sgSound = new SettingGroup("Sound");
    public SettingGroup sgExpert = new SettingGroup("Expert");


    public CycleSetting theme = sgMisc.add(new CycleSetting.Builder()
            .name("GUI Theme")
            .description("Theme for only the ClickGUI. Rounded GUI may cause frame drops.")
            .onSettingChange(this)
            .value(List.of(Theme.values()))
            .defaultListIndex(0)
            .build()
    );

    //Sorry guys, these aren't camel cased.

    public CycleSetting ScrollType = sgMisc.add(new CycleSetting.Builder()
            .name("Scrolling System")
            .description("Scrolling for the ClickGui")
            .onSettingChange(this)
            .value(List.of(ScrollTypes.values()))
            .defaultListIndex(0)
            .build()
    );
    public DoubleSetting CategoryHeight = sgMisc.add(new DoubleSetting.Builder()
            .name("CategoryPane Height")
            .description("CategoryPane Height for the ClickGUI")
            .onSettingChange(this)
            .defaultValue(230.0)
            .max(1000)
            .min(230.0)
            .roundingPlace(0)
            .shouldRender(() -> ScrollType.value == 1)
            .build()
    );
    public DoubleSetting ScrollSpeed = sgMisc.add(new DoubleSetting.Builder()
            .name("Scroll Speed")
            .description("Change your scroll speed for the ClickGUI")
            .onSettingChange(this)
            .defaultValue(7.0)
            .min(1)
            .max(20)
            .roundingPlace(1)
            .build()
    );
    public CycleSetting FontRenderer = sgMisc.add(new CycleSetting.Builder()
            .name("Font Renderer")
            .description("Font Rendering for the client")
            .onSettingChange(this)
            .value(new ArrayList<>(List.of("Custom", "Vanilla")))
            .defaultListIndex(0)
            .shouldRender(() -> true)
            .build()
    );
    public CycleSetting Font = sgMisc.add(new CycleSetting.Builder()
            .name("Font")
            .description("Font for the client")
            .onSettingChange(this)
            .value(FontManager.fontNames)
            .defaultListIndex(0)
            .shouldRender(() -> FontRenderer.value == 0)
            .build()
    );
    public DoubleSetting FontSize = sgMisc.add(new DoubleSetting.Builder()
            .name("Font Size")
            .description("Change your FontSize")
            .onSettingChange(this)
            .defaultValue(8.0)
            .min(1)
            .max(15)
            .roundingPlace(1)
            .shouldRender(() -> FontRenderer.value == 0)
            .build()
    );
    public DoubleSetting RainbowSpeed = sgMisc.add(new DoubleSetting.Builder()
            .name("Rainbow/Gradient speed")
            .description("Speed of the rainbow and gradients throughout the client")
            .onSettingChange(this)
            .value(14.0)
            .max(20)
            .min(1)
            .defaultValue(10.0)
            .roundingPlace(0)
            .build()
    );
    public DoubleSetting animationSpeed = sgMisc.add(new DoubleSetting.Builder()
            .name("Animation speed")
            .description("Speed of the animations in the GUI")
            .onSettingChange(this)
            .max(2)
            .min(0d)
            .defaultValue(0.5d)
            .roundingPlace(2)
            .build()
    );
    public BooleanSetting clickGUISound = sgSound.add(new BooleanSetting.Builder()
            .name("Play module toggle sound")
            .description("Play ClickGUI button sound on toggling modules")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .build()
    );

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
    public DoubleSetting tooltipSize = sgTooltip.add(new DoubleSetting.Builder()
            .name("Tooltip Size")
            .description("Change size of tooltips")
            .onSettingChange(this)
            .defaultValue(1d)
            .min(0)
            .max(3)
            .roundingPlace(1)
            .build()
    );
    public BooleanSetting ScreenHelp = sgGeneral.add(new BooleanSetting.Builder()
            .name("Show Keybind Help")
            .description("Show keybind help for HeliosClient screens.")
            .onSettingChange(this)
            .value(true)
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
    public BooleanSetting syncAccentColor = sgGeneral.add(new BooleanSetting.Builder()
            .name("Sync Accent color")
            .description("Syncs accent color for the entire client")
            .onSettingChange(this)
            .defaultValue(false)
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
    public KeyBind clickGUIKeyBind = sgConfig.add(new KeyBind.Builder()
            .name("ClickGUI bind")
            .description("The key to open the ClickGUI screen")
            .value(GLFW.GLFW_KEY_RIGHT_SHIFT)
            .defaultValue(GLFW.GLFW_KEY_RIGHT_SHIFT)
            .onSettingChange(this)
            .build()
    );
    public StringSetting configPath = sgConfig.add(new StringSetting.Builder()
            .name("Save Config Path")
            .description("Saves current config to that path. It only saves there for temporary purposes, otherwise it selects the default HeliosClient directory")
            .value(HeliosClient.CONFIG.otherConfigManager.getCurrentConfig().getConfigFile().getParent())
            .defaultValue(HeliosClient.CONFIG.otherConfigManager.getCurrentConfig().getConfigFile().getParent())
            .inputMode(InputBox.InputMode.ALL)
            .shouldSaveAndLoad(false)
            .characterLimit(300)
            .build()
    );
    public CycleSetting switchConfigs = sgConfig.add(new CycleSetting.Builder()
            .name("Switch Module Config")
            .description("Switch Module Configs")
            .value(HeliosClient.CONFIG.moduleConfigManager.getConfigNames())
            .defaultListIndex(0)
            .onSettingChange(this)
            .build()

    );
    public ButtonSetting config = sgConfig.add(new ButtonSetting.Builder()
            .name("Configs")
            .description("Reload or save Configs")
            .build()
    );

    public BooleanSetting disableEventSystem = sgExpert.add(new BooleanSetting.Builder()
            .name("Disable Event System")
            .description("Disables the client's event system. Warning: This will cause ALL modules and features of the client to stop working and may get out of sync. This option will also only be turned off again via the config")
            .onSettingChange(this)
            .value(false)
            .defaultValue(false)
            .build()
    );

    public ClickGUI() {
        super("ClickGUI", "ClickGui related stuff.", Categories.RENDER);

        addSettingGroup(sgMisc);
        addSettingGroup(sgGeneral);
        addSettingGroup(sgSound);
        addSettingGroup(sgTooltip);
        addSettingGroup(sgConfig);
        //addSettingGroup(sgExpert);


        active.value = true;

        configPath.setShouldSaveOrLoad(false);

        loadFonts.addButton("Load Fonts", 0, 0, () -> {
            FontManager.INSTANCE.refresh();
            Font.setOptions(FontManager.fontNames);

            HeliosClient.LOGGER.info("Reloaded fonts successfully!");
        });

        config.addButton("Reload All Configs", 0, 0, () -> {
            HeliosClient.CONFIG.getModuleConfigManager().checkDirectoryAgain();

            switchConfigs.options = HeliosClient.CONFIG.getModuleConfigManager().getConfigNames();
        });
        config.addButton("Save Config", 1, 0, () -> {
            File pathFile = new File(configPath.value);
            if (!pathFile.exists() || !pathFile.isDirectory() || !FileUtils.doesFileInPathExist(configPath.value)) {
                AnimationUtils.addErrorToast(ColorUtils.red + "Invalid Save Path. Path should be a valid directory", true, 2000);
                return;
            }
            pathFile = new File(configPath.value + "/" + switchConfigs.getOption().toString() + ".json");

            //So basically what we are doing here is storing the previous config file before.
            //Then we call the save method so all our module data gets written in the Map
            //And then we are changing the SubConfig target file to the new path
            //lastly we call the ConfigManager#save method to save the current config at the target location.
            File prevConfigFile = HeliosClient.CONFIG.moduleConfigManager.getCurrentConfig().getConfigFile();
            HeliosClient.CONFIG.writeConfigData();

            HeliosClient.CONFIG.moduleConfigManager.getCurrentConfig().setConfigFile(pathFile);
            boolean saveSuccessful = HeliosClient.CONFIG.moduleConfigManager.save(false);
            HeliosClient.CONFIG.moduleConfigManager.getCurrentConfig().setConfigFile(prevConfigFile);

            if(saveSuccessful) {
                AnimationUtils.addInfoToast(ColorUtils.green + "Config was saved successfully", true, 2000);
            }else{
                AnimationUtils.addErrorToast(ColorUtils.red + "Config could not be saved: ", true, 2000);
            }

        });
        config.addButton("Load Config", 1, 1, HeliosClient::loadModulesOnly);

        EventManager.register(this);
    }

    @Override
    public void onSettingChange(Setting<?> setting) {
        super.onSettingChange(setting);

        Tooltip.tooltip.mode = TooltipMode.value;
        Tooltip.tooltip.fixedPos = TooltipPos.value;

        Renderer2D.renderer = Renderer2D.Renderers.values()[FontRenderer.value];
        pause = Pause.value;
        keybinds = Keybinds.value;
        fontSize = ((int) FontSize.value);

        //Font changes
        if (HeliosClient.MC.getWindow() != null) {
            if (setting == FontRenderer || setting == FontSize || setting == loadFonts || setting == Font) {
                fonts = FontUtils.rearrangeFontsArray(FontManager.originalFonts, FontManager.originalFonts[Font.value]);
                FontRenderers.fontRenderer = new FontRenderer(fonts, fontSize);
                EventManager.postEvent(new FontChangeEvent(fonts));
            }

            if (setting == FontRenderer || setting == Font) {
                FontManager.INSTANCE.registerFonts();
            }
        }

        //Config changes
        if (setting == switchConfigs) {
            //Todo: Replace with cleaner config manager
            if (!HeliosClient.CONFIG.moduleConfigManager.getConfigNames().isEmpty()) {
                //Change the file name we want to load
                HeliosClient.CONFIG.moduleConfigManager.switchConfig(switchConfigs.getOption().toString(),true);
            }
        }
    }


    @Override
    public void onLoad() {
        Tooltip.tooltip.mode = TooltipMode.value;
        Tooltip.tooltip.fixedPos = TooltipPos.value;
        Renderer2D.renderer = Renderer2D.Renderers.values()[FontRenderer.value];

        ColorManager.INSTANCE.onTick(null);

        pause = Pause.value;
        keybinds = Keybinds.value;
        fontSize = ((int) FontSize.value);

        fonts = FontUtils.rearrangeFontsArray(FontManager.originalFonts, FontManager.originalFonts[Font.value]);

        FontManager.INSTANCE.registerFonts();
    }
    public int getAccentColor(){
        return AccentColor.getColor().getRGB();
    }

    @Override
    public void toggle() {
    }
    public Theme getTheme(){
        return (Theme) theme.getOption();
    }

    public enum Theme{
        Rounded,
        Rectangle
    }

    public enum ScrollTypes {
        OLD,
        NEW
    }
}
