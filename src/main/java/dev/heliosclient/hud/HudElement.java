package dev.heliosclient.hud;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.module.Category;
import dev.heliosclient.ui.clickgui.hudeditor.HudCategoryPane;
import dev.heliosclient.ui.clickgui.hudeditor.HudElementButton;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;

public class HudElement {
    public String name;
    public String description;
    public int height = 8;
    public int width = 10;
    public int x = 90;
    public int y = 90;
    public boolean dragging;
    int startX, startY;
    public double scaledX;
    public int offsetX;

    public boolean selected = false;

    public HudElement(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void renderEditor(DrawContext drawContext, TextRenderer textRenderer, int mouseX, int mouseY) {
        if (dragging) {
            x = mouseX - startX;
            y = mouseY - startY;
            scaledX = Math.min(Math.max((double) x /drawContext.getScaledWindowWidth(), 0f), 1f);

            if (scaledX < 0.33333) {
                offsetX = 0;
            } else if (scaledX >= 0.33333 && scaledX <= 0.66666) {
                offsetX = width/2;
            } else if (scaledX > 0.66666) {
                offsetX = width;
            }
        }
        x= (int) (drawContext.getScaledWindowWidth()*scaledX);

        if (this.selected) {
            drawContext.fill(x-1, y-1, x, y+height+1, 0xFFFFFFFF);
            drawContext.fill(x-1, y-1, x+width+1, y, 0xFFFFFFFF);
            drawContext.fill(x+width, y-1, x+width+1, y+height+1, 0xFFFFFFFF);
            drawContext.fill(x-1, y+height, x+width+1, y+height+1, 0xFFFFFFFF);
        }

        if (scaledX < 0.33333) {
            offsetX = 0;
            renderLeft(drawContext, textRenderer);
        } else if (scaledX >= 0.33333 && scaledX <= 0.66666) {
            offsetX = width/2;
            renderCenter(drawContext, textRenderer);
        } else if (scaledX > 0.66666) {
            offsetX = width;
            renderRight(drawContext, textRenderer);
        }

    }

    public void render(DrawContext drawContext, TextRenderer textRenderer) {
        x= (int) (drawContext.getScaledWindowWidth()*scaledX);

        if (scaledX < 0.33333) {
            renderLeft(drawContext, textRenderer);
        } else if (scaledX >= 0.33333 && scaledX <= 0.66666) {
            renderCenter(drawContext, textRenderer);
        } else if (scaledX > 0.66666) {
            renderRight(drawContext, textRenderer);
        }
    }

    public void renderLeft(DrawContext drawContext, TextRenderer textRenderer) {

    }

    public void renderCenter(DrawContext drawContext, TextRenderer textRenderer) {

    }
    public void renderRight(DrawContext drawContext, TextRenderer textRenderer) {

    }

    public void onLoad() {}
    public void onSettingChange() {}

    public void  mouseClicked(double mouseX, double mouseY, int button) {
        if (selected && button == 0) {
            startX = (int) (mouseX - x);
            startY = (int) (mouseY - y);
            dragging = true;
        }
    }

    public boolean hovered(double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
    }
}
