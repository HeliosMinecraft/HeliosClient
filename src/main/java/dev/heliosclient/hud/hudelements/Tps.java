package dev.heliosclient.hud.hudelements;

import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.system.TickRate;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.MathUtils;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class Tps extends HudElement {

    public Tps() {
        super(DATA);
        this.width = 20;
        this.height = Math.round(Renderer2D.getStringHeight());
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        super.renderElement(drawContext, textRenderer);
        String text = "TPS: " + ColorUtils.gray + MathUtils.round(TickRate.INSTANCE.getTPS(), 1);
        this.width = Math.round(Renderer2D.getStringWidth(text)) + 1;
        Renderer2D.drawString(drawContext.getMatrices(), text, this.x + 1, this.y, ColorManager.INSTANCE.hudColor);
    }

    public static HudElementData<Tps> DATA = new HudElementData<>("TPS", "Shows current tps", Tps::new);


}
