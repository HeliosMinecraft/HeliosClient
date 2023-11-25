package dev.heliosclient.ui.clickgui;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.input.MouseClickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.RGBASetting;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.ui.clickgui.gui.Hitbox;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.KeycodeToString;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.TimerUtils;
import dev.heliosclient.util.animation.AnimationUtils;
import dev.heliosclient.util.animation.Easing;
import dev.heliosclient.util.animation.EasingType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.AbstractMap;

public class ModuleButton implements Listener {
    public final Screen parentScreen;
    private final Hitbox hitBox;
    private final float delayBetweenSettings = 0.1f;
    public int hoverAnimationTimer;
    public Module_ module;
    public float x, y;
    public int width, height;
    public boolean settingsOpen = false;
    public int boxHeight = 0;
    public boolean animationDone = false;
    float animationSpeed = 0.13f;
    float delay = 0;
    int settingHeight = 0;
    private boolean faded = true;
    private float targetY;
    private float animationProgress = 0;
    public Screen screen;
    public boolean collapsed = false;

    public ModuleButton(Module_ module, Screen parentScreen) {
        this.module = module;
        this.width = CategoryPane.getWidth() - 2;
        this.height = 16;
        this.parentScreen = parentScreen;
        hitBox = new Hitbox(x, y, width, height);
        EventManager.register(this);
    }


    public void updateSetting() {
        settingHeight = 2;
        module.quickSettingGroups.stream()
                .filter(SettingGroup::shouldRender)
                .flatMap(settingGroup -> settingGroup.getSettings().stream().map(setting -> new AbstractMap.SimpleEntry<>(settingGroup, setting)))
                .filter(entry -> entry.getValue().shouldRender())
                .forEach(entry -> {
                    SettingGroup settingGroup = entry.getKey();
                    Setting setting = entry.getValue();
                    setting.update(settingGroup.getY());
                    settingHeight += setting.heightCompact;
                    if (!setting.isAnimationDone() && delay <= 0) {
                        delay = delayBetweenSettings;
                        delay -= setting.animationSpeed;
                    }
                });
    }

    public void update(float targetY) {
        if (!animationDone) {
            //the first update, set the initial position above the target
            if (animationProgress == 0) {
                y = (int) (targetY);
            }

            this.targetY = targetY;
            animationProgress += animationSpeed;
            animationProgress = Math.min(animationProgress, 1);

            float easedProgress = Easing.ease(EasingType.LINEAR_IN_OUT, animationProgress);
            y = Math.round(AnimationUtils.lerp(y, this.targetY, easedProgress));
            if (animationProgress >= 1) {
                animationDone = true;
            }
        }
    }

    public void setFaded(boolean faded) {
        this.faded = faded;
    }

    public boolean hasFaded() {
        return faded;
    }

