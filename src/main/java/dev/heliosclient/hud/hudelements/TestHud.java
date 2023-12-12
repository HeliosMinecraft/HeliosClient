package dev.heliosclient.hud.hudelements;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.DamageUtils;
import dev.heliosclient.util.EntityUtils;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class TestHud extends HudElement {
    public static HudElementData<TestHud> DATA = new HudElementData<>("TestHUD", "Testing stuff", TestHud::new);

    public TestHud() {
        super(DATA);
        this.width = 40;
        this.height = 10;
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        super.renderElement(drawContext,textRenderer);
        String text = "CrystalDamage: ";
        String value = ColorUtils.gray + "0";
        this.width = Math.round(Renderer2D.getStringWidth(text + value));
        if (mc.player == null || mc.player.getWorld() == null) return;
        if (EntityUtils.getNearestCrystal(mc.player.getWorld(), mc.player, 10) != null) {
            value = ColorUtils.gray + DamageUtils.calculateCrystalDamage(mc.player.getWorld(), EntityUtils.getNearestCrystal(mc.player.getWorld(), mc.player, 10).getPos(), mc.player);
        }
        this.width = Math.round(Renderer2D.getStringWidth(text + value));

        Renderer2D.drawString(drawContext.getMatrices(), text + value, this.x - 1, this.y, HeliosClient.uiColor);
    }

}
