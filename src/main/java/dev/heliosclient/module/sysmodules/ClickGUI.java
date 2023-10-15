package dev.heliosclient.module.sysmodules;

import dev.heliosclient.event.events.FontChangeEvent;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.fontutils.FontUtils;
import dev.heliosclient.util.fontutils.fxFontRenderer;
import me.x150.renderer.font.FontRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Setting for ClickGUI.
 */
public class ClickGUI extends Module_ {
    public static boolean pause = false;
    public static boolean keybinds = false;
    static ClickGUI INSTANCE = new ClickGUI();
    public static DoubleSetting ScrollSpeed = new DoubleSetting("Scroll Speed", "Change your scroll speed for the ClickGUI", INSTANCE, 7, 1, 8, 1);
    BooleanSetting Pause = new BooleanSetting("Pause game", "Pause the game when Click GUI is on.", this, false);
    BooleanSetting Keybinds = new BooleanSetting("Show Keybinds", "Show keybinds in the Module Button.", this, true);
    ColorSetting AccentColor = new ColorSetting("Accent color", "Accent color of Click GUI.", this, ColorManager.INSTANCE.clickGuiSecondary);
    BooleanSetting RainbowAccent = new BooleanSetting("Rainbow", "Rainbow effect for accent color.", this, false);
    BooleanSetting RainbowPane = new BooleanSetting("Rainbow", "Rainbow effect for category panes.", this, false);
    ColorSetting PaneTextColor = new ColorSetting("Category pane text color", "Color of pane text.", this, ColorManager.INSTANCE.clickGuiPaneText);
    ColorSetting TextColor = new ColorSetting("Text color", "Color of text all through out the client.", this, ColorManager.INSTANCE.defaultTextColor);
    CycleSetting TooltipMode = new CycleSetting("Tooltip mode", "Mode in what tooltips should be shown.", this, new ArrayList<String>(List.of("Normal", "Fixed", "Vanilla")), 0);
    CycleSetting TooltipPos = new CycleSetting("Tooltip position", "Position of fixed tooltip.", this, new ArrayList<>(List.of("Top-left", "Top-right", "Bottom-left", "Bottom-right", "Center")), 3) {
        @Override
        public boolean shouldRender() {
            return TooltipMode.value == 1;
        }
    };
    public static CycleSetting FontRenderer = new CycleSetting("Font Renderer", "Font Rendering for the client", ClickGUI.INSTANCE, new ArrayList<>(List.of("Custom", "Vanilla")), 0);
    public static CycleSetting Font = new CycleSetting("Font", "Font for the client", ClickGUI.INSTANCE, FontManager.fontNames, 0) {
        @Override
        public boolean shouldRender() {
            return FontRenderer.value == 0;
        }
    };
    public static DoubleSetting FontSize = new DoubleSetting("Font Size", "Change your FontSize", INSTANCE, 8, 1, 15, 1) {
        @Override
        public boolean shouldRender() {
            return FontRenderer.value == 0;
        }
    };
    ButtonSetting loadFonts = new ButtonSetting("Font");

    public ClickGUI() {
        super("ClickGUI", "ClickGui related stuff.", Category.RENDER);
        settings.add(Pause);
        settings.add(Keybinds);
        settings.add(TooltipMode);
        settings.add(TooltipPos);
        settings.add(ScrollSpeed);
        settings.add(FontRenderer);
        settings.add(Font);
        settings.add(FontSize);
        settings.add(loadFonts);

        settings.add(AccentColor);
        settings.add(RainbowAccent);
        settings.add(PaneTextColor);
        settings.add(RainbowPane);
        settings.add(TextColor);

        quickSettings.add(Pause);
        quickSettings.add(TooltipMode);
        quickSettings.add(TooltipPos);
        quickSettings.add(RainbowPane);
        quickSettings.add(RainbowAccent);
        quickSettings.add(TextColor);
        active.value = true;
        loadFonts.addButton("Load Fonts", () -> {
            FontManager.INSTANCE = new FontManager();
            Font.setOptions(FontManager.fontNames);
        });
    }

    @Override
    public void onSettingChange(Setting setting) {
        super.onSettingChange(setting);
        Tooltip.tooltip.mode = TooltipMode.value;
        Tooltip.tooltip.fixedPos = TooltipPos.value;
        Renderer2D.renderer = Renderer2D.Renderers.values()[FontRenderer.value];

        if (!(setting instanceof CycleSetting) && !(setting instanceof DoubleSetting)) {
            ColorManager.INSTANCE.clickGuiSecondaryAlpha = AccentColor.getA();
            ColorManager.INSTANCE.clickGuiSecondary = AccentColor.value;
            ColorManager.INSTANCE.clickGuiSecondaryRainbow = RainbowAccent.value;

            ColorManager.INSTANCE.defaultTextColor = TextColor.value;

            ColorManager.INSTANCE.clickGuiPaneTextAlpha = PaneTextColor.getA();
            ColorManager.INSTANCE.clickGuiPaneText = PaneTextColor.value;
            ColorManager.INSTANCE.clickGuiPaneTextRainbow = RainbowPane.value;
        }
        pause = Pause.value;
        keybinds = Keybinds.value;

        FontManager.fonts = FontUtils.rearrangeFontsArray(FontManager.Originalfonts, FontManager.Originalfonts[Font.value]);
        FontManager.fontRenderer = new FontRenderer(FontManager.fonts, FontManager.fontSize);
        FontManager.fxfontRenderer = new fxFontRenderer(FontManager.fonts, 8f);
        EventManager.postEvent(new FontChangeEvent(FontManager.fonts));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        Tooltip.tooltip.mode = TooltipMode.value;
        Tooltip.tooltip.fixedPos = TooltipPos.value;
        Renderer2D.renderer = Renderer2D.Renderers.values()[FontRenderer.value];

        ColorManager.INSTANCE.clickGuiSecondaryAlpha = AccentColor.getA();
        ColorManager.INSTANCE.clickGuiSecondary = AccentColor.value;
        ColorManager.INSTANCE.clickGuiSecondaryRainbow = RainbowAccent.value;

        ColorManager.INSTANCE.defaultTextColor = TextColor.value;

        ColorManager.INSTANCE.clickGuiPaneTextAlpha = PaneTextColor.getA();
        ColorManager.INSTANCE.clickGuiPaneText = PaneTextColor.value;
        ColorManager.INSTANCE.clickGuiPaneTextRainbow = RainbowPane.value;
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
}
