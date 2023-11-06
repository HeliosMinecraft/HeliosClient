package dev.heliosclient.ui.clickgui;

import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.KeyBind;
import dev.heliosclient.module.settings.RGBASetting;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.ui.clickgui.ui.Window;
import dev.heliosclient.util.interfaces.IWindowContentRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class SettingsScreen extends Screen implements IWindowContentRenderer {
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    int x, y, windowWidth = 224, windowHeight;
    private final Module_ module;
    private final Screen parentScreen;
    private final Window window;
    private final float delayBetweenSettings = 0.1f;
    float delay = 0;

    public SettingsScreen(Module_ module, Screen parentScreen) {
        super(Text.literal("Settings"));
        this.module = module;
        this.parentScreen = parentScreen;
        window = new Window(100, windowWidth, true, this);
    }

    public void updateSetting() {
        for (SettingGroup settingGroup : module.settingGroups) {
            if (!settingGroup.shouldRender()) continue;
            for (Setting setting : settingGroup.getSettings()) {
                if (!setting.shouldRender()) continue;
                setting.update(settingGroup.getY());
                if (!setting.isAnimationDone()) {
                    if (delay <= 0) {
                        delay = delayBetweenSettings;
                    }
                    delay -= setting.animationSpeed;
                }
            }
        }
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext);

        windowHeight = 45;
        for (SettingGroup settingGroup : module.settingGroups) {
            windowHeight += Math.round(settingGroup.getGroupNameHeight() + 10);
            if (!settingGroup.shouldRender()) continue;
            for (Setting setting : settingGroup.getSettings()) {
                if (!setting.shouldRender()) continue;
                setting.quickSettings = false;
                windowHeight += setting.height + 1;
            }
            windowHeight += Math.round(settingGroup.getGroupNameHeight());
        }

        window.setWindowHeight(windowHeight);
        window.render(drawContext, mouseX, mouseY, module.name, module.description, textRenderer);


        x = window.getX();
        y = window.getY();

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
        window.mouseClicked(mouseX, mouseY);
        for (SettingGroup settingGroup : module.settingGroups) {
            settingGroup.mouseClickedBuilder(mouseX, mouseY);
            if (!settingGroup.shouldRender()) continue;
            settingGroup.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void init() {
        super.init();
        window.init();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (SettingGroup settingGroup : module.settingGroups) {
            if (!settingGroup.shouldRender()) continue;
            settingGroup.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (SettingGroup settingGroup : module.settingGroups) {
            if (!settingGroup.shouldRender()) continue;
            settingGroup.keyReleased(keyCode, scanCode, modifiers);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && !KeyBind.listeningKey) {
            MinecraftClient.getInstance().setScreen(parentScreen);
        }
        for (SettingGroup settingGroup : module.settingGroups) {
            if (!settingGroup.shouldRender()) continue;
            settingGroup.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        window.mouseScrolled(mouseX, mouseY, amount);
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (SettingGroup settingGroup : module.settingGroups) {
            if (!settingGroup.shouldRender()) continue;
            settingGroup.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (SettingGroup settingGroup : module.settingGroups) {
            if (!settingGroup.shouldRender()) continue;
            settingGroup.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void renderContent(Window window, DrawContext drawContext, int x, int y, int mouseX, int mouseY) {
        updateSetting();

        int yOffset = y;
        for (SettingGroup settingGroup : module.settingGroups) {
            yOffset += Math.round(settingGroup.getGroupNameHeight() + 10);
            settingGroup.renderBuilder(drawContext, x + 16, yOffset - 3, windowWidth - 32);
            if (!settingGroup.shouldRender()) {
                for (Setting setting : settingGroup.getSettings()) {
                    setting.animationDone = false;
                    delay = 0;
                    setting.setAnimationProgress(0.5f);
                }
                continue;
            }

            for (Setting setting : settingGroup.getSettings()) {
                if (!setting.shouldRender()) {
                    setting.animationDone = false;
                    delay = 0;
                    setting.setAnimationProgress(0.5f);
                    continue;
                }
                if (setting instanceof RGBASetting rgbaSetting) {
                    rgbaSetting.setParentScreen(this);
                }

                // Update the y position of the setting based on its animation progress
                int animatedY = Math.round(setting.getY() + (yOffset - setting.getY()) * setting.getAnimationProgress());
                setting.render(drawContext, x + 16, animatedY + 3, mouseX, mouseY, textRenderer);
                yOffset += setting.height + 1;
            }

            yOffset += Math.round(settingGroup.getGroupNameHeight() + 1);
        }
    }
}
