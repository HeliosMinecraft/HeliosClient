package dev.heliosclient.ui.clickgui.hudeditor;

import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementList;
import dev.heliosclient.hud.HudManager;
import dev.heliosclient.system.ColorManager;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;

public class HudCategoryPane {
    public int x = 20;
    public int y = 20;
    public int startX, startY;
    public int width = 96;
    public boolean dragging = false;
    public boolean collapsed = false;
    public ArrayList<HudElementButton> hudElementButtons = new ArrayList<HudElementButton>();

    public static HudCategoryPane INSTACE = new HudCategoryPane();

    public HudCategoryPane() {
        for (HudElement element : HudElementList.INSTANCE.hudElements) {
            hudElementButtons.add(new HudElementButton(element));
        }
    }

    public void render(DrawContext drawContext, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
        if (dragging) {
            x = mouseX - startX;
            y = mouseY - startY;
        }

        Renderer2D.drawRoundedRectangle(drawContext, x, y, true, true, false, false, width, 16, 3, 0xFF1B1B1B);
        Renderer2D.drawRectangle(drawContext, x, y+16, width, 2, ColorManager.INSTANCE.clickGuiSecondary());
        drawContext.drawText(textRenderer, "Hud elements", x + 4, y + 4, ColorManager.INSTANCE.clickGuiPaneText(), false);
        drawContext.drawText(textRenderer, collapsed ? "+" : "-", x + width - 11, y + 4, ColorManager.INSTANCE.clickGuiPaneText(), false);

        int offsetY = y + 18;
        if (!collapsed) {
            for (HudElementButton elementButton : hudElementButtons) {
                elementButton.render(drawContext, textRenderer, x, offsetY, delta);
                offsetY += 14;
            }
        }
    }

    public void  mouseClicked(double mouseX, double mouseY, int button) {
        if (hovered(mouseX, mouseY) && button == 1) collapsed = !collapsed;
        else if (hovered(mouseX, mouseY) && button == 0) {
            startX = (int) (mouseX - x);
            startY = (int) (mouseY - y);
            dragging = true;
        }

        for (HudElementButton elementButton : hudElementButtons) {
            elementButton.mouseClicked(mouseX, mouseY, button);
        }
    }
    public void  mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
    }

    public boolean hovered(double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + 16;
    }
}
