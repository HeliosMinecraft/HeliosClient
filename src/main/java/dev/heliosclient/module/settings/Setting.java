package dev.heliosclient.module.settings;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.ui.clickgui.CategoryPane;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public abstract class Setting {
    public String name;
    public String description;
    public int height = 24;
    public int width = 192;
    public int heightCompact = 24;
    public int widthCompact = CategoryPane.getWidth();
    public Object value;
    public boolean quickSettings = false;
    protected int moduleWidth = CategoryPane.getWidth();
    int x = 0, y = 0;
    int hovertimer = 0;
    private int hoverAnimationTimer = 0;

    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        this.x = x;
        this.y = y;
        if (hovered(mouseX, mouseY)) {
            hoverAnimationTimer = Math.min(hoverAnimationTimer + 1, 40);
        } else {
            hoverAnimationTimer = Math.max(hoverAnimationTimer - 1, 0);
        }
       // int fillColor = (int) (34 + 0.85 * hoverAnimationTimer);
        int fillColor =  ColorUtils.changeAlpha(new Color(ColorManager.INSTANCE.ClickGuiPrimary()),  181).getRGB();
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y, width, height, 2,fillColor);
    }

    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        this.x = x;
        this.y = y;
        if (hovered(mouseX, mouseY)) {
            hoverAnimationTimer = Math.min(hoverAnimationTimer + 1, 40);
        } else {
            hoverAnimationTimer = Math.max(hoverAnimationTimer - 1, 0);
        }
        int fillColor =  ColorUtils.changeAlpha(new Color(ColorManager.INSTANCE.ClickGuiPrimary()),  200).getRGB();
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y, widthCompact, heightCompact, 2,fillColor);
    }

    public boolean shouldRender() {
        return true;
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
    }
    public void mouseDragged(double mouseX, double mouseY, int button,double deltaX, double deltaY) {
    }

    public void keyReleased(int keyCode, int scanCode, int modifiers) {
    }
    public void charTyped(char chr, int modifiers) {
    }

    protected boolean hovered(int mouseX, int mouseY) {
        if (quickSettings) {
            return mouseX > x && mouseX < x + widthCompact && mouseY > y && mouseY < y + heightCompact;
        } else {
            return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
        }
    }
}
