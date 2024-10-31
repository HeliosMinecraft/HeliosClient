package dev.heliosclient.hud.hudelements;

import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.system.TickRate;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public class LagTimerHud extends HudElement {
    public static HudElementData<LagTimerHud> DATA = new HudElementData<>("LagTimerHud", "Shows how long the server has been lagging for", LagTimerHud::new);

    public LagTimerHud() {
        super(DATA);
        this.height = Math.round(Renderer2D.getStringHeight());
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        super.renderElement(drawContext, textRenderer);
        float timeSinceLastTick = TickRate.INSTANCE.getTimeSinceLastTick();

        if(isInHudEditor){
            timeSinceLastTick = 5;
        }

        if (timeSinceLastTick >= 1f) {
            Color color;

            if (timeSinceLastTick > 10) color = Color.RED;
            else if (timeSinceLastTick > 3) color = Color.ORANGE;
            else color = Color.YELLOW;

            String text = String.format(ColorUtils.white + "Latency since last tick: " + ColorUtils.reset + "%.1f", timeSinceLastTick);
            this.width = Math.round(Renderer2D.getStringWidth(text) + 1);
            Renderer2D.drawString(drawContext.getMatrices(), text, this.x, this.y, color.getRGB());
        }
    }
}
