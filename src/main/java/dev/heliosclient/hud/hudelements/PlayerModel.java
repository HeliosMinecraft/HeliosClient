package dev.heliosclient.hud.hudelements;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

import java.awt.*;
import java.util.List;

public class PlayerModel extends HudElement {
  public static HudElementData<PlayerModel> DATA = new HudElementData<>("Player Model", "Shows player model in a small cute way", PlayerModel::new);

public SettingGroup group = new SettingGroup("Size");
    public DoubleSetting size = group.add(new DoubleSetting.Builder()
            .name("Size")
            .description("Change the size of the model")
            .min(0.1D)
            .max(2.0D)
            .value(1D)
            .defaultValue(1D)
            .onSettingChange(this)
            .roundingPlace(1)
            .build()
    );

    public PlayerModel() {
        super(DATA);
        this.width = 30;
        this.height = 55;
        addSettingGroup(group);

    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        this.width = 30;
        this.height = 55;
        super.renderElement(drawContext, textRenderer);
        ClientPlayerEntity player = HeliosClient.MC.player;
        if (player == null && !renderBg.value) {
            Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), this.x, this.y, width - 1, height - 1, Color.BLACK.getRGB());
        }
        if (player != null) {
            Renderer2D.drawEntity(drawContext, x + width / 2, y + height - 6, ((int) (25 * size.value)), player, HeliosClient.MC.getTickDelta());
        }
    }
}
