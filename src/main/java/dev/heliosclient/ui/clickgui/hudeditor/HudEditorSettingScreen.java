package dev.heliosclient.ui.clickgui.hudeditor;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.module.settings.ListSetting;
import dev.heliosclient.module.settings.RGBASetting;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.Space;
import dev.heliosclient.ui.clickgui.gui.AbstractSettingScreen;
import dev.heliosclient.ui.clickgui.gui.Window;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HudEditorSettingScreen extends AbstractSettingScreen {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final float delayBetweenSettings = 0.2f;

    public HudEditorSettingScreen(HudElement hudElement, int windowHeight, int windowWidth) {
        super(Text.of(hudElement.name), hudElement, windowHeight, windowWidth);
        updateSetting();
    }

    @Override
    public void updateSetting() {
        executor.submit(() -> {
            Setting previousSetting = new Space(1, () -> false, false);
            if(window != null) {
                previousSetting.setY(window.getY());
            }
            for (Setting setting : hudElement.settings) {
                if (!setting.shouldRender()) continue;
                setting.update(previousSetting.getY());
                if (!setting.isAnimationDone() && delay <= 0) {
                    delay = delayBetweenSettings;
                    delay -= setting.animationSpeed;
                }
                previousSetting = setting;
            }
        });
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        window.setBackButtonTask(() -> {
                HeliosClient.MC.setScreen(HudEditorScreen.INSTANCE);
        });
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext, mouseX, mouseY, delta);

        windowHeight = 40;
        for (Setting setting : hudElement.settings) {
            if (!setting.shouldRender()) continue;
            setting.quickSettings = false;
            windowHeight += setting.height + 1;
        }

        window.setWindowHeight(windowHeight);
        window.setWindowWidth(220);
        super.render(drawContext, mouseX, mouseY, delta);

    }

    @Override
    public void renderContent(Window window, DrawContext drawContext, int x, int y, int mouseX, int mouseY) {
        updateSetting();
        int yOffset = y;
        for (Setting setting : hudElement.settings) {
            if (setting.shouldRender()) {
                if (setting instanceof RGBASetting rgbaSetting) {
                    rgbaSetting.setParentScreen(this);
                } else if (setting instanceof ListSetting listSetting) {
                    listSetting.setParentScreen(this);
                }

                // Update the y position of the setting based on its animation progress
                int animatedY = Math.round(setting.getY() + (yOffset - setting.getY()) * setting.getAnimationProgress());
                setting.render(drawContext, x + 16, animatedY + 6, mouseX, mouseY, textRenderer);
                yOffset += setting.height + 1;
            } else {
                resetSettingAnimation(setting);
            }
        }
    }

    private void resetSettingAnimation(Setting setting) {
        setting.animationDone = false;
        delay = 0;
        setting.setAnimationProgress(0.5f);
    }
}
