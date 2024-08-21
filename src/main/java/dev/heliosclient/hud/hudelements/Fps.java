package dev.heliosclient.hud.hudelements;

import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class Fps extends HudElement {
    public static HudElementData<Fps> DATA = new HudElementData<>("FPS", "Shows current Fps", Fps::new);

    public Fps() {
        super(DATA);
        this.width = 20;
        this.height = Math.round(Renderer2D.getStringHeight());
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        super.renderElement(drawContext, textRenderer);
        String text = "FPS: " + ColorUtils.gray + mc.getCurrentFps();
        this.width = Math.round(Renderer2D.getStringWidth(text)) + 1;
        Renderer2D.drawString(drawContext.getMatrices(), text, this.x + 1, this.y, ColorManager.INSTANCE.hudColor);
    }

}
