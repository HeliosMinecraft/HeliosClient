package dev.heliosclient.ui.clickgui;

import dev.heliosclient.module.Module_;
import dev.heliosclient.system.ColorManager;
import dev.heliosclient.util.animation.AnimationUtils;
import dev.heliosclient.util.animation.EasingType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class ModuleButton {
    AnimationUtils TextAnimation = new AnimationUtils();
    AnimationUtils BackgroundAnimation = new AnimationUtils();

    public int hoverAnimationTimer;
    public Module_ module;
    public int x, y, width, height = 0;
    private int alpha = 255;
    private int hovertimer = 0;
    private boolean faded = true;
    private Screen parentScreen;
    public boolean settingsOpen = false;
    public ModuleButton(Module_ module, Screen parentScreen) {
        this.module = module;
        this.width = 96;
        this.height = 14;
        this.parentScreen = parentScreen;
        BackgroundAnimation.FADE_SPEED=0.2f;
        TextAnimation.FADE_SPEED=0.2f;
    }

    public void startFading() {
        BackgroundAnimation.startFading(faded,EasingType.LINEAR_IN);
        TextAnimation.startFading(faded,EasingType.LINEAR_IN);
    }
    public void setFaded(boolean faded) {
        this.faded = faded;
    }

    public boolean hasFaded() {
        return faded;
    }

    public void render(DrawContext drawContext, int mouseX, int mouseY, int x, int y, TextRenderer textRenderer) {
        this.x = x;
        this.y = y;

        if (hovered(mouseX, mouseY)) {
            hovertimer++;
            hoverAnimationTimer = Math.min(hoverAnimationTimer + 1, 20);
        } else {
            hovertimer = 0;
            hoverAnimationTimer = Math.max(hoverAnimationTimer - 1, 0);
        }

        int fillColor = (int) (34 + 0.85 * hoverAnimationTimer);
        Color fill = new Color(fillColor, fillColor, fillColor, alpha);

        BackgroundAnimation.drawFadingBox(drawContext,x, y, width, height, fill.getRGB(),false,0);
        TextAnimation.drawFadingText(drawContext,textRenderer,module.name, x + 3, y + 3, module.active.value ? ColorManager.INSTANCE.clickGuiSecondary() : ColorManager.INSTANCE.defaultTextColor(), false);

        if (hovertimer >= 100) {
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
                }
                else if(button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE){
                    this.module.settingsOpen = !this.module.settingsOpen;
                    this.settingsOpen=this.module.settingsOpen;
                    return true;
                }
            }
        }
        return false;
    }

}
