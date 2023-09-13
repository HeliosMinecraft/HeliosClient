package dev.heliosclient.module.sysmodules;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.system.ColorManager;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.InputBox;
import me.x150.renderer.font.FontRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ClickGUI extends Module_ {
    public static boolean pause = false;
    static ClickGUI INSTANCE = new ClickGUI();
    public static DoubleSetting ScrollSpeed = new DoubleSetting("Scroll Speed", "Change your scroll speed for the ClickGUI", INSTANCE, 7, 1, 8, 1);
    DoubleSetting FontSize = new DoubleSetting("FontSize", "Change your FontSize", INSTANCE, 8, 1, 15, 1);
    BooleanSetting Pause = new BooleanSetting("Pause game", "Pause the game when Click GUI is on.", this, false);
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
    StringSetting Font = new StringSetting("Font", "Font for the client", "", 100, InputBox.InputMode.CHARACTERS_AND_WHITESPACE);


    public ClickGUI() {
        super("ClickGUI", "ClickGui related stuff.", Category.RENDER);
        settings.add(Pause);
        settings.add(TooltipMode);
        settings.add(TooltipPos);
        settings.add(ScrollSpeed);
        settings.add(Font);
        settings.add(FontSize);
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
    }

    @Override
    public void onSettingChange(Setting setting) {
        super.onSettingChange(setting);
        Tooltip.tooltip.mode = TooltipMode.value;
        Tooltip.tooltip.fixedPos = TooltipPos.value;

        ColorManager.INSTANCE.clickGuiSecondaryAlpha = AccentColor.getA();
        ColorManager.INSTANCE.clickGuiSecondary = AccentColor.value;
        ColorManager.INSTANCE.clickGuiSecondaryRainbow = RainbowAccent.value;

        ColorManager.INSTANCE.defaultTextColor = TextColor.value;

        ColorManager.INSTANCE.clickGuiPaneTextAlpha = PaneTextColor.getA();
        ColorManager.INSTANCE.clickGuiPaneText = PaneTextColor.value;
        ColorManager.INSTANCE.clickGuiPaneTextRainbow = RainbowPane.value;
        pause = Pause.value;

        HeliosClient.fonts[0]=new Font(Font.getValue(), java.awt.Font.PLAIN,8);
        HeliosClient.fontRenderer = new FontRenderer(HeliosClient.fonts,(int) FontSize.value);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        Tooltip.tooltip.mode = TooltipMode.value;
        Tooltip.tooltip.fixedPos = TooltipPos.value;

        ColorManager.INSTANCE.clickGuiSecondaryAlpha = AccentColor.getA();
        ColorManager.INSTANCE.clickGuiSecondary = AccentColor.value;
        ColorManager.INSTANCE.clickGuiSecondaryRainbow = RainbowAccent.value;

        ColorManager.INSTANCE.defaultTextColor = TextColor.value;

        ColorManager.INSTANCE.clickGuiPaneTextAlpha = PaneTextColor.getA();
        ColorManager.INSTANCE.clickGuiPaneText = PaneTextColor.value;
        ColorManager.INSTANCE.clickGuiPaneTextRainbow = RainbowPane.value;
        pause = Pause.value;
    }

    @Override
    public void toggle() {
    }
}
