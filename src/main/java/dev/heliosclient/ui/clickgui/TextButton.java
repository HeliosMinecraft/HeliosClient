package dev.heliosclient.ui.clickgui;

import dev.heliosclient.system.ColorManager;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.awt.*;

public class TextButton {
    String text;
    int x, y, width;
    private int hoverAnimationTimer = 0;

    public TextButton(String text) {
        this.text = text;
    }

    public void render(DrawContext drawContext, TextRenderer textRenderer, int x, int y, int mouseX, int mouseY) {
        this.x = x;
        this.y = y;
        width = textRenderer.getWidth(text);
        if (hovered(mouseX, mouseY)) {
            hoverAnimationTimer = Math.min(hoverAnimationTimer + 2, 40);
        } else {
            hoverAnimationTimer = Math.max(hoverAnimationTimer - 2, 0);
        }
        Renderer2D.drawRoundedRectangle(drawContext, x - 1, y - 2, width + 2, 12, 0, new Color(255, 255, 255, hoverAnimationTimer).getRGB());
        drawContext.drawText(textRenderer, Text.literal(text), x, y, ColorManager.INSTANCE.clickGuiPaneText(), true);
    }

    public boolean hovered(int mouseX, int mouseY) {
        return mouseX >= x - 1 && mouseX <= x + width + 1 && mouseY >= y - 2 && mouseY <= y + 10;
    }

    public void mouseClicked(int mouseX, int mouseY) {
        if (hovered(mouseX, mouseY)) {
            ClickGUIScreen.INSTANCE.onLoad();
            MinecraftClient.getInstance().setScreen(ClickGUIScreen.INSTANCE);
        }
    }
}
