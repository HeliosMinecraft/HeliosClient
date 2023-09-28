package dev.heliosclient.ui.clickgui;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.util.animation.AnimationUtils;
import dev.heliosclient.util.animation.EasingType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class ModuleButton {
    public int hoverAnimationTimer;
    public Module_ module;
    public int x, y, width, height = 0;
    public boolean settingsOpen = false;
    AnimationUtils TextAnimation = new AnimationUtils();
    AnimationUtils BackgroundAnimation = new AnimationUtils();
    private final int alpha = 255;
    private final int hovertimer = 0;
    private boolean faded = true;
    private final Screen parentScreen;

    public ModuleButton(Module_ module, Screen parentScreen) {
        this.module = module;
        this.width = (int) FontManager.fxfontRenderer.getStringWidth(module.name) + 4;
        this.height = 14;
        this.parentScreen = parentScreen;
        BackgroundAnimation.FADE_SPEED = 0.2f;
        TextAnimation.FADE_SPEED = 0.2f;
    }


    public void startFading() {
        BackgroundAnimation.startFading(faded, EasingType.LINEAR_IN);
        TextAnimation.startFading(faded, EasingType.LINEAR_IN);
    }

    public void setFaded(boolean faded) {
        this.faded = faded;
    }

    public boolean hasFaded() {
        return faded;
    }

    public void render(DrawContext drawContext, int mouseX, int mouseY, int x, int y,int maxWidth) {
        this.x = x;
        this.y = y;

        if (hovered(mouseX, mouseY)) {
            hoverAnimationTimer = Math.min(hoverAnimationTimer + 1, 20);
        } else {
            hoverAnimationTimer = Math.max(hoverAnimationTimer - 1, 0);
        }
        // Get the width and height of the module name
        int moduleNameHeight = (int) FontManager.fxfontRenderer.getStringHeight(module.name);

        // Adjust the button size based on the text dimensions
        this.height = moduleNameHeight + 6;
        this.width = maxWidth;

        int fillColor = (int) (34 + 0.85 * hoverAnimationTimer);
        Color fill = new Color(fillColor, fillColor, fillColor, alpha);

        BackgroundAnimation.drawFadingBox(drawContext, x, y, maxWidth + 4 , height, fill.getRGB(), false, 0);
        TextAnimation.drawFadingText(drawContext.getMatrices(),  module.name, x + 3, y + moduleNameHeight/4 + 1, module.active.value ? ColorManager.INSTANCE.clickGuiSecondary() : ColorManager.INSTANCE.defaultTextColor(),true);
        if (hovered(mouseX, mouseY)) {
            Tooltip.tooltip.changeText(module.description);
        }
    }

    public boolean hovered(double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button, boolean collapsed) {
        if (!collapsed) {
            if (hovered(mouseX, mouseY)) {
                if (button == 0) {
                    module.toggle();
                    return true;
                } else if (button == 1) {
                    MinecraftClient.getInstance().setScreen(new SettingsScreen(module, parentScreen));
                    return true;
                } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                    this.module.settingsOpen = !this.module.settingsOpen;
                    this.settingsOpen = this.module.settingsOpen;
                    return true;
                }
            }
        }
        return false;
    }

}
