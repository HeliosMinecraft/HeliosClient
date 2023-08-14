package dev.heliosclient.ui.clickgui;

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
            if (this.mode == 0) {
                drawContext.fill(mouseX + 1, mouseY - 1, mouseX + textRenderer.getWidth(this.tooltipText) + 5, mouseY - 14, 0xAA000000);
                drawContext.drawText(textRenderer, this.tooltipText, mouseX + 4, mouseY - 11, 0xFFFFFF, true);
            } else if (this.mode == 1) {
                drawContext.fill(drawContext.getScaledWindowWidth()-textRenderer.getWidth(this.tooltipText)-6, drawContext.getScaledWindowHeight()-13, drawContext.getScaledWindowWidth(), drawContext.getScaledWindowHeight(), 0xAA000000);
                drawContext.drawText(textRenderer, this.tooltipText, drawContext.getScaledWindowWidth()-textRenderer.getWidth(this.tooltipText)-2, drawContext.getScaledWindowHeight() -10, 0xFFFFFF, true);
            } else if (this.mode == 2) {
                drawContext.drawTooltip(textRenderer, Text.of(this.tooltipText), mouseX, mouseY);
            }
            }
        this.tooltipText = "";
        }

    public void changeText(String str) {
        this.tooltipText = str;
    }
}