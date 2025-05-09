package dev.heliosclient.managers;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.heliosclient.FontChangeEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.util.fontutils.BetterFontRenderer;
import dev.heliosclient.util.fontutils.FontLoader;
import dev.heliosclient.util.fontutils.FontRenderers;
import me.x150.renderer.font.FontRenderer;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class FontManager implements Listener {
    public static final Map<String, Font> FONTS = new HashMap<>();
    public static final Map<String, Font> ICON_FONTS = new HashMap<>();
    public static Font ACTIVE_FONT;
    public static Font ACTIVE_ICON_FONT;
    public static int HUD_FONT_SIZE = 8, GLOBAL_FONT_SIZE = 8;
    public static FontManager INSTANCE = new FontManager();

    private FontManager(){
        refresh();
    }

    public void refresh(){
        FONTS.clear();
        ICON_FONTS.clear();
        ACTIVE_FONT = null;
        ACTIVE_ICON_FONT = null;

        FontLoader.getFonts().forEach(FontManager::registerFont);
        FontLoader.getFonts().forEach(FontManager::registerIconFont);
    }

    @SubscribeEvent(priority = SubscribeEvent.Priority.HIGHEST)
    public void onTick(TickEvent.CLIENT e) {
        if (HeliosClient.MC.getWindow() != null) {
            initialiseFonts();
            EventManager.unregister(this);
        }
    }

    public void initialiseFonts() {
        FontRenderers.fontRenderer = new FontRenderer(ACTIVE_FONT, HUD_FONT_SIZE);
        FontRenderers.fxfontRenderer = new BetterFontRenderer(ACTIVE_FONT, GLOBAL_FONT_SIZE);
        FontRenderers.iconRenderer = new BetterFontRenderer(ACTIVE_ICON_FONT, 10f);

        FontRenderers.Super_Small_fxfontRenderer = new BetterFontRenderer(ACTIVE_FONT, 4f);
        FontRenderers.Super_Small_iconRenderer = new BetterFontRenderer(ACTIVE_ICON_FONT, 4f);

        FontRenderers.Small_fxfontRenderer = new BetterFontRenderer(ACTIVE_FONT, 6f);
        FontRenderers.Small_iconRenderer = new BetterFontRenderer(ACTIVE_ICON_FONT, 6f);

        FontRenderers.Mid_fxfontRenderer = new BetterFontRenderer(ACTIVE_FONT, 8f);
        FontRenderers.Mid_iconRenderer = new BetterFontRenderer(ACTIVE_ICON_FONT, 8f);

        FontRenderers.Large_fxfontRenderer = new BetterFontRenderer(ACTIVE_FONT, 13f);
        FontRenderers.Large_iconRenderer = new BetterFontRenderer(ACTIVE_ICON_FONT, 13f);

        if(FontLoader.COMICALFONTS != null)
            FontRenderers.Comical_fxfontRenderer = new BetterFontRenderer(FontLoader.COMICALFONTS[0], 12f);

        FontRenderers.Ultra_Large_iconRenderer = new BetterFontRenderer(ACTIVE_ICON_FONT, 25f);

        //Post the font change event to the EventManager
        EventManager.postEvent(new FontChangeEvent());
    }

    public static void registerFont(Font font) {
        FONTS.put(font.getFontName(), font);
        if(ACTIVE_FONT == null) ACTIVE_FONT = font;
    }

    public static void registerIconFont(Font font) {
        ICON_FONTS.put(font.getFontName(), font);
        if(ACTIVE_ICON_FONT == null) ACTIVE_ICON_FONT = font;
    }
    public static void setActiveIconFont(Font font) {
        if(!ICON_FONTS.containsValue(font)) {
            ICON_FONTS.put(font.getFontName(), font);
        }
        ACTIVE_ICON_FONT = font;
    }

    public static void setActiveFont(Font font) {
        if(!FONTS.containsValue(font)) {
            FONTS.put(font.getFontName(), font);
        }
        ACTIVE_FONT = font;
    }

    public static Font getFont(String key) {
        return FONTS.getOrDefault(key, ACTIVE_FONT);
    }

    public static Font getIconFont(String key) {
        return ICON_FONTS.getOrDefault(key, ACTIVE_ICON_FONT);
    }

    public static boolean areFontsAvailable(){
        return !FONTS.isEmpty();
    }
}
