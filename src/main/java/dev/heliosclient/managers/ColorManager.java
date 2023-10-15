package dev.heliosclient.managers;

import dev.heliosclient.util.ColorUtils;

import java.awt.*;

public class ColorManager {

    public static final ColorManager INSTANCE = new ColorManager();

    //Global
    public int defaultTextColor = 0xFFFFFFFF;
    //ClickGui
    public final int clickGuiPrimary = new Color(17, 18, 19, 255).getRGB();
    public int clickGuiPrimaryAlpha = 255;

    public boolean clickGuiSecondaryRainbow = false;
    public boolean clickGuiPrimaryRainbow = false;

    public int clickGuiSecondary = 0xffff6e78;
    public int clickGuiSecondaryAlpha = 255;
    public boolean clickGuiPaneTextRainbow = false;
    public int clickGuiPaneText = 0xFFFFFFFF;
    public int clickGuiPaneTextAlpha = 255;

    public int defaultTextColor() {
        return defaultTextColor;
    }

    public int clickGuiSecondary() {
        if (clickGuiSecondaryRainbow) {
            return ColorUtils.changeAlpha(ColorUtils.getRainbowColor(), clickGuiSecondaryAlpha).getRGB();
        } else {
            return clickGuiSecondary;
        }
    }

    public int ClickGuiPrimary() {
        if (clickGuiPrimaryRainbow) {
            return ColorUtils.changeAlpha(ColorUtils.getRainbowColor(), clickGuiPrimaryAlpha).getRGB();
        } else {
            return clickGuiPrimary;
        }
    }

    public int clickGuiPaneText() {
        if (clickGuiPaneTextRainbow) {
            return ColorUtils.changeAlpha(ColorUtils.getRainbowColor(), clickGuiPaneTextAlpha).getRGB();
        } else {
            return clickGuiPaneText;
        }
    }
}
