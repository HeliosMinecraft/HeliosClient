package dev.heliosclient.hud.hudelements;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class Fps extends HudElement {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public Fps() {
        super("FPS", "Shows current Fps");
        this.width = 20;
        this.height = 10;
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        super.renderElement(drawContext,textRenderer);
        String text = "FPS: " + ColorUtils.gray + mc.getCurrentFps();
        this.width = Math.round(Renderer2D.getStringWidth(text)) + 2;
        Renderer2D.drawString(drawContext.getMatrices(), text, this.x, this.y, HeliosClient.uiColor);
    }

}