package dev.heliosclient.ui.clickgui;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.KeyBind;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.animation.Easing;
import dev.heliosclient.util.animation.EasingType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        offsetY += (int) (amount * (Easing.ease(EasingType.QUADRATIC_IN, (float) ClickGUI.ScrollSpeed.value)));
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext);

        int halfWindowWidth = drawContext.getScaledWindowWidth() / 2;


        windowHeight = 52;
        for (SettingGroup settingBuilder : module.settingGroups) {
            windowHeight += Math.round(settingBuilder.getGroupNameHeight() + 10);
            if (!settingBuilder.shouldRender()) continue;
            for (Setting setting : settingBuilder.getSettings()) {
                if (!setting.shouldRender()) continue;
                setting.quickSettings = false;
                windowHeight += setting.height + 1;
            }
            windowHeight += Math.round(settingBuilder.getGroupNameHeight());
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

        // Draw the screen
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y, windowWidth, windowHeight, 5, ColorUtils.changeAlpha(new Color(ColorManager.INSTANCE.clickGuiPrimary), 180).getRGB());
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y, true, true, false, false, windowWidth, 18, 5, ColorManager.INSTANCE.clickGuiPrimary);
        Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y + 16, windowWidth, 2, ColorManager.INSTANCE.clickGuiSecondary());

        //Render module name and description
        String warpedText = Renderer2D.wrapText("§o" + module.description, windowWidth, textRenderer);
        int warpedTextWidth = textRenderer.getWidth(warpedText);

        drawContext.drawText(textRenderer, module.name, halfWindowWidth - textRenderer.getWidth(module.name) / 2, y + 4, ColorManager.INSTANCE.clickGuiPaneText(), false);
        drawContext.drawText(textRenderer, warpedText, halfWindowWidth - warpedTextWidth / 2, y + 26, ColorManager.INSTANCE.defaultTextColor(), false);

        // Render the back button
        backButton.render(drawContext, textRenderer, x + 4, y + 4, mouseX, mouseY);

        int yOffset = y + 44;
        for (SettingGroup settingBuilder : module.settingGroups) {
            yOffset += Math.round(settingBuilder.getGroupNameHeight() + 10);
            settingBuilder.renderBuilder(drawContext, x + 16, yOffset - 3, windowWidth - 32);
            if (!settingBuilder.shouldRender()) continue;
            for (Setting setting : settingBuilder.getSettings()) {
                if (!setting.shouldRender()) continue;
                setting.render(drawContext, x + 16, yOffset + 3, mouseX, mouseY, textRenderer);
                yOffset += setting.height + 1;
            }
            yOffset += Math.round(settingBuilder.getGroupNameHeight() + 1);
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
        for (SettingGroup settingBuilder : module.settingGroups) {
            settingBuilder.mouseClickedBuilder(mouseX, mouseY);
            if (!settingBuilder.shouldRender()) continue;
            settingBuilder.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (SettingGroup settingBuilder : module.settingGroups) {
            if (!settingBuilder.shouldRender()) continue;
            settingBuilder.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (SettingGroup settingBuilder : module.settingGroups) {
            if (!settingBuilder.shouldRender()) continue;
            settingBuilder.keyReleased(keyCode, scanCode, modifiers);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && !KeyBind.listeningKey) {
            MinecraftClient.getInstance().setScreen(parentScreen);
        }
        for (SettingGroup settingBuilder : module.settingGroups) {
            if (!settingBuilder.shouldRender()) continue;
            settingBuilder.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (SettingGroup settingBuilder : module.settingGroups) {
            if (!settingBuilder.shouldRender()) continue;
            settingBuilder.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (SettingGroup settingBuilder : module.settingGroups) {
            if (!settingBuilder.shouldRender()) continue;
            settingBuilder.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
