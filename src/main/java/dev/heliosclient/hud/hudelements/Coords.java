package dev.heliosclient.hud.hudelements;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.MathUtils;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class Coords extends HudElement {

    public static HudElementData<Coords> DATA = new HudElementData<>("Coords", "Shows player coords", Coords::new);

    public Coords() {
        super(DATA);
        this.width = 50;
        this.height = 10;
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        super.renderElement(drawContext,textRenderer);
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

        this.width = Math.round(Renderer2D.getStringWidth(text) + 1);
        Renderer2D.drawString(drawContext.getMatrices(), text, this.x, this.y, HeliosClient.uiColor);
    }

}
