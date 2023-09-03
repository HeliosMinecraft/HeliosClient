package dev.heliosclient.hud.hudelements;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.hud.HudElement;
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
        this.width = textRenderer.getWidth(text);
        drawContext.drawText(textRenderer, text, this.x - width / 2 + 1, this.y + height / 2 - 10, HeliosClient.uiColorA, false);
    }

}
