package dev.heliosclient.managers;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.ColorUtils;

import java.awt.*;

public class ColorManager implements Listener {

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
    @SubscribeEvent
    public void onTick(TickEvent event) {
        if(HeliosClient.clickGUI == null) return;
        Tooltip.tooltip.mode = HeliosClient.clickGUI.TooltipMode.value;
        Tooltip.tooltip.fixedPos = HeliosClient.clickGUI.TooltipPos.value;

        float hue = (System.currentTimeMillis() % 10000) / 10000f;
        //System.out.println(ColorMode.value+ ": ");
        //System.out.print(" ");

        if(HeliosClient.clickGUI.ColorMode.value == 0) {
            this.primaryGradientStart = HeliosClient.clickGUI.staticColor.getColor();
            this.primaryGradientEnd = HeliosClient.clickGUI.staticColor.getColor();
        }
        if(HeliosClient.clickGUI.ColorMode.value == 1) {
            switch(HeliosClient.clickGUI.GradientType.value){
                case 0->{
                    this.primaryGradientStart = ColorUtils.getRainbowColor();
                    this.primaryGradientEnd = ColorUtils.getRainbowColor2();
                }
                case 1 ->{
                    this.primaryGradientStart = ColorUtils.getDaySkyColors(hue)[0];
                    this.primaryGradientEnd = ColorUtils.getDaySkyColors(hue)[1];
                }
                case 2 ->{
                    this.primaryGradientStart = ColorUtils.getEveningSkyColors(hue)[0];
                    this.primaryGradientEnd = ColorUtils.getEveningSkyColors(hue)[1];
                }
                case 3 ->{
                    this.primaryGradientStart = ColorUtils.getNightSkyColors(hue)[0];
                    this.primaryGradientEnd = ColorUtils.getNightSkyColors(hue)[1];
                }
                case 4->{
                    this.primaryGradientStart = HeliosClient.clickGUI.linear2Start.getColor();
                    this.primaryGradientEnd = HeliosClient.clickGUI.linear2end.getColor();
                }
            }
        }


        this.clickGuiSecondaryAlpha = HeliosClient.clickGUI.AccentColor.getColor().getAlpha();
        this.clickGuiSecondary = HeliosClient.clickGUI.AccentColor.getColor().getRGB();
        this.clickGuiSecondaryRainbow = HeliosClient.clickGUI.AccentColor.isRainbow();

        this.defaultTextColor = HeliosClient.clickGUI.TextColor.getColor().getRGB();

        this.clickGuiPaneTextAlpha = HeliosClient.clickGUI.PaneTextColor.getColor().getAlpha();
        this.clickGuiPaneText = HeliosClient.clickGUI.PaneTextColor.getColor().getRGB();
        this.clickGuiPaneTextRainbow = HeliosClient.clickGUI.PaneTextColor.isRainbow();
    }
}
