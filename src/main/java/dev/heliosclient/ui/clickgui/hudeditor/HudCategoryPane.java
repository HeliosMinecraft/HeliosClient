package dev.heliosclient.ui.clickgui.hudeditor;

import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.hud.HudElementList;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.fontutils.FontRenderers;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;

public class HudCategoryPane {
    public static HudCategoryPane INSTANCE = new HudCategoryPane();
    public int x = 20;
    public int y = 20;
    public int startX, startY;
    public int width = 55;
    public boolean dragging = false;
    public boolean collapsed = false;
    public ArrayList<HudElementButton> hudElementButtons = new ArrayList<HudElementButton>();

    public HudCategoryPane() {
        float maxWidth = 0;
        for (HudElementData data : HudElementList.INSTANCE.elementDataMap.values()) {
            HudElement element = data.create();
            HudElementButton button = new HudElementButton(element);
            float elementWidth = Renderer2D.getCustomStringWidth(button.hudElement.name + " [" + button.count + "]", FontRenderers.Small_fxfontRenderer) + 4;
            maxWidth = Math.max(maxWidth, elementWidth);
        }
        width = Math.round(maxWidth);

        for (HudElementData data : HudElementList.INSTANCE.elementDataMap.values()) {
            HudElement element = data.create();
            HudElementButton button = new HudElementButton(element);
            button.width = width;
            hudElementButtons.add(button);
        }
    }
    private float calculateMaxWidth() {
        float maxWidth = 0;
        for (HudElementButton elementButton : hudElementButtons) {
            float elementWidth = Renderer2D.getCustomStringWidth(elementButton.hudElement.name + " [" + elementButton.count + "]", FontRenderers.Small_fxfontRenderer) + 4;
            maxWidth = Math.max(maxWidth, elementWidth);
        }
        return Math.round(maxWidth);
    }

    public void render(DrawContext drawContext, TextRenderer textRenderer, int mouseX, int mouseY, float delta) {
        if (dragging) {
            x = mouseX - startX;
            y = mouseY - startY;
        }

        int offsetY = y + 12;
        if (!collapsed) {
            float maxWidth = calculateMaxWidth();

            width = Math.round(maxWidth);
            for (HudElementButton elementButton : hudElementButtons) {
                elementButton.render(drawContext, collapsed, x, offsetY, delta);
                elementButton.width = width;
                offsetY += 12;
            }
        }
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y, true, true, false, false, width, 12, 3, 0xFF1B1B1B);
        Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y + 12, width, 1, ColorManager.INSTANCE.clickGuiSecondary());

        Renderer2D.drawCustomString(FontRenderers.Small_fxfontRenderer,drawContext.getMatrices(), "Hud elements", x + 4, y + 3,ColorManager.INSTANCE.clickGuiPaneText());
        Renderer2D.drawCustomString(FontRenderers.Small_fxfontRenderer,drawContext.getMatrices(), collapsed ? "+" : "-", x + width - Renderer2D.getCustomStringWidth(collapsed ? "+" : "-",FontRenderers.Small_fxfontRenderer) - 2, y + 3, ColorManager.INSTANCE.clickGuiPaneText());
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (hovered(mouseX, mouseY) && button == 1) collapsed = !collapsed;
        else if (hovered(mouseX, mouseY) && button == 0) {
            startX = (int) (mouseX - x);
            startY = (int) (mouseY - y);
            dragging = true;
        }

        for (HudElementButton elementButton : hudElementButtons) {
            elementButton.mouseClicked(mouseX, mouseY, button,collapsed);
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
    }

    public boolean hovered(double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + 13;
    }
    public boolean hoveredComplete(double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + (hudElementButtons.size() * 12) + 13;
    }
}
