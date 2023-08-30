package dev.heliosclient.hud.hudelements;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class Coords extends HudElement {

    private MinecraftClient mc = MinecraftClient.getInstance();

    public Coords() {
        super("Coords", "Shows player coords");
        this.width = 50;
        this.height = 13;
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        if (mc.player == null) return;
        String text = "X: " + ColorUtils.gray + MathUtils.round(mc.player.getX(), 1) + ColorUtils.reset +
                " Y: " + ColorUtils.gray + MathUtils.round(mc.player.getY(), 1) + ColorUtils.reset +
                " Z: " + ColorUtils.gray + MathUtils.round(mc.player.getZ(), 1);

        this.width = textRenderer.getWidth(text);

        drawContext.drawText(textRenderer, text, this.x-width/2, this.y+height/2-10, HeliosClient.uiColorA, false);
    }

}
