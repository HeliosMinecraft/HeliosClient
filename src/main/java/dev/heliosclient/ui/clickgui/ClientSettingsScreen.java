package dev.heliosclient.ui.clickgui;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.ui.clickgui.navbar.NavBar;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.animation.Easing;
import dev.heliosclient.util.animation.EasingType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;


public class ClientSettingsScreen extends Screen {
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    static int offsetY = 0;
    public NavBar navBar = new NavBar();

    int x, y, windowWidth = 224, windowHeight;
    private final Module_ module;

    public ClientSettingsScreen(Module_ module) {
        super(Text.literal("Client Settings"));
        this.module = module;
        offsetY = 0;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        offsetY += (int) (amount * (Easing.ease(EasingType.QUADRATIC_IN_OUT, (float) ClickGUI.ScrollSpeed.value)));
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext);

        int halfWindowWidth = drawContext.getScaledWindowWidth() / 2;

        windowHeight = 45;
        for (SettingGroup settingBuilder : module.settingGroups) {
            windowHeight += Math.round(settingBuilder.getGroupNameHeight() + 3);
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
            offsetY = Math.max(Math.min(offsetY, 12), screenHeight - windowHeight);
            y = offsetY;
        }

        x = Math.max(drawContext.getScaledWindowWidth() / 2 - windowWidth / 2, 0);

        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y, windowWidth, windowHeight, 5, 0xFF222222);
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y, windowWidth, 18, 5, 0xFF1B1B1B);
        Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y + 16, windowWidth, 2, ColorManager.INSTANCE.clickGuiSecondary());


        drawContext.drawText(textRenderer, module.name, halfWindowWidth - textRenderer.getWidth(module.name) / 2, y + 4, ColorManager.INSTANCE.clickGuiPaneText(), false);

        //Render module description wrapped to lines, so it doesn't appear out of the window
        Renderer2D.wrapText("§o" + module.description, windowWidth).forEach(text -> {
            int warpedTextWidth = textRenderer.getWidth(text);
            drawContext.drawText(textRenderer, text, halfWindowWidth - warpedTextWidth / 2, y + 26, ColorManager.INSTANCE.defaultTextColor(), false);
            windowHeight += warpedTextWidth + 2;
        });

        int yOffset = y + 44;
        for (SettingGroup settingBuilder : module.settingGroups) {
            yOffset += Math.round(settingBuilder.getGroupNameHeight() + 3);
            settingBuilder.renderBuilder(drawContext, x + 16, yOffset - 3, windowWidth - 32);
            if (!settingBuilder.shouldRender()) continue;
            for (Setting setting : settingBuilder.getSettings()) {
                if (!setting.shouldRender()) continue;
                setting.render(drawContext, x + 16, yOffset, mouseX, mouseY, textRenderer);
                yOffset += setting.height + 1;
            }
            yOffset += Math.round(settingBuilder.getGroupNameHeight() + 1);
        }
        navBar.render(drawContext, textRenderer, mouseX, mouseY);
        Tooltip.tooltip.render(drawContext, textRenderer, mouseX, mouseY);
        FontManager.fontSize = ((int) ClickGUI.FontSize.value);
    }

    @Override
    public boolean shouldPause() {
        return ClickGUI.pause;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (SettingGroup settingBuilder : module.settingGroups) {
            settingBuilder.mouseClickedBuilder(mouseX, mouseY);
            if (!settingBuilder.shouldRender()) continue;
            settingBuilder.mouseClicked(mouseX, mouseY, button);
        }
        navBar.mouseClicked((int) mouseX, (int) mouseY, button);
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (SettingGroup settingBuilder : module.settingGroups) {
            if (!settingBuilder.shouldRender()) continue;
            settingBuilder.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (SettingGroup settingBuilder : module.settingGroups) {
            if (!settingBuilder.shouldRender()) continue;
            settingBuilder.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }
}
