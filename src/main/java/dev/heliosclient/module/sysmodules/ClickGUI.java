package dev.heliosclient.module.sysmodules;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.ColorSetting;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.system.ColorManager;
import dev.heliosclient.ui.ModulesListOverlay;
import dev.heliosclient.util.ColorUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class ClickGUI extends Module_ {
    BooleanSetting Pause = new BooleanSetting("Pause game", "Pause the game when Click GUI is on.", this,false);
    ColorSetting AccentColor = new ColorSetting("Accent color", "Accent color of Click GUI.", this, ColorManager.INSTANCE.clickGuiSecondary);
    BooleanSetting RainbowAccent = new BooleanSetting("Rainbow", "Rainbow effect for accent color.",this,false);
    BooleanSetting RainbowPane = new BooleanSetting("Rainbow", "Rainbow effect for category panes.", this,false);
    ColorSetting PaneTextColor = new ColorSetting("Category pane text color", "Color of pane text.", this, ColorManager.INSTANCE.clickGuiPaneText);
    ColorSetting TextColor = new ColorSetting("Text color", "Color of text all through out the client.", this, ColorManager.INSTANCE.defaultTextColor);

    public static boolean pause = false;



    public ClickGUI() {
        super("Settings", "ClickGUI settings.",  Category.RENDER);
        settings.add(Pause);
        settings.add(AccentColor);
        settings.add(RainbowAccent);
        settings.add(PaneTextColor);
        settings.add(RainbowPane);
        settings.add(TextColor);
        active.value=true;
    }

    @Override
    public void onSettingChange(Setting setting) {
        ColorManager.INSTANCE.clickGuiSecondary = AccentColor.value;
        ColorManager.INSTANCE.clickGuiSecondaryRainbow = RainbowAccent.value;
        ColorManager.INSTANCE.defaultTextColor = TextColor.value;
        ColorManager.INSTANCE.clickGuiPaneText = PaneTextColor.value;
        ColorManager.INSTANCE.clickGuiPaneTextRainbow = RainbowPane.value;
        pause = Pause.value;
    }

    @Override
    public void onLoad() {
        ColorManager.INSTANCE.clickGuiSecondary = AccentColor.value;
        ColorManager.INSTANCE.clickGuiSecondaryRainbow = RainbowAccent.value;
        ColorManager.INSTANCE.defaultTextColor = TextColor.value;
        ColorManager.INSTANCE.clickGuiPaneText = PaneTextColor.value;
        ColorManager.INSTANCE.clickGuiPaneTextRainbow = RainbowPane.value;
        pause = Pause.value;
    }

    @Override
    public void toggle() {}
}
