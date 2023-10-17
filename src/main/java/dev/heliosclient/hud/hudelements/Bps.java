package dev.heliosclient.hud.hudelements;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.MathUtils;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;


public class Bps extends HudElement {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public Bps() {
        super("Player Speed", "Shows player speed in blocks per second");
        this.width = 40;
        this.height = 10;
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        String text = "Blocks Per Second: ";
        String value = ColorUtils.gray + MathUtils.round(moveSpeed(), 2);
        this.width = Math.round(Renderer2D.getStringWidth(text + value));
        Renderer2D.drawString(drawContext.getMatrices(), text + value, this.x - (float) width / 2, this.y - ((float) height / 2), HeliosClient.uiColorA);

    }

    private double moveSpeed() {
        if (mc.player == null) {
            return 0;
        }
        Vec3d move = new Vec3d(mc.player.getX() - mc.player.prevX, 0, mc.player.getZ() - mc.player.prevZ).multiply(20);

        return Math.abs(MathUtils.length2D(move));
    }
}
