package dev.heliosclient.managers;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.client.FontChangeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.ui.clickgui.gui.Quadtree;
import dev.heliosclient.util.fontutils.FontLoader;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.fontutils.fxFontRenderer;
import me.x150.renderer.font.FontRenderer;

import java.awt.*;
import java.util.ArrayList;

public class FontManager implements Listener {
    public static Font[] fonts, iconFonts;
    public static Font[] Originalfonts;
    public static int fontSize = 8;
    public static ArrayList<String> fontNames = new ArrayList<>();

    public static FontManager INSTANCE = new FontManager();

    public FontManager() {
        fontNames.clear();
        fonts = FontLoader.loadFonts();
        iconFonts = FontLoader.loadIconFonts();
        Originalfonts = fonts;
        for (Font font : fonts) {
            fontNames.add(font.getName());
        }
        EventManager.register(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent.CLIENT event) {
        if (HeliosClient.MC.getWindow() != null) {
            FontRenderers.fontRenderer = new FontRenderer(fonts, fontSize);
            FontRenderers.fxfontRenderer = new fxFontRenderer(fonts, 8f);
            FontRenderers.iconRenderer = new fxFontRenderer(iconFonts, 10f);

            FontRenderers.Small_fxfontRenderer = new fxFontRenderer(fonts, 6f);
            FontRenderers.Small_iconRenderer = new fxFontRenderer(iconFonts, 6f);

            FontRenderers.Mid_fxfontRenderer = new fxFontRenderer(fonts, 8f);
            FontRenderers.Mid_iconRenderer = new fxFontRenderer(iconFonts, 8f);

            FontRenderers.Large_fxfontRenderer = new fxFontRenderer(fonts, 13f);
            FontRenderers.Large_iconRenderer = new fxFontRenderer(iconFonts, 13f);

            EventManager.postEvent(new FontChangeEvent(FontManager.fonts));
            HeliosClient.quadTree = new Quadtree(0);
            EventManager.unregister(this);
        }
    }
}
