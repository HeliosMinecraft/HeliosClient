package dev.heliosclient.managers;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.heliosclient.FontChangeEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.util.fontutils.FontLoader;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.fontutils.fxFontRenderer;
import me.x150.renderer.font.FontRenderer;

import java.awt.*;
import java.util.ArrayList;

public class FontManager implements Listener {
    public static Font[] fonts, iconFonts;
    public static Font font, iconFont;
    public static Font[] originalFonts;
    public static int hudFontSize = 8, clientFontSize = 8;
    public static ArrayList<String> fontNames = new ArrayList<>();
    public static FontManager INSTANCE = new FontManager();

    public FontManager() {
        refresh();
    }

    public void refresh() {
        fontNames.clear();
        fonts = FontLoader.loadFonts();
        font = fonts[0];
        iconFonts = FontLoader.loadIconFonts();
        iconFont = iconFonts[0];
        originalFonts = fonts.clone();
        for (Font font : originalFonts) {
            fontNames.add(font.getName());
        }
        EventManager.register(this);
    }

    @SubscribeEvent(priority = SubscribeEvent.Priority.HIGHEST)
    public void onTick(TickEvent.CLIENT event) {
        if (HeliosClient.MC.getWindow() != null) {
            registerFonts();
            EventManager.unregister(this);
        }
    }

    public void registerFonts() {
        FontRenderers.fontRenderer = new FontRenderer(font, hudFontSize);
        FontRenderers.fxfontRenderer = new fxFontRenderer(font, clientFontSize);
        FontRenderers.iconRenderer = new fxFontRenderer(iconFont, 10f);

        FontRenderers.Super_Small_fxfontRenderer = new fxFontRenderer(font, 4f);
        FontRenderers.Super_Small_iconRenderer = new fxFontRenderer(iconFont, 4f);

        FontRenderers.Small_fxfontRenderer = new fxFontRenderer(font, 6f);
        FontRenderers.Small_iconRenderer = new fxFontRenderer(iconFont, 6f);

        FontRenderers.Mid_fxfontRenderer = new fxFontRenderer(font, 8f);
        FontRenderers.Mid_iconRenderer = new fxFontRenderer(iconFont, 8f);

        FontRenderers.Large_fxfontRenderer = new fxFontRenderer(font, 13f);
        FontRenderers.Large_iconRenderer = new fxFontRenderer(iconFont, 13f);

        if(FontLoader.COMICALFONTS != null)
          FontRenderers.Comical_fxfontRenderer = new fxFontRenderer(FontLoader.COMICALFONTS[0], 12f);

        FontRenderers.Ultra_Large_iconRenderer = new fxFontRenderer(iconFont, 25f);

        //Post the font change event to the EventManager
        EventManager.postEvent(new FontChangeEvent(FontManager.fonts));
    }
}
