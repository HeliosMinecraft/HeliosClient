package dev.heliosclient.managers;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.util.fontutils.FontLoader;
import dev.heliosclient.util.fontutils.fxFontRenderer;
import me.x150.renderer.font.FontRenderer;

import java.awt.*;
import java.util.ArrayList;

public class FontManager implements Listener {
    public static Font[] fonts;
    public static Font[] Originalfonts;
    public static int fontSize = 8;
    public static ArrayList<String> fontNames = new ArrayList<>();
    public static FontRenderer fontRenderer;
    public static fxFontRenderer fxfontRenderer;

    public FontManager() {
        fontNames.clear();
        fonts = FontLoader.loadFonts();
        Originalfonts = fonts;
        for (Font font : fonts) {
            fontNames.add(font.getName());
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.CLIENT event) {
        if (HeliosClient.MC.getWindow() != null) {
            fontRenderer = new FontRenderer(fonts, fontSize);
            fxfontRenderer = new fxFontRenderer(fonts, 8f);
            EventManager.unregister(this);
        }
    }
}
