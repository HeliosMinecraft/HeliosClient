package dev.heliosclient.hud.hudelements;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.util.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class Fps extends HudElement {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public Fps() {
        super("FPS", "Shows current Fps");
        this.width = 20;
        this.height = 13;
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        String text = "FPS: " + ColorUtils.gray + mc.getCurrentFps();
        this.width = Math.round(FontManager.fontRenderer.getStringWidth(text));
        FontManager.fontRenderer.drawString(drawContext.getMatrices(),text, this.x - (float) width / 2 , this.y - (float) height / 2,256 - ColorUtils.getRed(HeliosClient.uiColorA),256 - ColorUtils.getGreen(HeliosClient.uiColorA),256 - ColorUtils.getBlue(HeliosClient.uiColorA),256 - ColorUtils.getAlpha(HeliosClient.uiColorA));
    }

}
