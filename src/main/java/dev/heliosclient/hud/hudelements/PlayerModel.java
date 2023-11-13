package dev.heliosclient.hud.hudelements;

import dev.heliosclient.hud.HudElement;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

import java.awt.*;

public class PlayerModel extends HudElement {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public PlayerModel() {
        super("Player Model", "Shows player model in a small cute way");
        this.width = 30;
        this.height = 55;
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        this.width = 30;
        this.height = 55;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), this.x, this.y, width - 1, height - 1, Color.BLACK.getRGB());
        } else {
            Renderer2D.drawEntity(drawContext, x + width / 2, y + height - 3, 25, player);
        }
    }

}
