package dev.heliosclient.hud.hudelements;

import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

import java.awt.*;
import java.util.List;

public class PlayerModel extends HudElement {

    public PlayerModel() {
        super(DATA);
        this.width = 30;
        this.height = 55;
    }    public static HudElementData<PlayerModel> DATA = new HudElementData<>("Player Model", "Shows player model in a small cute way", PlayerModel::new);

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        this.width = 30;
        this.height = 55;
        super.renderElement(drawContext, textRenderer);
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), this.x, this.y, width - 1, height - 1, Color.BLACK.getRGB());
        } else {
            Renderer2D.drawEntity(drawContext, x + width / 2, y + height - 6, 25, player);
        }

    }

    @Override
    public Object saveToToml(List<Object> objects) {
        return super.saveToToml(objects);
    }


}
