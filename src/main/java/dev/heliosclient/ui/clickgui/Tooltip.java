package dev.heliosclient.ui.clickgui;

import dev.heliosclient.system.ColorManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class Tooltip {
    public static final Tooltip tooltip = new Tooltip();
    public String tooltipText = "";
    public Integer mode = 0;

    public Tooltip()
    {
    }

    public void render(DrawContext drawContext, TextRenderer textRenderer, int mouseX, int mouseY)
    {
        if (!this.tooltipText.isEmpty()) {
            int windWidth = drawContext.getScaledWindowWidth();
            int winHeight = drawContext.getScaledWindowHeight();
            int textWidth = textRenderer.getWidth(this.tooltipText);
            if (this.mode == 0) {
                this.renderTooltip(drawContext, textRenderer, tooltipText, mouseX + 1, mouseY - 1);
            } else if (this.mode == 1) {
                int x = windWidth-textWidth-4;
                int y = winHeight-13;
                this.renderTooltip(drawContext, textRenderer, tooltipText, windWidth-textWidth-4, winHeight);
            } else if (this.mode == 2) {
                drawContext.drawTooltip(textRenderer, Text.of(this.tooltipText), mouseX, mouseY);
            }
            }
        this.tooltipText = "";
        }


        private void renderTooltip(DrawContext drawContext, TextRenderer textRenderer, String text, int x, int y) {
            int textWidth = textRenderer.getWidth(text);
            drawContext.fill(x, y, x + textWidth + 4, y - 13, 0xAA000000);
            drawContext.drawText(textRenderer, text, x + 2, y - 10, ColorManager.INSTANCE.defaultTextColor(), true);
        }

    public void changeText(String str) {
        this.tooltipText = str;
    }
}