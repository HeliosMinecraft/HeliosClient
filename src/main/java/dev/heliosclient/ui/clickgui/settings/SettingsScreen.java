package dev.heliosclient.ui.clickgui.settings;

import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.system.HeliosExecutor;
import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.ui.clickgui.gui.AbstractSettingScreen;
import dev.heliosclient.ui.clickgui.gui.Window;
import dev.heliosclient.util.interfaces.IWindowContentRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.AbstractMap;
import java.util.List;

public class SettingsScreen extends AbstractSettingScreen implements IWindowContentRenderer {
    private final float delayBetweenSettings = 0.2f;
    int x, y, windowWidth = 224, windowHeight;

    public SettingsScreen(Module_ module, Screen parentScreen) {
        super(Text.literal(module.name + " Settings"), module, 0, 224);
        updateSetting();
    }

    public void updateSetting() {
        HeliosExecutor.execute(() -> module.settingGroups.stream()
                .filter(SettingGroup::shouldRender)
                .flatMap(settingGroup -> settingGroup.getSettings().stream()
                        .map(setting -> new AbstractMap.SimpleEntry<>(settingGroup, setting)))
                .filter(entry -> entry.getValue().shouldRender())
                .forEach(entry -> {
                    SettingGroup settingGroup = entry.getKey();
                    Setting<?> setting = entry.getValue();
                    setting.update(settingGroup.getY());
                    if (!setting.isAnimationDone() && delay.get() <= 0) {
                        delay.set(delayBetweenSettings);
                        delay.set(delay.get() - setting.getAnimationSpeed());
                    }
                }));
    }


    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        if (this.client.world == null) {
            super.renderBackgroundTexture(drawContext);
        }

        windowHeight = 50;
        for (SettingGroup settingGroup : module.settingGroups) {
            windowHeight += Math.round(settingGroup.getGroupNameHeight() + 13);
            if (!settingGroup.shouldRender()) continue;
            for (Setting<?> setting : settingGroup.getSettings()) {
                if (!setting.shouldRender()) continue;
                setting.quickSettings = false;
                windowHeight += setting.getHeight() + 1;
            }
            windowHeight += 8;
        }

        window.setWindowHeight(windowHeight);
        window.setWindowWidth(windowWidth);
        window.render(drawContext, mouseX, mouseY, module.name, module.description, textRenderer);

        x = window.getX();
        y = window.getY();

        Tooltip.tooltip.render(drawContext, textRenderer, mouseX, mouseY);
    }

    @Override
    public void renderContent(Window window, DrawContext drawContext, int x, int y, int mouseX, int mouseY) {
        updateSetting();

        int yOffset = y;
        for (SettingGroup settingGroup : module.settingGroups) {
            yOffset += Math.round(settingGroup.getGroupNameHeight() + 12);
            settingGroup.renderBuilder(drawContext, x + 16, yOffset - 3, windowWidth - 32);

            if (settingGroup.shouldRender()) {
                List<Setting<?>> settings = settingGroup.getSettings();
                for (Setting<?> setting : settings) {
                    if (setting.shouldRender()) {
                        if (setting instanceof ParentScreenSetting<?> pcs) {
                            pcs.setParentScreen(this);
                        }

                        // Update the y position of the setting based on its animation progress
                        int animatedY = Math.round(setting.getY() + (yOffset - setting.getY()) * setting.getAnimationProgress());
                        setting.render(drawContext, x + 16, animatedY + 6, mouseX, mouseY, textRenderer);
                        yOffset += setting.getHeight() + 1;
                    } else {
                        resetSettingAnimation(setting);
                    }
                }
            } else {
                settingGroup.getSettings().forEach(this::resetSettingAnimation);
            }

            yOffset += Math.round(settingGroup.getGroupNameHeight() + 3);
        }
    }

    private void resetSettingAnimation(Setting<?> setting) {
        setting.setAnimationDone(false);
        delay.set(0);
        setting.setAnimationProgress(0.5f);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            mc.setScreen(ClickGUIScreen.INSTANCE);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}

