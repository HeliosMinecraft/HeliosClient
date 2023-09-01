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
        int coordX, coordY, coordZ;
        if (mc.player == null) {
            coordX = 0;
            coordY = 0;
            coordZ = 0;
        } else {
            coordX = (int) MathUtils.round(mc.player.getX(), 0);
            coordY = (int) MathUtils.round(mc.player.getY(), 0);
            coordZ = (int) MathUtils.round(mc.player.getZ(), 0);
        }

        String text = "X: " + ColorUtils.gray + coordX + ColorUtils.reset +
                " Y: " + ColorUtils.gray + coordY + ColorUtils.reset +
                " Z: " + ColorUtils.gray + coordZ;

        this.width = textRenderer.getWidth(text) + 1;

        drawContext.drawText(textRenderer, text, this.x-width/2, this.y+height/2-10, HeliosClient.uiColorA, false);
    }

}
