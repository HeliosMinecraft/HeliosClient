package dev.heliosclient.ui.clickgui;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.KeycodeToString;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.animation.AnimationUtils;
import dev.heliosclient.util.animation.EasingType;
import net.minecraft.client.MinecraftClient;
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
    private boolean faded = true;
    public final Screen parentScreen;
    public int boxHeight = 0;


    public ModuleButton(Module_ module, Screen parentScreen) {
        this.module = module;
        this.width = CategoryPane.getWidth() - 2;
        this.height = 16;
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

    public void render(DrawContext drawContext, int mouseX, int mouseY, int x, int y, int maxWidth) {
        this.x = x;
        this.y = y;

        if (hovered(mouseX, mouseY)) {
            hoverAnimationTimer = Math.min(hoverAnimationTimer + 1, 20);
        } else {
            hoverAnimationTimer = Math.max(hoverAnimationTimer - 1, 0);
        }
        // Get the width and height of the module name
        int moduleNameHeight = (int) Renderer2D.getFxStringHeight(module.name) - 1;

        // Adjust the button size based on the text dimensions
        this.width = maxWidth;

        Color fillColor = module.isActive() ? new Color(ColorManager.INSTANCE.clickGuiSecondary()) : ColorUtils.changeAlpha(new Color(ColorManager.INSTANCE.ClickGuiPrimary()), 100);

        BackgroundAnimation.drawFadingBox(drawContext, x + 1, y, maxWidth, height, fillColor.getRGB(), true, 2);
        if (settingsOpen) {
            BackgroundAnimation.drawFadingBox(drawContext, x + 1, y + height, maxWidth, boxHeight, ColorUtils.changeAlpha(fillColor, 100).getRGB(), true, 2);
        }

        int textY = y + (height - moduleNameHeight) / 2;

        TextAnimation.drawFadingText(drawContext.getMatrices(), module.name, x + 3, textY, ColorManager.INSTANCE.defaultTextColor(), true);
        if (hovered(mouseX, mouseY)) {
            Tooltip.tooltip.changeText(module.description);
        }


        if (module.keyBind.value != 0 && ClickGUI.keybinds) {
            String keyName = "[" + KeycodeToString.translateShort(module.keyBind.value) + "]";
            TextAnimation.drawFadingText(drawContext.getMatrices(), keyName.toUpperCase(), (int) (x + width - 3 - Renderer2D.getFxStringWidth(keyName)), textY, ColorManager.INSTANCE.defaultTextColor, true);
        }
    }

    public boolean hovered(double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + width - 3 && mouseY > y && mouseY < y + height;
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

    public void setHeight(int height) {
        this.height = height;
    }

    public void setBoxHeight(int boxHeight) {
        this.boxHeight = boxHeight;
    }
}
