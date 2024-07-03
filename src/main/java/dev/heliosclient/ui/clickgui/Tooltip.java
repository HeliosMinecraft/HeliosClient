package dev.heliosclient.ui.clickgui;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.awt.*;

public class Tooltip {
    public static final Tooltip tooltip = new Tooltip();
    public String tooltipText = "";
    public Integer mode = 0;
    public Integer fixedPos = 3;

    private Tooltip() {
    }

    public void render(DrawContext drawContext, TextRenderer textRenderer, int mouseX, int mouseY) {
        if (this.tooltipText != null && !this.tooltipText.isEmpty()) {
            int windWidth = drawContext.getScaledWindowWidth();
            int winHeight = drawContext.getScaledWindowHeight();
            int textWidth = Math.round(Renderer2D.getCustomStringWidth(this.tooltipText, FontRenderers.Small_fxfontRenderer));
            int height = Math.round(Renderer2D.getCustomStringHeight(FontRenderers.Small_fxfontRenderer) + 6);

            if (this.mode == 0) {
                this.renderTooltip(drawContext, tooltipText, mouseX + 1, mouseY - 1);
            } else if (this.mode == 1) {
                switch (fixedPos) {
                    case 0 ->
                        //Top-left
                            this.renderTooltip(drawContext, tooltipText, 0, 0);
                    case 1 ->
                        //Top-right
                            this.renderTooltip(drawContext, tooltipText, windWidth - textWidth - 16, 0);
                    case 2 ->
                        //Bottom-left
                            this.renderTooltip(drawContext, tooltipText, 0, winHeight - height - 6);
                    case 3 ->
                        //Bottom-right
                            this.renderTooltip(drawContext, tooltipText, windWidth - textWidth - 16, winHeight - height - 6);
                    case 4 ->
                        //Center
                            this.renderTooltip(drawContext, tooltipText, windWidth / 2 - (textWidth - 5) / 2, winHeight / 2);
                }
            } else if (this.mode == 2) {
                drawContext.drawTooltip(textRenderer, Text.of(this.tooltipText), mouseX, mouseY);
            }
        }
        this.tooltipText = "";
    }


    private void renderTooltip(DrawContext drawContext, String text, int x, int y) {
        int textWidth = Math.round(Renderer2D.getCustomStringWidth(text, FontRenderers.Small_fxfontRenderer));
        float textHeight = Renderer2D.getCustomStringHeight(FontRenderers.Small_fxfontRenderer);
        float textY = y + textHeight / 2 + 4; // Center the text vertically


        drawContext.getMatrices().push();
        float scale = 1f;
        if (HeliosClient.CLICKGUI != null) {
            scale = (float) HeliosClient.CLICKGUI.tooltipSize.value;
        }

        Renderer2D.scaleAndPosition(drawContext.getMatrices(), x, y, scale);

        Renderer2D.drawRoundedRectangleWithShadow(drawContext.getMatrices(), x + 5, textY - 2, textWidth + 8, textHeight + 3, 3, 3, Color.BLACK.brighter().brighter().getRGB());
        Renderer2D.drawOutlineGradientRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x + 5, textY - 2, textWidth + 8, textHeight + 3, 3, 0.7f, ColorManager.INSTANCE.getPrimaryGradientStart().darker(), ColorManager.INSTANCE.getPrimaryGradientEnd().darker(), ColorManager.INSTANCE.getPrimaryGradientEnd().darker(), ColorManager.INSTANCE.getPrimaryGradientStart().darker());

        DrawContext prevDrawContext = Renderer2D.drawContext;

        //Apply scaling to vanilla renderer
        if (Renderer2D.isVanillaRenderer()) {
            Renderer2D.setDrawContext(drawContext);
        }

        Renderer2D.drawCustomString(FontRenderers.Small_fxfontRenderer, drawContext.getMatrices(), text, x + 8 + (Renderer2D.isVanillaRenderer() ? 1 : 0), textY + (Renderer2D.isVanillaRenderer() ? 1 : 0), ColorManager.INSTANCE.defaultTextColor());

        if (Renderer2D.isVanillaRenderer()) {
            Renderer2D.setDrawContext(prevDrawContext);
        }

        Renderer2D.stopScaling(drawContext.getMatrices());
    }

    public void changeText(String str) {
        this.tooltipText = str;
    }
}