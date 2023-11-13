package dev.heliosclient.module.sysmodules;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.FontChangeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.module.settings.buttonsetting.ButtonSetting;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.fontutils.FontUtils;
import dev.heliosclient.util.fontutils.fxFontRenderer;
import me.x150.renderer.font.FontRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Setting for ClickGUI.
 */
public class ClickGUI extends Module_ {
    public static boolean pause = false;
    public static boolean keybinds = false;
    static ClickGUI INSTANCE = new ClickGUI();
    public static SettingGroup sgUI = new SettingGroup("UI");
    public static CycleSetting ScrollType = sgUI.add(new CycleSetting.Builder()
            .name("Scrolling System")
            .description("Scrolling for the ClickGui")
            .module(INSTANCE)
            .value(new ArrayList<>(List.of(ScrollTypes.values())))
            .listValue(0)
            .shouldRender(() -> true)
            .build()
    );
    public static DoubleSetting CategoryHeight = sgUI.add(new DoubleSetting.Builder()
            .name("CategoryPane Height")
            .description("CategoryPane Height for the ClickGUI")
            .module(INSTANCE)
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
            .module(INSTANCE)
            .value(7.0)
            .min(1)
            .max(8)
            .roundingPlace(1)
            .build()
    );
    public static CycleSetting FontRenderer = sgUI.add(new CycleSetting.Builder()
            .name("Font Renderer")
            .description("Font Rendering for the client")
            .module(INSTANCE)
            .value(new ArrayList<>(List.of("Custom", "Vanilla")))
            .listValue(0)
            .shouldRender(() -> true)
            .build()
    );
    public static CycleSetting Font = sgUI.add(new CycleSetting.Builder()
            .name("Font")
            .description("Font for the client")
            .module(INSTANCE)
            .value(FontManager.fontNames)
            .listValue(0)
            .shouldRender(() -> FontRenderer.value == 0)
            .build()
    );
    public static DoubleSetting FontSize = sgUI.add(new DoubleSetting.Builder()
            .name("Font Size")
            .description("Change your FontSize")
            .module(INSTANCE)
            .value(8.0)
            .min(1)
            .max(15)
            .roundingPlace(1)
            .shouldRender(() -> FontRenderer.value == 0)
            .build()
    );
    public static DoubleSetting RainbowSpeed = sgUI.add(new DoubleSetting.Builder()
            .name("Rainbow speed")
            .description("Speed of the rainbow throughout the client")
            .module(ClickGUI.INSTANCE)
            .value(7.0)
            .max(20)
            .min(1)
            .defaultValue(8.0)
            .roundingPlace(0)
            .build()
    );
    public SettingGroup sgTooltip = new SettingGroup("ToolTip");
    public SettingGroup sgGeneral = new SettingGroup("General");
    public CycleSetting TooltipMode = sgTooltip.add(new CycleSetting.Builder()
            .name("Tooltip mode")
            .description("Mode in what tooltips should be shown.")
            .module(INSTANCE)
            .value(new ArrayList<String>(List.of("Normal", "Fixed", "Vanilla")))
            .listValue(0)
            .shouldRender(() -> true)
            .build()
    );
    public CycleSetting TooltipPos = sgTooltip.add(new CycleSetting.Builder()
            .name("Tooltip position")
            .description("Position of fixed tooltip.")
            .module(INSTANCE)
            .value(new ArrayList<>(List.of("Top-left", "Top-right", "Bottom-left", "Bottom-right", "Center")))
            .listValue(3)
            .shouldRender(() -> TooltipMode.value == 1)
            .build()
    );
    ButtonSetting loadFonts = sgGeneral.add(new ButtonSetting.Builder()
            .name("Font")
            .build()
    );
    BooleanSetting Pause = sgGeneral.add(new BooleanSetting.Builder()
            .name("Pause game")
            .description("Pause the game when Click GUI is on.")
            .module(this)
            .value(false)
            .build()
    );
    BooleanSetting Keybinds = sgGeneral.add(new BooleanSetting.Builder()
            .name("Show Keybinds")
            .description("Show keybinds in the Module Button.")
            .module(this)
            .value(true)
            .build()
    );
    RGBASetting AccentColor = sgGeneral.add(new RGBASetting.Builder()
            .name("Accent color")
            .description("Accent color of Click GUI.")
            .module(this)
            .value(new Color(ColorManager.INSTANCE.clickGuiSecondary))
            .defaultValue(new Color(ColorManager.INSTANCE.clickGuiSecondary))
            .build()
    );
    RGBASetting PaneTextColor = sgGeneral.add(new RGBASetting.Builder()
            .name("Category pane text color")
            .description("Color of pane text.")
            .module(this)
            .value(new Color(ColorManager.INSTANCE.clickGuiPaneText))
            .defaultValue(new Color(ColorManager.INSTANCE.clickGuiPaneText))
            .build()
    );
    RGBASetting TextColor = sgGeneral.add(new RGBASetting.Builder()
            .name("Text color")
            .description("Color of text all through out the client.")
            .module(this)
            .value(new Color(ColorManager.INSTANCE.defaultTextColor))
            .defaultValue(new Color(ColorManager.INSTANCE.defaultTextColor))
            .build()
    );

    public ClickGUI() {
        super("ClickGUI", "ClickGui related stuff.", Categories.RENDER);

        addSettingGroup(sgUI);
        addSettingGroup(sgGeneral);
        addSettingGroup(sgTooltip);

        active.value = true;
        loadFonts.addButton("Load Fonts", 0, 0, () -> {
            FontManager.INSTANCE = new FontManager();
            Font.setOptions(FontManager.fontNames);
        });

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

        FontManager.fonts = FontUtils.rearrangeFontsArray(FontManager.Originalfonts, FontManager.Originalfonts[Font.value]);
        FontManager.fontRenderer = new FontRenderer(FontManager.fonts, FontManager.fontSize);
        FontManager.fxfontRenderer = new fxFontRenderer(FontManager.fonts, 8f);
        EventManager.postEvent(new FontChangeEvent(FontManager.fonts));
    }

    @SubscribeEvent
    @Override
    public void onTick(TickEvent event) {
        super.onTick(event);
        ColorManager.INSTANCE.clickGuiSecondaryAlpha = AccentColor.getColor().getAlpha();
        ColorManager.INSTANCE.clickGuiSecondary = AccentColor.getColor().getRGB();
        ColorManager.INSTANCE.clickGuiSecondaryRainbow = AccentColor.isRainbow();

        ColorManager.INSTANCE.defaultTextColor = TextColor.getColor().getRGB();

        ColorManager.INSTANCE.clickGuiPaneTextAlpha = PaneTextColor.getColor().getAlpha();
        ColorManager.INSTANCE.clickGuiPaneText = PaneTextColor.getColor().getRGB();
        ColorManager.INSTANCE.clickGuiPaneTextRainbow = PaneTextColor.isRainbow();
    }

    @Override
    public void onLoad() {
        super.onLoad();
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

        pause = Pause.value;
        keybinds = Keybinds.value;

        FontManager.fonts = FontUtils.rearrangeFontsArray(FontManager.Originalfonts, FontManager.Originalfonts[Font.value]);
        FontManager.fontRenderer = new FontRenderer(FontManager.fonts, FontManager.fontSize);
        FontManager.fxfontRenderer = new fxFontRenderer(FontManager.fonts, 8f);
        EventManager.postEvent(new FontChangeEvent(FontManager.fonts));
    }

    @Override
    public void toggle() {
    }

    public enum ScrollTypes {
        OLD,
        NEW
    }
}
