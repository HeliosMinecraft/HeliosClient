package dev.heliosclient.hud.hudelements;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

public class Ping extends HudElement {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public Ping() {
        super("Ping", "Shows current player ping");
        this.width = 20;
        this.height = 10;
    }

    public static int getPing() {
        if (mc.player == null) {
            return 0;
        }
        PlayerListEntry entry = mc.player.networkHandler.getPlayerListEntry(mc.player.getUuid());
        if (entry != null) {
            return entry.getLatency();
        }
        return 0;
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        super.renderElement(drawContext,textRenderer);
        String text = "Ping: " + ColorUtils.gray + getPing();
        this.width = Math.round(Renderer2D.getStringWidth(text) + 1);
        Renderer2D.drawString(drawContext.getMatrices(), text, this.x, this.y, HeliosClient.uiColor);
    }
}
