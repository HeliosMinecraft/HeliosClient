package dev.heliosclient.ui.clickgui.hudeditor;

import dev.heliosclient.hud.HudElement;
import dev.heliosclient.module.settings.ListSetting;
import dev.heliosclient.module.settings.RGBASetting;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.ui.clickgui.gui.AbstractSettingScreen;
import dev.heliosclient.ui.clickgui.gui.Window;
import dev.heliosclient.util.interfaces.IWindowContentRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class HudEditorSettingScreen extends AbstractSettingScreen implements IWindowContentRenderer {

    int x, y, windowWidth = 224, windowHeight;

    public HudEditorSettingScreen(HudElement hudElement, int windowHeight, int windowWidth) {
        super(Text.of(hudElement.name), hudElement, 0, 224);
        updateSetting();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        window.setBackButtonTask(() -> mc.setScreen(HudEditorScreen.INSTANCE));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        if (this.client.world == null) {
            super.renderBackgroundTexture(drawContext);
        }

        windowHeight = 50;
        for (SettingGroup settingGroup : hudElement.settingGroups) {
            windowHeight += Math.round(settingGroup.getGroupNameHeight() + 13);
            if (!settingGroup.shouldRender()) continue;
            for (Setting<?> setting : settingGroup.getSettings()) {
                if (!setting.shouldRender()) continue;
                setting.quickSettings = false;
                windowHeight += setting.getHeight() + 1;
            }
            windowHeight += Math.round(settingGroup.getGroupNameHeight() + 2);
        }

        window.setWindowHeight(windowHeight);
        window.setWindowWidth(windowWidth);
        window.render(drawContext, mouseX, mouseY, hudElement.name, hudElement.description, textRenderer);

        x = window.getX();
        y = window.getY();

        Tooltip.tooltip.render(drawContext, textRenderer, mouseX, mouseY);
    }

    @Override
    public void updateSetting() {

    }

    @Override
    public void renderContent(Window window, DrawContext drawContext, int x, int y, int mouseX, int mouseY) {
        int yOffset = y;
        for (SettingGroup settingGroup : hudElement.settingGroups) {
            yOffset += Math.round(settingGroup.getGroupNameHeight() + 12);
            settingGroup.renderBuilder(drawContext, x + 16, yOffset - 3, windowWidth - 32);

            if (settingGroup.shouldRender()) {
                List<Setting<?>> settings = settingGroup.getSettings();
                for (Setting<?> setting : settings) {
                    if (setting.shouldRender()) {
                        if (setting instanceof RGBASetting rgbaSetting) {
                            rgbaSetting.setParentScreen(this);
                        } else if (setting instanceof ListSetting listSetting) {
                            listSetting.setParentScreen(this);
                        }
                        setting.render(drawContext, x + 16, yOffset + 6, mouseX, mouseY, textRenderer);
                        yOffset += setting.getHeight() + 1;
                        setting.setAnimationProgress(1f);
                    } else {
                        setting.setAnimationProgress(0f);
                    }
                }
            } else {
                settingGroup.getSettings().forEach(setting1 -> setting1.setAnimationProgress(0f));
            }
            yOffset += Math.round(settingGroup.getGroupNameHeight() + 3);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            mc.setScreen(HudEditorScreen.INSTANCE);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}

