package dev.heliosclient.ui.clickgui;

import dev.heliosclient.managers.FontManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.RGBASetting;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.ui.clickgui.gui.AbstractSettingScreen;
import dev.heliosclient.ui.clickgui.gui.Window;
import dev.heliosclient.ui.clickgui.navbar.NavBar;
import dev.heliosclient.util.interfaces.IWindowContentRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.AbstractMap;
import java.util.List;

public class SettingsScreen extends AbstractSettingScreen implements IWindowContentRenderer {
        private final float delayBetweenSettings = 0.2f;
        int x, y, windowWidth = 224, windowHeight;

        public SettingsScreen(Module_ module, Screen parentScreen) {
            super(Text.literal("Settings"),module,0,224);
        }

        public void updateSetting() {
            module.settingGroups.stream()
                    .filter(SettingGroup::shouldRender)
                    .flatMap(settingGroup -> settingGroup.getSettings().stream()
                            .map(setting -> new AbstractMap.SimpleEntry<>(settingGroup, setting)))
                    .filter(entry -> entry.getValue().shouldRender())
                    .forEach(entry -> {
                        SettingGroup settingGroup = entry.getKey();
                        Setting setting = entry.getValue();
                        setting.update(settingGroup.getY());
                        if (!setting.isAnimationDone() && delay <= 0) {
                            delay = delayBetweenSettings;
                            delay -= setting.animationSpeed;
                        }
                    });
        }


        @Override
        public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
            this.renderBackground(drawContext, mouseX, mouseY, delta);

            windowHeight = 50;
            for (SettingGroup settingGroup : module.settingGroups) {
                windowHeight += Math.round(settingGroup.getGroupNameHeight() + 3);
                if (!settingGroup.shouldRender()) continue;
                for (Setting setting : settingGroup.getSettings()) {
                    if (!setting.shouldRender()) continue;
                    setting.quickSettings = false;
                    windowHeight += setting.height + 1;
                }
                windowHeight += Math.round(settingGroup.getGroupNameHeight() + 2);
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
                yOffset += Math.round(settingGroup.getGroupNameHeight() + 3);
                settingGroup.renderBuilder(drawContext, x + 16, yOffset - 3, windowWidth - 32);

                if (settingGroup.shouldRender()) {
                    List<Setting> settings = settingGroup.getSettings();
                    for (Setting setting : settings) {
                        if (setting.shouldRender()) {
                            if (setting instanceof RGBASetting rgbaSetting) {
                                rgbaSetting.setParentScreen(this);
                            }

                            // Update the y position of the setting based on its animation progress
                            int animatedY = Math.round(setting.getY() + (yOffset - setting.getY()) * setting.getAnimationProgress());
                            setting.render(drawContext, x + 16, animatedY + 6, mouseX, mouseY, textRenderer);
                            yOffset += setting.height + 1;
                        } else {
                            resetSettingAnimation(setting);
                        }
                    }
                } else {
                    settingGroup.getSettings().forEach(this::resetSettingAnimation);
                }

                yOffset += Math.round(settingGroup.getGroupNameHeight() + 1);
            }
        }

        private void resetSettingAnimation(Setting setting) {
            setting.animationDone = false;
            delay = 0;
            setting.setAnimationProgress(0.5f);
        }
    }

