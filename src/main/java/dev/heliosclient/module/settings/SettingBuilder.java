package dev.heliosclient.module.settings;


import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class SettingBuilder {
    private final String name;
    private List<Setting> settings = new ArrayList<>();
    private float height = 0;
    private float groupNameHeight = 0;
    private boolean shouldRender = true;
    private int x, y, windowWidth;

    public SettingBuilder(String name) {
        this.name = name;
        height = Renderer2D.getStringHeight(name);
        groupNameHeight = Renderer2D.getStringHeight(name);
    }

    public <T extends Setting> T add(T setting) {
        settings.add(setting);
        height += setting.height;
        return setting;
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        for (Setting setting : settings) {
            if (setting.shouldRender()) {
                setting.mouseClicked(mouseX, mouseY, button);
            }
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        for (Setting setting : settings) {
            if (setting.shouldRender()) {
                setting.mouseReleased(mouseX, mouseY, button);
            }
        }
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Setting setting : settings) {
            if (setting.shouldRender()) {
                setting.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (Setting setting : settings) {
            if (setting.shouldRender()) {
                setting.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            }
        }
    }

    public void keyReleased(int keyCode, int scanCode, int modifiers) {
        for (Setting setting : settings) {
            if (setting.shouldRender()) {
                setting.keyReleased(keyCode, scanCode, modifiers);
            }
        }
    }

    public void charTyped(char chr, int modifiers) {
        for (Setting setting : settings) {
            if (setting.shouldRender()) {
                setting.charTyped(chr, modifiers);
            }
        }
    }

    public void renderBuilder(DrawContext drawContext, int x, int yOffset, int windowWidth) {
        this.x = x;
        this.y = yOffset;
        this.windowWidth = windowWidth;

        // Draw the horizontal line and the name of the setting builder
        drawContext.drawHorizontalLine(x + 16, (int) (x + windowWidth / 2 - Renderer2D.getFxStringWidth(this.getName()) / 2) - 1, yOffset, ColorManager.INSTANCE.clickGuiSecondary());
        Renderer2D.drawFixedString(drawContext.getMatrices(), this.getName(), (int) (x + windowWidth / 2 - Renderer2D.getFxStringWidth(this.getName()) / 2) + 1, (int) (yOffset - Renderer2D.getFxStringHeight() / 2), ColorManager.INSTANCE.clickGuiPaneText());
        drawContext.drawHorizontalLine((int) ((x + windowWidth / 2 - Renderer2D.getFxStringWidth(this.getName()) / 2) + Renderer2D.getFxStringWidth(this.getName())) + 2, x + windowWidth - 17, yOffset, ColorManager.INSTANCE.clickGuiSecondary());

    }

    public void mouseClickedBuilder(double mouseX, double mouseY) {
        if (hoveredOverGroup((int) mouseX, (int) mouseY)) {
            this.shouldRender = !this.shouldRender;
        }
    }

    public boolean hoveredOverGroup(int mouseX, int mouseY) {
        return mouseX > x + 16 && mouseX < x + windowWidth - 16 && mouseY > y - 4 && mouseY < y + 4;
    }

    public List<Setting> getSettings() {
        return settings;
    }

    public void setSettings(List<Setting> settings) {
        this.settings = settings;
    }

    public String getName() {
        return name;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getGroupNameHeight() {
        return groupNameHeight;
    }

    public boolean shouldRender() {
        return shouldRender;
    }

    public void setShouldRender(boolean shouldRender) {
        this.shouldRender = shouldRender;
    }
}