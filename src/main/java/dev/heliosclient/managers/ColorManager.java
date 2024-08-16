package dev.heliosclient.managers;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.module.modules.render.GUI;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.ColorUtils;

import java.awt.*;

public class ColorManager implements Listener {

    public static ColorManager INSTANCE;

    public static boolean SYNC_ACCENT = false;

    //ClickGui
    public int clickGuiPrimary = new Color(17, 18, 19, 255).getRGB();
    //Global
    public int defaultTextColor = 0xFFFFFFFF;

    public boolean clickGuiSecondaryRainbow = false;

    public int clickGuiSecondary = 0xffff6e78;
    public int clickGuiSecondaryAlpha = 255;

    public boolean clickGuiPaneTextRainbow = false;

    public int clickGuiPaneText = 0xFFFFFFFF;
    public int clickGuiPaneTextAlpha = 255;

    public int hudColor = 0x55FFFF;

    //This is the gradient we use across the GUI
    public Color primaryGradientStart = new Color(0);
    public Color primaryGradientEnd = new Color(0);

    public static void createInstance(){
        INSTANCE = new ColorManager();
    }

    private ColorManager() {
        new GradientManager.GradientBuilder()
                .setName("Rainbow")
                .setStartGradient(ColorUtils::getRainbowColor2)
                .setEndGradient(ColorUtils::getRainbowColor)
                .register();

        new GradientManager.GradientBuilder()
                .setName("DaySky")
                .setStartGradient(() -> {
                    float hue = (System.currentTimeMillis() % 10000) / 10000f;
                    return ColorUtils.getDaySkyColors(hue)[0];
                })
                .setEndGradient(() -> {
                    float hue = (System.currentTimeMillis() % 10000) / 10000f;
                    return ColorUtils.getDaySkyColors(hue)[1];
                })
                .register();

        new GradientManager.GradientBuilder()
                .setName("EveningSky")
                .setStartGradient(() -> {
                    float hue = (System.currentTimeMillis() % 10000) / 10000f;
                    return ColorUtils.getEveningSkyColors(hue)[0];
                })
                .setEndGradient(() -> {
                    float hue = (System.currentTimeMillis() % 10000) / 10000f;
                    return ColorUtils.getEveningSkyColors(hue)[1];
                })
                .register();

        new GradientManager.GradientBuilder()
                .setName("NightSky")
                .setStartGradient(() -> {
                    float hue = (System.currentTimeMillis() % 10000) / 10000f;
                    return ColorUtils.getNightSkyColors(hue)[0];
                })
                .setEndGradient(() -> {
                    float hue = (System.currentTimeMillis() % 10000) / 10000f;
                    return ColorUtils.getNightSkyColors(hue)[1];
                })
                .register();

        new GradientManager.GradientBuilder()
                .setName("Linear2D")
                .setStartGradient(() -> ModuleManager.get(GUI.class).linear2Start.getColor())
                .setEndGradient(() -> ModuleManager.get(GUI.class).linear2end.getColor())
                .register();

        new GradientManager.GradientBuilder()
                .setName("Primary")
                .setStartGradient(this::getPrimaryGradientStart)
                .setEndGradient(this::getPrimaryGradientEnd)
                .register();

    }

    public int defaultTextColor() {
        return defaultTextColor;
    }

    public static GradientManager.Gradient getPrimaryGradient() {
        return GradientManager.getGradient("Primary");
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

    public int hudColor() {
        return hudColor;
    }

    public Color ClickGuiPrimary() {
        return new Color(clickGuiPrimary);
    }

    public int clickGuiPaneText() {
        if (clickGuiPaneTextRainbow) {
            return ColorUtils.changeAlpha(ColorUtils.getRainbowColor(), clickGuiPaneTextAlpha).getRGB();
        } else {
            return clickGuiPaneText;
        }
    }

    @SuppressWarnings("all")
    @SubscribeEvent
    public void onTick(TickEvent.CLIENT e) {
        if (HeliosClient.CLICKGUI == null) return;
        ColorManager.SYNC_ACCENT = HeliosClient.CLICKGUI.syncAccentColor.value;


        Tooltip.tooltip.mode = HeliosClient.CLICKGUI.TooltipMode.value;
        Tooltip.tooltip.fixedPos = HeliosClient.CLICKGUI.TooltipPos.value;

        //Sync accent color with the rest of the client.
        //Hud color is synced in HUDModule.java
        if (SYNC_ACCENT) {
            updateClickGuiSecondary(HeliosClient.CLICKGUI.AccentColor.getColor(), HeliosClient.CLICKGUI.AccentColor.isRainbow());

            updatePrimaryGradients(HeliosClient.CLICKGUI.AccentColor.getColor(), HeliosClient.CLICKGUI.AccentColor.getColor());
            return;
        }


        float hue = (System.currentTimeMillis() % 10000) / 10000f;

        GUI gui = (ModuleManager.get(GUI.class));

        clickGuiPrimary = gui.clickGUIPrimary.getColor().getRGB();

        if (gui.ColorMode.value == 0) {
            updatePrimaryGradients(gui.staticColor.getColor(), gui.staticColor.getColor());
        }else if (gui.ColorMode.value == 1) {
            updatePrimaryGradients(gui.gradientType.get().getStartGradient(), gui.gradientType.get().getEndGradient());
        }


        updateClickGuiSecondary(HeliosClient.CLICKGUI.AccentColor.getColor(), HeliosClient.CLICKGUI.AccentColor.isRainbow());

        this.defaultTextColor = HeliosClient.CLICKGUI.TextColor.getColor().getRGB();

        updateClickGuiPaneText(HeliosClient.CLICKGUI.PaneTextColor.getColor(), HeliosClient.CLICKGUI.PaneTextColor.isRainbow());
    }

    public void updatePrimaryGradients(Color start, Color end) {
        this.primaryGradientStart = start;
        this.primaryGradientEnd = end;
    }

    public void updateClickGuiSecondary(Color color, boolean rainbow) {
        this.clickGuiSecondaryAlpha = color.getAlpha();
        this.clickGuiSecondary = color.getRGB();
        this.clickGuiSecondaryRainbow = rainbow;
    }

    public void updateClickGuiPaneText(Color color, boolean rainbow) {
        this.clickGuiPaneTextAlpha = color.getAlpha();
        this.clickGuiPaneText = color.getRGB();
        this.clickGuiPaneTextRainbow = rainbow;
    }
}
