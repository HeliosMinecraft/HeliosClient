package dev.heliosclient.ui.clickgui;

import dev.heliosclient.module.Category;
import dev.heliosclient.module.ModuleManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.system.ColorManager;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;

public class CategoryPane {
    public Category category;
    public int x, y, height, width = 96;
    public boolean collapsed = false;
    int startX, startY;
    boolean dragging = false;
    ArrayList<ModuleButton> moduleButtons;

    public CategoryPane(Category category, int initialX, int initialY, boolean collapsed, Screen parentScreen) {
        this.category = category;
        this.x = initialX;
        this.y = initialY;
        this.collapsed = collapsed;
        moduleButtons = new ArrayList<ModuleButton>();
        for (Module_ m : ModuleManager.INSTANCE.getModulesByCategory(category)) {
            moduleButtons.add(new ModuleButton(m,parentScreen));
        }
        if (moduleButtons.size() == 0) collapsed = true;
        height = (moduleButtons.size() * 12) + 18;
    }

    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta, TextRenderer textRenderer) {
        if (dragging) {
            x = mouseX - startX;
            y = mouseY - startY;
        }
        Renderer2D.drawRoundedRectangle(drawContext,x, y, true,true,false,false, width, 16,3, 0xFF1B1B1B);
        Renderer2D.drawRectangle(drawContext,x, y + 16, width,  2, ColorManager.INSTANCE.clickGuiSecondary());

        drawContext.drawText(textRenderer, category.name, x + 4, y + 4, ColorManager.INSTANCE.clickGuiPaneText(), false);
        drawContext.drawText(textRenderer, collapsed ? "+" : "-", x + width - 11, y + 4, ColorManager.INSTANCE.clickGuiPaneText(), false);

        if (!collapsed) {
            int buttonYOffset = y + 18;


            for (ModuleButton m : moduleButtons) {
                if(m.hasFaded()){
                   m.startFading();
                }
                m.setFaded(false);
                m.render(drawContext, mouseX, mouseY, x, buttonYOffset, textRenderer);
                buttonYOffset += 14;
                // Draw the settings for this module if they are open
                if (m.settingsOpen) {
                    for (Setting setting : m.module.quickSettings) {
                        if (!setting.shouldRender()) continue;
                        //setting.width=96;
                        setting.quickSettings=m.settingsOpen;
                        setting.renderCompact(drawContext,x, buttonYOffset,mouseX,mouseY,textRenderer);
                        buttonYOffset += setting.heightCompact;
                    }
                }
            }
        }
        if (collapsed) {
            for (ModuleButton m : moduleButtons) {
                m.setFaded(true);
            }
        }
    }

    public boolean hovered(double mouseX, double mouseY) {
        return mouseX > x + 2 && mouseX < x + (width - 2) && mouseY > y + 2 && mouseY < y + 14;
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        for (ModuleButton moduleButton : moduleButtons) {
            if (moduleButton.mouseClicked(mouseX, mouseY, button,collapsed)) return;
            if (moduleButton.settingsOpen) {
                for (Setting setting : moduleButton.module.settings) {
                    setting.mouseClicked(mouseX,mouseY,button);
                }
            }
        }
        if (hovered(mouseX, mouseY) && button == 1) collapsed = !collapsed;
        else if (hovered(mouseX, mouseY) && button == 0) {
            startX = mouseX - x;
            startY = mouseY - y;
            dragging = true;
        }
        if (button == 2) {
            startX = mouseX - x;
            startY = mouseY - y;
            dragging = true;
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
        for (ModuleButton moduleButton : moduleButtons) {
            if (moduleButton.settingsOpen) {
                for (Setting setting : moduleButton.module.settings) {
                    setting.mouseReleased(mouseX, mouseY, button);
                }
            }
        }
        dragging = false;
    }
}
