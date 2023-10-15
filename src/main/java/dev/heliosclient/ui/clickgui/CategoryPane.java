package dev.heliosclient.ui.clickgui;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.module.Category;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryPane {
    public Category category;
    public int x;
    public int y;
    public int height;
    public static int width = 80;
    public boolean collapsed = false;
    int startX, startY;
    boolean dragging = false;
    ArrayList<ModuleButton> moduleButtons;
    private final Screen parentScreen;

    public CategoryPane(Category category, int initialX, int initialY, boolean collapsed, Screen parentScreen) {
        this.category = category;
        this.x = initialX;
        this.y = initialY;
        this.collapsed = collapsed;
        this.parentScreen = parentScreen;
        moduleButtons = new ArrayList<ModuleButton>();
        for (Module_ m : ModuleManager.INSTANCE.getModulesByCategory(category)) {
            moduleButtons.add(new ModuleButton(m, parentScreen));
        }
        if (moduleButtons.size() == 0) collapsed = true;
        height = Math.round((moduleButtons.size() * (5 +  FontManager.fxfontRenderer.getStringHeight("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"))));
    }

    public void addModule(List<Module_> moduleS) {
        for (Module_ module : moduleS) {
            boolean exists = false;
            for (ModuleButton button : moduleButtons) {
                if (button.module == module) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                ModuleButton moduleButton = new ModuleButton(module, parentScreen);
                moduleButtons.add(moduleButton);
            }
        }
    }

    public void removeModule(Module_ module) {
        moduleButtons.removeIf(button -> button.module == module);
    }

    public void keepOnlyModule(Module_ module) {
        moduleButtons.removeIf(button -> button.module != module);
    }

    public void removeModules() {
        moduleButtons.clear();
    }

    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta, TextRenderer textRenderer) {
        int categoryNameHeight = (int) FontManager.fxfontRenderer.getStringHeight(category.name);

        int maxWidth = 0;
        int maxHeight = 4;
        for (ModuleButton m : moduleButtons) {
            maxWidth = Math.max(maxWidth, m.width);
            maxHeight += categoryNameHeight + 10;
        }
        if(maxWidth<getWidth() - 3 ) {
            maxWidth = getWidth() - 3;
        }

        height = maxHeight;
        if(!collapsed && height>=10) {
            if (category != Category.SEARCH) {
                Renderer2D.drawRoundedRectangle(drawContext, x - 2, y + categoryNameHeight + 6, false, false, true, true, width + 4.5f, height, 3, ColorUtils.changeAlpha(new Color(ColorManager.INSTANCE.ClickGuiPrimary()), 100).getRGB());
            }
            else{
                Renderer2D.drawRoundedRectangle(drawContext, x - 2, y + categoryNameHeight + 25, false, false, true, true, width + 4.5f, height, 3, ColorUtils.changeAlpha(new Color(ColorManager.INSTANCE.ClickGuiPrimary()), 100).getRGB());
            }
        }

        if (dragging) {
            x = mouseX - startX;
            y = mouseY - startY;
        }
        if (!collapsed) {
            int buttonYOffset = y + 10 + categoryNameHeight;
            if (category == Category.SEARCH) {
                buttonYOffset = y + 25 + categoryNameHeight;

            }


            for (ModuleButton m : moduleButtons) {
                if (m.hasFaded()) {
                    m.startFading();
                }

                m.setFaded(false);
                m.render(drawContext, mouseX, mouseY, x, buttonYOffset, maxWidth);
                buttonYOffset += categoryNameHeight + 10;
                // Draw the settings for this module if they are open
                if (m.settingsOpen) {
                    for (Setting setting : m.module.quickSettings) {
                        if (!setting.shouldRender()) continue;
                        //setting.width=96;
                        setting.quickSettings = m.settingsOpen;
                        setting.renderCompact(drawContext, x, buttonYOffset, mouseX, mouseY, textRenderer);
                        buttonYOffset += setting.heightCompact + 1;
                    }
                    if(!m.module.quickSettings.isEmpty()) {
                        buttonYOffset += 2;
                    }
                }
            }
        }
        if (collapsed) {
            for (ModuleButton m : moduleButtons) {
                m.setFaded(true);
            }
        }
        //0xFF1B1B1B
        Renderer2D.drawRoundedRectangle(drawContext, x - 2, y,  width + 4.5f , categoryNameHeight + 8, 3, ColorUtils.changeAlpha(new Color(ColorManager.INSTANCE.ClickGuiPrimary()),200).getRGB());
        // Renderer2D.drawRectangle(drawContext, x-2, y + categoryNameHeight + 8, width + 4.5f, 2, ColorManager.INSTANCE.clickGuiSecondary());

        FontManager.fxfontRenderer.drawString(drawContext.getMatrices(), category.name, (float) (x + (CategoryPane.getWidth()-4)/2 - FontManager.fxfontRenderer.getStringWidth(category.name)/2), (float) (y + 4),ColorManager.INSTANCE.clickGuiPaneText(), (float) (10f));
        // FontManager.fxfontRenderer.drawString(drawContext.getMatrices(), collapsed ? "+" : "-", (float) (x + width - 11), (float) (y + 4),ColorManager.INSTANCE.clickGuiPaneText(), (float) (10f));

    }

    public boolean hovered(double mouseX, double mouseY) {
        int categoryNameHeight = (int) FontManager.fxfontRenderer.getStringHeight(category.name);
        return mouseX > x + 2 && mouseX < x + (width - 2) && mouseY > y + 2 && mouseY < y + categoryNameHeight + 14;
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        for (ModuleButton moduleButton : moduleButtons) {
            if (moduleButton.mouseClicked(mouseX, mouseY, button, collapsed)) return;
            if (moduleButton.settingsOpen) {
                for (Setting setting : moduleButton.module.settings) {
                    setting.mouseClicked(mouseX, mouseY, button);
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
    public void charTyped(char chr, int modifiers){
        for (ModuleButton moduleButton : moduleButtons) {
            if (moduleButton.settingsOpen) {
                for (Setting setting : moduleButton.module.settings) {
                    setting.charTyped(chr, modifiers);
                }
            }
        }
    }

    public static int getWidth() {
        return width;
    }
}
