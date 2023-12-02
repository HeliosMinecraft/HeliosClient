package dev.heliosclient.managers;

import dev.heliosclient.util.ColorUtils;

import java.awt.*;

public class ColorManager {

    public static final ColorManager INSTANCE = new ColorManager();
    //ClickGui
    public final int clickGuiPrimary = new Color(17, 18, 19, 255).getRGB();
    //Global
    public int defaultTextColor = 0xFFFFFFFF;
    public int clickGuiPrimaryAlpha = 255;

    public boolean clickGuiSecondaryRainbow = false;
    public boolean clickGuiPrimaryRainbow = false;

    public int clickGuiSecondary = 0xffff6e78;
    public int clickGuiSecondaryAlpha = 255;
    public boolean clickGuiPaneTextRainbow = false;
    public int clickGuiPaneText = 0xFFFFFFFF;
    public int clickGuiPaneTextAlpha = 255;

    public Color primaryGradientStart = new Color(0);
    public Color primaryGradientEnd = new Color(0);
    public int defaultTextColor() {
        return defaultTextColor;
    }

    public Color getPrimaryGradientStart() {
        return primaryGradientStart;
    }

    public Color getPrimaryGradientEnd() {
        return primaryGradientEnd;
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
