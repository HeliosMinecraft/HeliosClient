package dev.heliosclient.managers;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.module.modules.render.GUI;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.color.ColorUtils;

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
                .setStartGradient(() -> ColorUtils.getDaySkyColors(getHue())[0])
                .setEndGradient(() -> ColorUtils.getDaySkyColors(getHue())[1])
                .register();

        new GradientManager.GradientBuilder()
                .setName("EveningSky")
                .setStartGradient(() -> ColorUtils.getEveningSkyColors(getHue())[0])
                .setEndGradient(() -> ColorUtils.getEveningSkyColors(getHue())[1])
                .register();

        new GradientManager.GradientBuilder()
                .setName("NightSky")
                .setStartGradient(() -> ColorUtils.getNightSkyColors(getHue())[0])
                .setEndGradient(() -> ColorUtils.getNightSkyColors(getHue())[1])
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

        registerStaticGradient("Sunrise", new Color(255, 94, 77), new Color(255, 165, 0));
        registerStaticGradient("Ocean", new Color(0, 105, 148), new Color(0, 168, 255));
        registerStaticGradient("Forest", new Color(34, 139, 34), new Color(107, 142, 35));
        registerStaticGradient("Candy", new Color(255, 105, 180), new Color(255, 20, 147));
        registerStaticGradient("Peach", new Color(255, 229, 180), new Color(255, 204, 153));
        registerStaticGradient("Mango", new Color(255, 204, 102), new Color(255, 179, 71));
        registerStaticGradient("Berry", new Color(138, 43, 226), new Color(75, 0, 130));
        registerStaticGradient("Mint", new Color(189, 252, 201), new Color(144, 238, 144));
        registerStaticGradient("Rose", new Color(255, 192, 203), new Color(219, 112, 147));
        registerStaticGradient("Sky", new Color(135, 206, 235), new Color(70, 130, 180));
        registerStaticGradient("Grape", new Color(128, 0, 128), new Color(75, 0, 130));
        registerStaticGradient("Peacock", new Color(0, 128, 128), new Color(0, 206, 209));
        registerStaticGradient("Flame", new Color(255, 69, 0), new Color(255, 140, 0));

        // Additional universe and space inspired gradients
        registerStaticGradient("Cosmos", new Color(70, 130, 180), new Color(75, 0, 130));
        registerStaticGradient("Galaxy", new Color(0, 0, 128), new Color(75, 0, 130));
        registerStaticGradient("Starlight", new Color(255, 255, 204), new Color(173, 216, 230));
        registerStaticGradient("Comet", new Color(0, 255, 255), new Color(255, 255, 0));
        registerStaticGradient("Aurora", new Color(0, 128, 128), new Color(72, 61, 139));
        registerStaticGradient("Quasar", new Color(255, 140, 0), new Color(239, 46, 140, 169));

        // Additional darker-themed gradients
        registerStaticGradient("Maroon", new Color(128, 0, 0), new Color(102, 0, 0));
        registerStaticGradient("Sapphire", new Color(8, 37, 103), new Color(0, 84, 159));
        registerStaticGradient("Emerald", new Color(1, 50, 32), new Color(0, 128, 0));
        registerStaticGradient("Amethyst", new Color(75, 0, 130), new Color(138, 43, 226));
        registerStaticGradient("Ruby", new Color(155, 17, 30), new Color(204, 0, 0));
        registerStaticGradient("Obsidian", new Color(38, 38, 38), new Color(77, 77, 77));
        registerStaticGradient("Copper", new Color(184, 115, 51), new Color(205, 127, 50));
        registerStaticGradient("Charcoal", new Color(54, 69, 79), new Color(70, 79, 85));
        registerStaticGradient("Steel", new Color(112, 128, 144), new Color(119, 136, 153));
    }
    public static float getHue(){
        return (System.currentTimeMillis() % (HeliosClient.CLICKGUI.getRainbowSpeed() * 1000)) / (HeliosClient.CLICKGUI.getRainbowSpeed() * 1000.0f);
    }
    public int defaultTextColor() {
        return defaultTextColor;
    }

    private void registerStaticGradient(String name, Color start, Color end) {
        new GradientManager.GradientBuilder()
                .setName(name)
                .setStartGradient(() -> start)
                .setEndGradient(() -> end)
                .register();
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
        //Eh could be better, (as it should be)... TODO

        GUI gui = (ModuleManager.get(GUI.class));

        clickGuiPrimary = gui.clickGUIPrimary.getColor().getRGB();

        if (gui.ColorMode.value == 0) {
            updatePrimaryGradients(gui.staticColor.getColor(), gui.staticColor.getColor());
        }else if (gui.ColorMode.value == 1) {
            updatePrimaryGradients(gui.gradientType.get().getStartColor(), gui.gradientType.get().getEndColor());
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
