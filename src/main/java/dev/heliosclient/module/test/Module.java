package dev.heliosclient.module.test;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

// Module class
public class Module {
    private String name;
    private boolean enabled;
    private List<Setting> settings = new ArrayList<>();
    private boolean settingsOpen;
    private int x, y, width, height;


    public Module(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
        this.width = 100;
        this.height = 20;
    }

    public void setSettings(List<Setting> settings) {
        this.settings = settings;
    }
    public void addSetting(Setting setting){
        this.settings.add(setting);
    }

    public List<Setting> getSettings() {
        return settings;
    }

    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        // Draw the module name
        drawContext.drawText(HeliosClient.MC.textRenderer, name, x + 2, y + 2, 0xFFFFFF, false);

        // Draw the enable/disable button
        Renderer2D.drawRectangle(drawContext, x + 80, y + 2, 98, 16, enabled ? 0xFF00FF00 : 0xFFFF0000);

        // Draw the settings if they are open
        if (settingsOpen) {
            for (int i = 0; i < settings.size(); i++) {
                Setting setting = settings.get(i);
                int sx = x + 10;
                int sy = y + height + i * 20;
                setting.setPosition(x,y);
                setting.render(drawContext, sx, sy);
            }
        }
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }


    public boolean isMouseOver(double mouseX, double mouseY) {
        // Check if the mouse is over this module
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        // Handle clicks on the enable/disable button
        if (mouseX >= x + width - 20 && mouseX <= x + width - 2 && mouseY >= y + 2 && mouseY <= y + height - 2) {
            enabled = !enabled;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            settingsOpen = !settingsOpen;
        }

        // Handle clicks on the settings
        if (settingsOpen) {
            for (Setting setting : settings) {
                setting.mouseClicked(mouseX - x - 10, mouseY - y - height, button);
            }
        }
    }
}
