package dev.heliosclient.hud.hudelements;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class ClientTag extends HudElement {
    public ClientTag() {
        super(DATA);
        this.width = 20;
        this.height = 13;
        this.draggable = false;
        this.renderOutLineBox = false;
    }    public static HudElementData<ClientTag> DATA = new HudElementData<>("Client Tag", "Shows client watermark", ClientTag::new);

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        String text = HeliosClient.clientTag + " " + HeliosClient.versionTag;
        this.width = (int) (Renderer2D.getStringWidth(text));
        this.x = MinecraftClient.getInstance().getWindow().getScaledWidth() - this.width - 3;
        this.y = MinecraftClient.getInstance().getWindow().getScaledHeight() - this.height - 1;

        super.renderElement(drawContext, textRenderer);
        Renderer2D.drawString(drawContext.getMatrices(), text, this.x, this.y, HeliosClient.uiColor);
    }



}