    public void render(DrawContext drawContext, int mouseX, int mouseY, int x, int y, int maxWidth) {
        this.screen = HeliosClient.MC.currentScreen;
        this.x = x;
        this.y = y;

        if (hitBox.contains(mouseX, mouseY)) {
            hoverAnimationTimer = Math.min(hoverAnimationTimer + 1, 20);
        } else {
            hoverAnimationTimer = Math.max(hoverAnimationTimer - 1, 0);
        }
        // Get the width and height of the module name
        int moduleNameHeight = (int) Renderer2D.getFxStringHeight(module.name) - 1;

        this.width = maxWidth;

        hitBox.set(x, y, width, height);

        Color fillColor = module.isActive() ? new Color(ColorManager.INSTANCE.clickGuiSecondary()) : ColorUtils.changeAlpha(new Color(ColorManager.INSTANCE.ClickGuiPrimary()), 100);
        if(hitBox.contains(mouseX,mouseY)) {
            Renderer2D.drawRoundedRectangleWithShadow(drawContext.getMatrices(), x + 1, y, width, height, 2, 4, fillColor.getRGB());
        }
        else{
            Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 1, y, width, height,2,fillColor.getRGB());
        }
        if (settingsOpen && boxHeight >= 4) {
            Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 1, y + height, width, boxHeight + 2,2,ColorUtils.changeAlpha(fillColor, 100).getRGB()); ;
        }

        int textY = y + (height - moduleNameHeight) / 2;

        Renderer2D.drawFixedString(drawContext.getMatrices(), module.name, x + 3, textY, ColorManager.INSTANCE.defaultTextColor());
        if (hitBox.contains(mouseX, mouseY)) {
            Tooltip.tooltip.changeText(module.description);
        }

        if (module.keyBind.value != 0 && ClickGUI.keybinds) {
            String keyName = "[" + KeycodeToString.translateShort(module.keyBind.value) + "]";
            Renderer2D.drawFixedString(drawContext.getMatrices(), keyName.toUpperCase(), (int) (x + width - 3 - Renderer2D.getFxStringWidth(keyName)), textY, ColorManager.INSTANCE.defaultTextColor);
        }
    }

    public int renderSettings(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        int buttonYOffset = 0;
        if (settingsOpen) {
            updateSetting();
            buttonYOffset = y + this.height + 4;
            for (SettingGroup settingGroup : module.quickSettingGroups) {
                buttonYOffset += Math.round(settingGroup.getGroupNameHeight() + 6);

                if (buttonYOffset >= y) {
                    settingGroup.renderBuilder(drawContext, x - 1, buttonYOffset, width);
                }

                for (Setting setting : settingGroup.getSettings()) {
                    if (!settingGroup.shouldRender() || !setting.shouldRender()) {
                        resetAnimation(setting);
                        continue;
                    }
                    if (setting instanceof RGBASetting rgbaSetting) {
                        rgbaSetting.setParentScreen(ClickGUIScreen.INSTANCE);
                    }
                    if (buttonYOffset >= y) {
                        setting.quickSettings = settingsOpen;

                        int animatedY = Math.round(setting.getY() + (buttonYOffset - setting.getY()) * setting.getAnimationProgress());
                        setting.renderCompact(drawContext, x, animatedY + 5, mouseX, mouseY, textRenderer);
                        buttonYOffset += setting.heightCompact + 1;
                    }
                }
            }
            if (!module.quickSettingGroups.isEmpty()) {
                buttonYOffset += 2;
            }
            setBoxHeight(buttonYOffset - y - this.height - 2);
        }
        else{
            module.quickSettingGroups.forEach(settingGroup -> settingGroup.getSettings().forEach(this::resetAnimation));
        }
        return buttonYOffset > 2 ? buttonYOffset - y - this.height : 0;
    }

    private void resetAnimation(Setting setting) {
        setting.animationDone = false;
        delay = 0;
        setting.setAnimationProgress(0.5f);
    }

   @SubscribeEvent
    public boolean mouseClicked(MouseClickEvent event) {
        if (screen != null && event.getScreen() == screen){
            double mouseX = event.getMouseX();
            double mouseY = event.getMouseY();
            int button = event.getButton();
            if (!collapsed) {
                setFaded(false);
                if (hitBox.contains(mouseX, mouseY)) {
                    if (button == 0) {
                        module.toggle();
                        return true;
                    } else if (button == 1) {
                        MinecraftClient.getInstance().setScreen(new SettingsScreen(module, parentScreen));
                        return true;
                    } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                        this.module.settingsOpen = !this.module.settingsOpen;
                        this.settingsOpen = this.module.settingsOpen;
                        return true;
                    }
                }
                if (this.module.settingsOpen) {
                    for (SettingGroup settingGroup : module.quickSettingGroups) {
                        settingGroup.mouseClickedBuilder(mouseX, mouseY);
                        if (!settingGroup.shouldRender()) continue;
                        settingGroup.mouseClicked(mouseX, mouseY, button);
                    }
                }
            }

        if (collapsed) {
            for (SettingGroup settingGroup : module.quickSettingGroups) {
                for (Setting setting : settingGroup.getSettings()) {
                    resetAnimation(setting);
                }
            }
            setFaded(true);
            animationDone = false;
            setAnimationProgress(0.0f);
        }
    }
        return false;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setBoxHeight(int boxHeight) {
        this.boxHeight = boxHeight;
    }

    public float getAnimationProgress() {
        return animationProgress;
    }

    public void setAnimationProgress(float animationProgress) {
        this.animationProgress = animationProgress;
    }

    public float getTargetY() {
        return targetY;
    }

    public float getY() {
        return y;
    }

    public boolean isAnimationDone() {
        return animationDone;
    }
}
