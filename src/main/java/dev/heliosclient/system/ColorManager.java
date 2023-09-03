package dev.heliosclient.system;

import dev.heliosclient.util.ColorUtils;

public class ColorManager {

    public static final ColorManager INSTANCE = new ColorManager();

    //Global
    public int defaultTextColor = 0xFFFFFFFF;
    //ClickGui
    public boolean clickGuiSecondaryRainbow = false;
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

    public int clickGuiPaneText() {
        if (clickGuiPaneTextRainbow) {
            return ColorUtils.changeAlpha(ColorUtils.getRainbowColor(), clickGuiPaneTextAlpha).getRGB();
        } else {
            return clickGuiPaneText;
        }
    }
}
