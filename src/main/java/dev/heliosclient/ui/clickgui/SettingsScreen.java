package dev.heliosclient.ui.clickgui;

import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.StringListSetting;
import dev.heliosclient.module.settings.StringSetting;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.system.ColorManager;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.animation.Easing;
import dev.heliosclient.util.animation.EasingType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class SettingsScreen extends Screen {
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    static int offsetY = 0;
    public TextButton backButton = new TextButton("< Back");

    int x, y, windowWidth = 224, windowHeight;
    private final Module_ module;
    private final Screen parentScreen;

    public SettingsScreen(Module_ module, Screen parentScreen) {
        super(Text.literal("Settings"));
        this.module = module;
        offsetY = 0;
        this.parentScreen = parentScreen;
    }

    public static void onScroll(double horizontal, double vertical) {
        offsetY += vertical * (Easing.ease(EasingType.QUADRATIC_IN, (float) ClickGUI.ScrollSpeed.value));
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext);

        windowHeight = 52;
        for (Setting setting : module.settings) {
            if (!setting.shouldRender()) continue;
            setting.quickSettings = false;
            windowHeight += setting.height + 1;
        }

        int screenHeight = drawContext.getScaledWindowHeight();

        if (drawContext.getScaledWindowHeight() > windowHeight) {
            offsetY = 0;
            y = drawContext.getScaledWindowHeight() / 2 - (windowHeight) / 2;
        } else {
            offsetY = Math.max(Math.min(offsetY, 0), screenHeight - windowHeight);
            y = offsetY;
        }

        x = Math.max(drawContext.getScaledWindowWidth() / 2 - windowWidth / 2, 0);

        Renderer2D.drawRoundedRectangle(drawContext, x, y, windowWidth, windowHeight, 5, 0xFF222222);
        Renderer2D.drawRoundedRectangle(drawContext, x, y, true, true, false, false, windowWidth, 18, 5, 0xFF1B1B1B);
        Renderer2D.drawRectangle(drawContext, x, y + 16, windowWidth, 2, ColorManager.INSTANCE.clickGuiSecondary());
        drawContext.drawText(textRenderer, module.name, drawContext.getScaledWindowWidth() / 2 - textRenderer.getWidth(module.name) / 2, y + 4, ColorManager.INSTANCE.clickGuiPaneText(), false);
        drawContext.drawText(textRenderer, "§o" + module.description, drawContext.getScaledWindowWidth() / 2 - textRenderer.getWidth("§o" + module.description) / 2, y + 26, ColorManager.INSTANCE.defaultTextColor(), false);
        backButton.render(drawContext, textRenderer, x + 4, y + 4, mouseX, mouseY);
        int yOffset = y + 44;
        for (Setting setting : module.settings) {
            if (!setting.shouldRender()) continue;
            setting.render(drawContext, x + 16, yOffset, mouseX, mouseY, textRenderer);
            yOffset += setting.height + 1;
        }
        Tooltip.tooltip.render(drawContext, textRenderer, mouseX, mouseY);
    }

    public boolean barHovered(double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + 16;
    }

    @Override
    public boolean shouldPause() {
        return ClickGUI.pause;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        backButton.mouseClicked((int) mouseX, (int) mouseY);
        for (Setting setting : module.settings) {
            setting.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Setting setting : module.settings) {
            setting.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (Setting setting : module.settings) {
            setting.keyReleased(keyCode, scanCode, modifiers);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            MinecraftClient.getInstance().setScreen(parentScreen);
        }
        for (Setting setting : module.settings) {
            setting.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (Setting setting : module.settings) {
            setting.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
