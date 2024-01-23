package dev.heliosclient.ui.clickgui;

import dev.heliosclient.managers.FontManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.ListSetting;
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
import net.minecraft.text.Text;

import java.util.AbstractMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ClientSettingsScreen extends AbstractSettingScreen implements IWindowContentRenderer {
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    private final float delayBetweenSettings = 0.2f;
    private final ExecutorService executorUpdate = Executors.newSingleThreadExecutor();
    private final ExecutorService executorReset = Executors.newSingleThreadExecutor();
    public NavBar navBar = new NavBar();
    int x, y, windowWidth = 224, windowHeight;

    public ClientSettingsScreen(Module_ module) {
        super(Text.literal("Client Settings"), module, 0, 224);
        window.setCollapsible(false);
    }

    public void updateSetting() {
        synchronized (executorUpdate) {
            executorUpdate.submit(() -> module.settingGroups.stream()
                    .filter(SettingGroup::shouldRender)
                    .flatMap(settingGroup -> settingGroup.getSettings().stream()
                            .map(setting -> new AbstractMap.SimpleEntry<>(settingGroup, setting)))
                    .filter(entry -> entry.getValue().shouldRender())
                    .forEach(entry -> {
                        SettingGroup settingGroup = entry.getKey();
                        Setting setting = entry.getValue();
                        setting.update(settingGroup.getY());
                        if (!setting.isAnimationDone() && delay.get() <= 0.0f) {
                            delay.set(delayBetweenSettings);
                            delay.set(delay.get() - setting.animationSpeed);
                        }
                    }));
        }
    }


    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext, mouseX, mouseY, delta);

        windowHeight = 50;
        for (SettingGroup settingGroup : module.settingGroups) {
            float groupNameHeight = settingGroup.getGroupNameHeight();
            windowHeight += Math.round(groupNameHeight + 12);
            if (!settingGroup.shouldRender()) continue;
            for (Setting setting : settingGroup.getSettings()) {
                if (!setting.shouldRender()) continue;
                setting.quickSettings = false;
                windowHeight += setting.height + 1;
            }
            windowHeight += Math.round(groupNameHeight + 4);
        }

        window.setWindowHeight(windowHeight);
        window.setWindowWidth(windowWidth);
        window.setY(20);
        window.render(drawContext, mouseX, mouseY, module.name, module.description, textRenderer);
        window.setY(20);

        x = window.getX();
        y = window.getY();

        navBar.render(drawContext, textRenderer, mouseX, mouseY);
        Tooltip.tooltip.render(drawContext, textRenderer, mouseX, mouseY);
        FontManager.fontSize = (int) ClickGUI.FontSize.value;
    }

    @Override
    public void renderContent(Window window, DrawContext drawContext, int x, int y, int mouseX, int mouseY) {
        // This should not happen but just incase.
        if (module.settingGroups == null) return;

        updateSetting();

        int yOffset = y;
        for (SettingGroup settingGroup : module.settingGroups) {
            float groupNameHeight = settingGroup.getGroupNameHeight();
            yOffset += Math.round(groupNameHeight + 12);
            settingGroup.renderBuilder(drawContext, x + 16, yOffset - 3, windowWidth - 32);

            if (settingGroup.shouldRender()) {
                List<Setting> settings = settingGroup.getSettings();
                for (Setting setting : settings) {
                    if (setting.shouldRender()) {
                        if (setting instanceof RGBASetting rgbaSetting) {
                            rgbaSetting.setParentScreen(this);
                        } else if (setting instanceof ListSetting listSetting) {
                            listSetting.setParentScreen(this);
                        }

                        // Update the y position of the setting based on its animation progress
                        int animatedY = Math.round(setting.getY() + (yOffset - setting.getY()) * setting.getAnimationProgress());
                        if (animatedY <= yOffset + setting.height + 5 && animatedY >= yOffset - 5) {
                            setting.render(drawContext, x + 16, animatedY + 6, mouseX, mouseY, textRenderer);
                        }
                        yOffset += setting.height + 1;
                    } else {
                        resetSettingAnimation(setting, settingGroup);
                    }
                }
            } else {
                settingGroup.getSettings().forEach(setting1 -> resetSettingAnimation(setting1, settingGroup));
            }

            yOffset += Math.round(groupNameHeight + 3);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        navBar.mouseClicked((int) mouseX, (int) mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void resetSettingAnimation(Setting setting, SettingGroup settingGroup) {
        setting.animationDone = false;
        setting.setAnimationProgress(0);
        synchronized (executorReset) {
            delay.set(0);
            executorReset.submit(() -> {
                setting.reset(settingGroup.getY());
                if (setting.isAnimationDone() && delay.get() <= 0) {
                    delay.set(delayBetweenSettings);
                    delay.set(delay.get() - setting.animationSpeed);
                }
            });
        }
        setting.setAnimationProgress(0.5f);
    }
}
