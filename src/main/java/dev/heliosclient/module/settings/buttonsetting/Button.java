package dev.heliosclient.module.settings.buttonsetting;

import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public class Button {
    private final String text;
    private final Runnable task;
    private int x, y, width, height;

    public Button(String text, Runnable task, int x, int y, int width, int height) {
        this.text = text;
        this.task = task;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void render(DrawContext drawContext, int mouseX, int mouseY, TextRenderer textRenderer) {
        int fillColor = Color.DARK_GRAY.getRGB();
        int borderColor = hovered(mouseX, mouseY) ? Color.WHITE.getRGB() : Color.BLACK.getRGB();
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y, width - 4, 14, 2, fillColor);
        Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x, y, width - 4, 14, 2, 0.7f, borderColor);

        // Render button text
        int textX = Math.round(x + (width - Renderer2D.getFxStringWidth(text)) / 2);
        float textHeight = Renderer2D.getFxStringHeight();
        float textY = y + (14 - textHeight) / 2; // Center the text vertically
        if (Renderer2D.isVanillaRenderer()) {
            textY += 1;
        }
        Renderer2D.drawFixedString(drawContext.getMatrices(), text, textX, textY, Color.WHITE.getRGB());
    }

    private boolean hovered(int mouseX, int mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + 18;
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        if (hovered((int) mouseX, (int) mouseY)) {
            task.run();
            return true;
        }
        return false;
    }

    public String getText() {
        return text;
    }

    public Runnable getTask() {
        return task;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}