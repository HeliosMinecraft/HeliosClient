package dev.heliosclient.module.test;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

// Main menu class
public class MainMenu extends Screen {
    private List<Module> modules;
    private int x, y;
    private boolean dragging;
    private int dragOffsetX, dragOffsetY;

    public MainMenu(List<Module> modules) {
        super(Text.of("ClickGUI"));
        this.modules = modules;
    }

    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        // Draw the background
        Renderer2D.drawRectangle(drawContext,x, y, x + 100, y + modules.size() * 20 + 10, 0x80000000);

        // Draw the module list
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            module.setPosition(x + 10, y + 10 + i * 20);
            module.render(drawContext, mouseX, mouseY, delta);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle clicks on the module list
        for (Module module : modules) {
            if (module.isMouseOver(mouseX, mouseY)) {
                module.mouseClicked(mouseX, mouseY, button);
                return true;
            }
        }

        // Start dragging if the user clicked on the background
        if (mouseX >= x && mouseX <= x + 100 && mouseY >= y && mouseY <= y + modules.size() * 20 + 10) {
            dragging = true;
            dragOffsetX = (int)(mouseX - x);
            dragOffsetY = (int)(mouseY - y);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        // Update the position of the menu while dragging
        if (dragging) {
            x = (int)(mouseX - dragOffsetX);
            y = (int)(mouseY - dragOffsetY);
            return true;
        }
        return false;
    }

    public boolean mouseReleased(double mouseX,double mouseY,int button){
        // Stop dragging
        dragging = false;
        return true;
    }
}


