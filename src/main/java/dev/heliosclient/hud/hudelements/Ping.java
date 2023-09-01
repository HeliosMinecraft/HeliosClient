package dev.heliosclient.hud.hudelements;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.util.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

public class Ping extends HudElement {

    private static MinecraftClient mc = MinecraftClient.getInstance();

    public Ping() {
        super("Ping", "Shows current player ping");
        this.width = 20;
        this.height = 13;
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        String text = "Ping: " + ColorUtils.gray + getPing() ;
        this.width = textRenderer.getWidth(text) + 1;
        drawContext.drawText(textRenderer, text, this.x-width/2 + 1, this.y+height/2-10, HeliosClient.uiColorA, false);
    }
    public static int getPing() {
        if (mc.player==null){
            return 0;
        }
        PlayerListEntry entry = mc.player.networkHandler.getPlayerListEntry(mc.player.getUuid());
        if (entry != null) {
            return entry.getLatency();
        }
        return 0;
    }
}
