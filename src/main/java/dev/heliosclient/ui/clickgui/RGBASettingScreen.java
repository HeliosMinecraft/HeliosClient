package dev.heliosclient.ui.clickgui;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.module.settings.RGBASetting;
import dev.heliosclient.ui.clickgui.gui.Window;
import dev.heliosclient.util.interfaces.IWindowContentRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class RGBASettingScreen extends Screen implements IWindowContentRenderer {
    private final Window window;
    private final RGBASetting setting;
    int windowWidth = 192, windowHeight = 140;

    public RGBASettingScreen(RGBASetting setting) {
        super(Text.literal(setting.name == null ? "NULL" : setting.name));
        this.setting = setting;
        window = new Window(windowHeight, windowWidth, true, this);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        if (this.client.world == null) {
            super.renderBackgroundTexture(drawContext);
        }

        if (textRenderer.getWidth(setting.description) > windowWidth) {
            windowWidth = textRenderer.getWidth(setting.description) + 5;
        } else if (textRenderer.getWidth(setting.name) > windowWidth) {
            windowWidth = textRenderer.getWidth(setting.name) + 30;
        }

        window.setWindowHeight(windowHeight);
        window.setWindowWidth(windowWidth);
        window.render(drawContext, mouseX, mouseY, setting.name, setting.description, textRenderer);


        Tooltip.tooltip.render(drawContext, textRenderer, mouseX, mouseY);
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        window.setBackButtonTask(() -> {
            if (setting.getParentScreen() != null) {
                HeliosClient.MC.setScreen(setting.getParentScreen());
            }
        });
        setting.mouse(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        setting.mouse(mouseX, mouseY, button);
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        window.mouseScrolled(mouseX, mouseY, verticalAmount);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        setting.keyPress(keyCode);
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            setting.height = 25;
            if (setting.getParentScreen() != null && !setting.hexInput.isFocused()) {
                HeliosClient.MC.setScreen(setting.getParentScreen());
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }


    @Override
    protected void init() {
        super.init();
        window.init();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void renderContent(Window window, DrawContext drawContext, int x, int y, int mouseX, int mouseY) {
        if (setting != null) {
            setting.renderSetting(drawContext, x + 3, y + 2, mouseX, mouseY, textRenderer);
        }
    }
}
