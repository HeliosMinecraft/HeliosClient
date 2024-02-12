package dev.heliosclient.ui.clickgui;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.input.MouseClickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.ListSetting;
import dev.heliosclient.module.settings.RGBASetting;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.ui.clickgui.gui.HudBox;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.KeycodeToString;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.animation.AnimationUtils;
import dev.heliosclient.util.animation.Easing;
import dev.heliosclient.util.animation.EasingType;
import dev.heliosclient.util.fontutils.FontRenderers;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class ModuleButton implements Listener {
    public final Screen parentScreen;
    private final HudBox hitBox;
    private final float delayBetweenSettings = 0.1f;
    public int hoverAnimationTimer;
    public Module_ module;
    public float x, y;
    public int width, height;
    public boolean settingsOpen = false;
    public int boxHeight = 0;
    public boolean animationDone = false;
    public Screen screen;
    public boolean collapsed = false;
    float animationSpeed = 0.13f;
    float delay = 0;
    int settingHeight = 0;
    private boolean faded = true;
    private float targetY;
    private float animationProgress = 0;

    public ModuleButton(Module_ module, Screen parentScreen) {
        this.module = module;
        this.width = CategoryPane.getWidth() - 2;
        this.height = 16;
        this.parentScreen = parentScreen;
        hitBox = new HudBox(x, y, width, height);
        EventManager.register(this);
    }


    public void updateSetting() {
        settingHeight = 2;
        module.quickSettings.stream().filter(Setting::shouldRender)
                .forEach(entry -> {
                    entry.update(entry.getY());
                    settingHeight += entry.heightCompact;
                    if (!entry.isAnimationDone() && delay <= 0) {
                        delay = delayBetweenSettings;
                        delay -= entry.animationSpeed;
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

        Color fillColorStart = module.isActive() ? ColorManager.INSTANCE.primaryGradientStart : ColorUtils.changeAlpha(new Color(ColorManager.INSTANCE.ClickGuiPrimary()), 100);
        Color fillColorEnd = module.isActive() ? ColorManager.INSTANCE.primaryGradientEnd : ColorUtils.changeAlpha(new Color(ColorManager.INSTANCE.ClickGuiPrimary()), 100);
        Color blendedColor = ColorUtils.blend(fillColorStart, fillColorEnd, 1 / 2f);

        if (hitBox.contains(mouseX, mouseY)) {
            Renderer2D.drawRoundedGradientRectangleWithShadow(drawContext.getMatrices(), x + 1, y, width, height, fillColorStart, fillColorEnd, fillColorEnd, fillColorStart, 2, 5, blendedColor);
        } else {
            Renderer2D.drawRoundedGradientRectangle(drawContext.getMatrices().peek().getPositionMatrix(), fillColorStart, fillColorEnd, fillColorEnd, fillColorStart, x + 1, y, width, height, 2);
        }
        if (settingsOpen && boxHeight >= 4) {
            Renderer2D.drawRoundedGradientRectangle(drawContext.getMatrices().peek().getPositionMatrix(), ColorUtils.changeAlpha(fillColorStart, 100), ColorUtils.changeAlpha(fillColorEnd, 100), ColorUtils.changeAlpha(fillColorEnd, 100), ColorUtils.changeAlpha(fillColorStart, 100), x + 1, y + height, width, boxHeight + 2, 2);
            // Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 1, y + height, width, boxHeight + 2,2,ColorUtils.changeAlpha(blendedColor, 100).getRGB()); ;
        }

        int textY = y + (height - moduleNameHeight) / 2;

        Renderer2D.drawFixedString(drawContext.getMatrices(), module.name, x + 3, textY, ColorManager.INSTANCE.defaultTextColor());
        if (hitBox.contains(mouseX, mouseY)) {
            Tooltip.tooltip.changeText(module.description);
        }

        if (module.keyBind.value != -1 && ClickGUI.keybinds) {
            String keyName = "[" + KeycodeToString.translateShort(module.keyBind.value) + "]";
            FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), keyName.toUpperCase(), (int) (x + width - 3 - Renderer2D.getCustomStringWidth(keyName, FontRenderers.Small_fxfontRenderer)), textY, ColorManager.INSTANCE.defaultTextColor);
        }
    }

    public int renderSettings(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        int buttonYOffset = 0;
        if (settingsOpen) {
            updateSetting();
            buttonYOffset = y + this.height + 2;

            for (Setting setting : module.quickSettings) {

                // Reset the animation if the setting is not visible.
                if (!setting.shouldRender()) {
                    resetAnimation(setting);
                    continue;
                }
                // Set the screen for the settings
                if (setting instanceof RGBASetting rgbaSetting) {
                    rgbaSetting.setParentScreen(ClickGUIScreen.INSTANCE);
                } else if (setting instanceof ListSetting listSetting) {
                    listSetting.setParentScreen(ClickGUIScreen.INSTANCE);
                }

                // If offset is more than Y level, render the setting.
                if (buttonYOffset >= y + 3) {
                    setting.quickSettings = settingsOpen;

                    int animatedY = getAnimatedY(setting, buttonYOffset);

                    setting.renderCompact(drawContext, x, animatedY + 1, mouseX, mouseY, textRenderer);
                    buttonYOffset += setting.heightCompact + 1;
                }
            }

            if (!module.quickSettings.isEmpty()) {
                buttonYOffset += 2;
            }
            setBoxHeight(buttonYOffset - y - this.height - 2);
        } else {
            module.quickSettings.forEach(this::resetAnimation);
        }
        // Return the total height of the quick settings
        return buttonYOffset > 2 ? buttonYOffset - y - this.height - 2 : 0;
    }

    public int getAnimatedY(Setting setting, int offset) {
        return Math.round(setting.getY() + (offset - setting.getY()) * setting.getAnimationProgress());
    }

    private void resetAnimation(Setting setting) {
        setting.animationDone = false;
        delay = 0;
        setting.setAnimationProgress(0.5f);
    }

    @SubscribeEvent
    public boolean mouseClicked(MouseClickEvent event) {
        if (screen != null && event.getScreen() == screen) {
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
                        HeliosClient.MC.setScreen(new SettingsScreen(module, parentScreen));
                        return true;
                    } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                        this.module.settingsOpen = !this.module.settingsOpen;
                        this.settingsOpen = this.module.settingsOpen;
                        return true;
                    }
                }
                if (this.module.settingsOpen) {
                    for (Setting setting : module.quickSettings) {
                        if (!setting.shouldRender()) continue;
                        setting.mouseClicked(mouseX, mouseY, button);
                    }
                }
            }

            if (collapsed) {
                for (Setting setting : module.quickSettings) {
                    resetAnimation(setting);
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
