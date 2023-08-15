package dev.heliosclient.system;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.ColorUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.awt.*;

public class ColorManager {

    public static final ColorManager INSTANCE = new ColorManager();

    //Global
    public int defaultTextColor = 0xFFFFFFFF;
    public int defaultTextColor() {
        return defaultTextColor;
    }

    //ClickGui
    public boolean clickGuiSecondaryRainbow = false;
    public int clickGuiSecondary = 0xffff6e78;
    public int clickGuiSecondary() {
        if (clickGuiSecondaryRainbow) {
            return ColorUtils.getRainbowColor().getRGB();
        } else {
            return clickGuiSecondary;
        }
    }

    public boolean clickGuiPaneTextRainbow = false;
    public int clickGuiPaneText = 0xFFFFFFFF;
    public int clickGuiPaneText() {
        if (clickGuiPaneTextRainbow) {
            return ColorUtils.getRainbowColor().getRGB();
        } else {
            return clickGuiPaneText;
        }
    }
}
