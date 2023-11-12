package dev.heliosclient.ui.clickgui;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.FontChangeEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.ui.clickgui.gui.Hitbox;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.FileUtils;
import dev.heliosclient.util.Renderer2D;
import me.x150.renderer.render.SVGFile;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CategoryPane implements Listener {
    public Category category;
    public int x;
    public int y;
    public int height;
    public static int width = 83;
    public boolean collapsed = false;
    int startX, startY;
    boolean dragging = false;
    ArrayList<ModuleButton> moduleButtons;
    private final Screen parentScreen;
    public static int MAX_HEIGHT = 150;
    private int scrollOffset = 0;
    public int iconWidth = 10;
    public int iconHeight = 10;
    private SVGFile svgFile;
    public static List<Hitbox> hitboxes = new CopyOnWriteArrayList<>();
    private final float delayBetweenButtons = 0.0f;
    float delay = 0;
    private final Hitbox hitBox;
    int categoryNameHeight = 2;

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

    public void update() {
        float prevbuttonY = y;
        for (ModuleButton button : moduleButtons) {
            button.update(prevbuttonY);
            if (!button.isAnimationDone()) {
                if (delay <= 0) {
                    delay = delayBetweenButtons;
                }
                delay -= button.animationSpeed;
            }
            prevbuttonY = button.y;
        }
    }

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
        height = Math.round((moduleButtons.size() * (5 + Renderer2D.getStringHeight())));

        if (FileUtils.doesFileInPathExist(category.iconSrc)) {
            svgFile = new SVGFile(category.iconSrc, iconWidth, iconHeight);
        } else {
            HeliosClient.LOGGER.info("SVG File for " + category.name + "does not exist");
        }
        hitBox = new Hitbox(x, y, width, height);
        hitboxes.add(hitBox);
        EventManager.register(this);
    }

    @SubscribeEvent
    public void onFontChange(FontChangeEvent event) {
        categoryNameHeight = Math.round(Renderer2D.getFxStringHeight(category.name));
    }
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta, TextRenderer textRenderer) {
        int maxWidth = 0;
        height = 4;
        for (ModuleButton m : moduleButtons) {
            maxWidth = Math.max(maxWidth, m.width);
            height += categoryNameHeight + 10;
        }
        if (maxWidth < getWidth() - 3) {
            maxWidth = getWidth() - 3;
        }

        if (ClickGUI.ScrollTypes.values()[ClickGUI.ScrollType.value] == ClickGUI.ScrollTypes.OLD) {
            MAX_HEIGHT = height;
        } else {
            MAX_HEIGHT = (int) Math.round(ClickGUI.CategoryHeight.value);
            if (MAX_HEIGHT > height) {
                MAX_HEIGHT = height;
            }
        }
        if (!collapsed && height >= 10) {
            if (category == Categories.SEARCH) {
                Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x - 2, y + categoryNameHeight + 25, false, false, true, true, width + 4.5f, hitBox.getHeight(), 3, ColorUtils.changeAlpha(new Color(ColorManager.INSTANCE.ClickGuiPrimary()), 100).getRGB());
            } else {
                Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x - 2, y + categoryNameHeight + 6, false, false, true, true, width + 4.5f, hitBox.getHeight(), 3, ColorUtils.changeAlpha(new Color(ColorManager.INSTANCE.ClickGuiPrimary()), 100).getRGB());
            }
        }


        if (dragging) {
            x = mouseX - startX;
            y = mouseY - startY;
        }
        if (collapsed) {
            delay = 0;
        } else {
            update();
            int buttonYOffset = y + 10 + categoryNameHeight - scrollOffset;
            if (category == Categories.SEARCH) {
                buttonYOffset += 17;
            }

            for (ModuleButton m : moduleButtons) {
                if (buttonYOffset >= y + categoryNameHeight && buttonYOffset < y + MAX_HEIGHT) {
                    int animatedY = Math.round(m.getY() + (buttonYOffset - m.getY()) * m.getAnimationProgress());
                    m.render(drawContext, mouseX, mouseY, x, animatedY, maxWidth);
                }
                int settingsHeight = m.renderSettings(drawContext, x, buttonYOffset, mouseX, mouseY, textRenderer);
                buttonYOffset += settingsHeight;
                MAX_HEIGHT = settingsHeight + MAX_HEIGHT;

                buttonYOffset += categoryNameHeight + 10;
            }
        }

        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x - 2, y, width + 4.5f, categoryNameHeight + 8, 3, ColorUtils.changeAlpha(new Color(ColorManager.INSTANCE.ClickGuiPrimary()), 255).getRGB());

        Renderer2D.drawFixedString(drawContext.getMatrices(), category.name, x + (float) (CategoryPane.getWidth() - 4) / 2 - Renderer2D.getFxStringWidth(category.name) / 2, (float) (y + 4), ColorManager.INSTANCE.clickGuiPaneText());
        hitBox.set(x, y, width, MAX_HEIGHT);

        if (svgFile != null) {
            // Gives a very shitty error idk why. Not fixable
            //svgFile.render(drawContext.getMatrices(), x, y,iconWidth,iconHeight);
        }
    }

    public boolean hovered(double mouseX, double mouseY) {
        return mouseX >= x - 2 && mouseX <= x + width + 4.5f && mouseY >= y && mouseY <= y + categoryNameHeight + 8;
    }

    public boolean hoveredOverModules(double mouseX, double mouseY) {
        int categoryNameHeight = (int) Renderer2D.getFxStringHeight(category.name);
        return mouseX > x + 2 && mouseX < x + (width - 2) && mouseY > y + categoryNameHeight + 14 && mouseY < y + height;
    }


    public void mouseClicked(int mouseX, int mouseY, int button) {
        for (ModuleButton moduleButton : moduleButtons) {
            if (moduleButton.mouseClicked(mouseX, mouseY, button, collapsed)) return;
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

    public void mouseScrolled(int mouseX, int mouseY, double amount) {
        if (hoveredOverModules(mouseX, mouseY) && ClickGUI.ScrollTypes.values()[ClickGUI.ScrollType.value] == ClickGUI.ScrollTypes.NEW) {
            int categoryNameHeight = (int) Renderer2D.getFxStringHeight(category.name);
            // Scroll this pane by changing the scroll offset
            scrollOffset += (int) (amount * ClickGUI.ScrollSpeed.value);

            // Clamp the scroll offset to prevent scrolling past the start or end of the modules
            int maxScroll = Math.max(0, moduleButtons.size() * (categoryNameHeight + 10));
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
        for (ModuleButton moduleButton : moduleButtons) {
            if (moduleButton.settingsOpen) {
                for (SettingGroup settingGroup : moduleButton.module.quickSettingGroups) {
                    if (!settingGroup.shouldRender()) continue;
                    settingGroup.mouseReleased(mouseX, mouseY, button);
                }
            }
        }
        dragging = false;
    }

    public void charTyped(char chr, int modifiers) {
        for (ModuleButton moduleButton : moduleButtons) {
            if (moduleButton.settingsOpen) {
                for (SettingGroup settingGroup : moduleButton.module.quickSettingGroups) {
                    if (!settingGroup.shouldRender()) continue;
                    settingGroup.charTyped(chr, modifiers);
                }
            }
        }
    }

    public static int getWidth() {
        return width;
    }
}
