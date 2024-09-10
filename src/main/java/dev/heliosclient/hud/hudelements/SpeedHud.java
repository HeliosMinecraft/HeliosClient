package dev.heliosclient.hud.hudelements;

import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.MathUtils;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;


public class SpeedHud extends HudElement {
    public SpeedHud() {
        super(DATA);
        this.width = 40;
        this.height = Math.round(Renderer2D.getStringHeight());
    }
    public static HudElementData<SpeedHud> DATA = new HudElementData<>("Player Speed", "Shows player speed in blocks per second", SpeedHud::new);

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        super.renderElement(drawContext, textRenderer);
        String text = "Speed: ";
        String value = ColorUtils.gray + MathUtils.round(moveSpeed(), 2);
        this.width = Math.round(Renderer2D.getStringWidth(text + value));
        Renderer2D.drawString(drawContext.getMatrices(), text + value, this.x, this.y, ColorManager.INSTANCE.hudColor);
    }

    private double moveSpeed() {
        if (mc.player == null) {
            return 0;
        }
        Vec3d move = new Vec3d(mc.player.getX() - mc.player.prevX, 0, mc.player.getZ() - mc.player.prevZ).multiply(20);

        return Math.abs(MathUtils.length2D(move));
    }
}
