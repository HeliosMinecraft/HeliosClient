package dev.heliosclient.module.settings;


import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class SettingGroup {
    private final String name;
    private List<Setting> settings = new ArrayList<>();
    private float height;
    private final float groupNameHeight;
    private boolean shouldRender = true;
    private int x, y, windowWidth;

    public SettingGroup(String name) {
        this.name = name;
        height = Renderer2D.getStringHeight(name);
        groupNameHeight = Renderer2D.getStringHeight(name);
    }

    public <T extends Setting> T add(T setting) {
        settings.add(setting);
        height += setting.height + 1;
        return setting;
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        for (Setting setting : settings) {
            if (setting.shouldRender() && setting.getAnimationProgress() > 0.9f) {
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
        drawContext.drawHorizontalLine(x, (int) (x + windowWidth / 2 - Renderer2D.getFxStringWidth(this.getName()) / 2) - 1, yOffset, ColorManager.INSTANCE.clickGuiSecondary());
        Renderer2D.drawFixedString(drawContext.getMatrices(), this.getName(), (int) (x + windowWidth / 2 - Renderer2D.getFxStringWidth(this.getName()) / 2) + 1, (int) (yOffset - Renderer2D.getFxStringHeight() / 2), -1);
        drawContext.drawHorizontalLine((int) ((x + windowWidth / 2 - Renderer2D.getFxStringWidth(this.getName()) / 2) + Renderer2D.getFxStringWidth(this.getName())) + 2, x + windowWidth, yOffset, ColorManager.INSTANCE.clickGuiSecondary());
        Renderer2D.drawFixedString(drawContext.getMatrices(), shouldRender ? "▾" : "▴", x + windowWidth + 1, (int) (yOffset - Renderer2D.getFxStringHeight() / 2), ColorManager.INSTANCE.clickGuiPaneText());
    }

    public void mouseClickedBuilder(double mouseX, double mouseY) {
        if (hoveredOverGroup((int) mouseX, (int) mouseY)) {
            this.shouldRender = !this.shouldRender;
        }
    }

    public boolean hoveredOverGroup(int mouseX, int mouseY) {
        return mouseX > x && mouseX < x + windowWidth && mouseY > y - 4 && mouseY < y + 4;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWindowWidth() {
        return windowWidth;
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