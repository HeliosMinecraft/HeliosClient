package dev.heliosclient.ui.clickgui;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.events.input.MouseClickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.render.GUI;
import dev.heliosclient.module.settings.ParentScreenSetting;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.ui.clickgui.gui.HudBox;
import dev.heliosclient.ui.clickgui.settings.SettingsScreen;
import dev.heliosclient.util.KeyboardUtils;
import dev.heliosclient.util.SoundUtils;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class ModuleButton implements Listener {

    public final Screen parentScreen;
    private final HudBox hitBox;
    public int hoverAnimationTimer;
    public Module_ module;
    public float x, y;
    public int width;
    public static int height = 16;
    public boolean settingsOpen = false;
    public int boxHeight = 0;
    public Screen screen;
    public boolean collapsed = false, shouldRender = true;
    protected double scale = 0.0f;

    public ModuleButton(Module_ module, Screen parentScreen) {
        this.module = module;
        this.width = CategoryPane.getWidth() - 2;
        this.parentScreen = parentScreen;
        hitBox = new HudBox(x, y, width, height);
        EventManager.register(this);
    }

    public void updateScale(boolean expand) {
        if (expand) {
            scale += (float) (HeliosClient.MC.getRenderTickCounter().getTickDelta(false) * HeliosClient.CLICKGUI.animationSpeed.value);
            if (scale > 1.0f) {
                scale = 1.0f;

            }
        } else {
            scale -= (float) (HeliosClient.MC.getRenderTickCounter().getTickDelta(false) * HeliosClient.CLICKGUI.animationSpeed.value);
            if (scale < 0.0f) {
                scale = 0.0f;

            }
        }

    }

    public void render(DrawContext drawContext, int mouseX, int mouseY, float x, int y, int maxWidth, boolean shouldRender) {
        this.screen = HeliosClient.MC.currentScreen;
        this.x = x;
        this.y = y;
        this.shouldRender = shouldRender;
        this.width = maxWidth;
        hitBox.set(this.x, this.y, width, height);

        if (!shouldRender) return;

        GUI gui = ModuleManager.get(GUI.class);

        if (hitBox.contains(mouseX, mouseY)) {
            hoverAnimationTimer = Math.min(hoverAnimationTimer + 1, 20);
        } else {
            hoverAnimationTimer = Math.max(hoverAnimationTimer - 1, 0);
        }
        // Get the width and height of the module name
        int moduleNameHeight = (int) Renderer2D.getFxStringHeight(module.name) - 1;

        Color fillColorStart = module.isActive() && !gui.textHighlight.value? ColorManager.INSTANCE.primaryGradientStart : ColorUtils.changeAlpha(gui.buttonColor.getColor(), 100);
        Color fillColorEnd = module.isActive() && !gui.textHighlight.value ? ColorManager.INSTANCE.primaryGradientEnd : ColorUtils.changeAlpha(gui.buttonColor.getColor(), 100);
        Color blendedColor = ColorUtils.blend(fillColorStart, fillColorEnd, 1 / 2f);

        int textY = y + (height - moduleNameHeight) / 2;

        if (hitBox.contains(mouseX, mouseY)) {
            textY = textY - 1;
            drawGradientRectangleWithShadow(drawContext.getMatrices(), x + 1, y - 1, fillColorStart, fillColorEnd, fillColorEnd, fillColorStart, width, height, HeliosClient.CLICKGUI.guiRoundness.getInt(), HeliosClient.CLICKGUI.guiRoundness.getInt() + 3, blendedColor);
        } else {
            drawGradientRectangle(drawContext.getMatrices().peek().getPositionMatrix(), fillColorStart, fillColorEnd, fillColorEnd, fillColorStart, x + 1, y, width, height,  HeliosClient.CLICKGUI.guiRoundness.getInt());
        }
        if (settingsOpen && boxHeight >= 4) {
            Renderer2D.scaleAndPosition(drawContext.getMatrices(), x + width / 2.0f, y + height + 2, (float) scale);
            drawGradientRectangle(drawContext.getMatrices().peek().getPositionMatrix(), fillColorStart, fillColorEnd, fillColorEnd, fillColorStart, x + 1, y + height, width, boxHeight + 2,  HeliosClient.CLICKGUI.guiRoundness.getInt());
            Renderer2D.stopScaling(drawContext.getMatrices());
        }
        
        if(module.isActive() && gui.textHighlight.value) {
            Renderer2D.drawBlurredShadow(drawContext.getMatrices(), x + 2, textY, Renderer2D.getFxStringWidth(module.name) + 2, moduleNameHeight + 1, 9,ColorUtils.changeAlpha(ColorManager.INSTANCE.primaryGradientStart,125));
        }

        Renderer2D.drawFixedString(drawContext.getMatrices(), module.name, x + 3, textY, ColorManager.INSTANCE.defaultTextColor());
        if (hitBox.contains(mouseX, mouseY)) {
            Tooltip.tooltip.changeText(module.description);
        }

        if (module.keyBind.value != -1 && ClickGUI.keybinds) {
            String keyName = "[" + KeyboardUtils.translateShort(module.keyBind.value) + "]";
            FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), keyName.toUpperCase(), (int) (x + width - 3 - Renderer2D.getCustomStringWidth(keyName, FontRenderers.Small_fxfontRenderer)), textY, ColorManager.INSTANCE.defaultTextColor);
        }
    }

    private void drawGradientRectangle(Matrix4f matrix4f, Color color1, Color color2, Color color3, Color color4, float x, float y, float width, float height, float radius) {
        if (HeliosClient.CLICKGUI.getTheme() == ClickGUI.Theme.Rounded) {
            Renderer2D.drawRoundedGradientRectangle(matrix4f, color1, color2, color3, color4, x, y, width, height, radius);
        } else {
            Renderer2D.drawGradient(matrix4f, x, y, width, height, color1.getRGB(), color3.getRGB(), Renderer2D.Direction.LEFT_RIGHT);
        }
    }

    private void drawGradientRectangleWithShadow(MatrixStack stack, float x, float y, Color color1, Color color2, Color color3, Color color4, float width, float height, float radius, int blurRadius, Color blurColor) {
        if (HeliosClient.CLICKGUI.getTheme() == ClickGUI.Theme.Rounded) {
            Renderer2D.drawRoundedGradientRectangleWithShadow(stack, x, y, width, height, color1, color2, color3, color4, radius, blurRadius, blurColor);
        } else {
            Renderer2D.drawGradientWithShadow(stack, x, y, width, height, blurRadius, color1.getRGB(), color3.getRGB(), Renderer2D.Direction.LEFT_RIGHT);
        }
    }

    public int renderSettings(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        if (!shouldRender){
            updateScale(false);
            return (int) (boxHeight * scale);
        }

        int settingYOffset = 0;
        updateScale(settingsOpen);
        if (scale > 0.0f) {
            settingYOffset = height + 2;
            Renderer2D.scaleAndPosition(drawContext.getMatrices(), x + (float) width / 2, y + height + 2, (float) scale);
            for (Setting<?> setting : module.quickSettings) {
                // Reset the animation if the setting is not visible.
                if (!setting.shouldRender()) {
                    continue;
                }
                // Set the screen for the settings
                if (setting instanceof ParentScreenSetting<?> setting1) {
                    setting1.setParentScreen(ClickGUIScreen.INSTANCE);
                }

                // If offset is more than 4, render the setting.
                if (settingYOffset > 4) {
                    setting.quickSettings = settingsOpen;

                    setting.renderCompact(drawContext, x, y + settingYOffset + 1, mouseX, mouseY, textRenderer);
                    settingYOffset += setting.heightCompact + 1;
                }
            }
            Renderer2D.stopScaling(drawContext.getMatrices());

            if (!module.quickSettings.isEmpty()) {
                settingYOffset += 2;
            }
        }

        //Multiplying by the scale gives us the "sliding in/out" effect.
        int finalHeight = (int) Math.round((settingYOffset - height - 2) * scale);
        setBoxHeight(finalHeight);

        // Return the total height of the quick settings
        return settingYOffset > 2 ? finalHeight : 0;
    }

    /**
     * Method is called in {@link CategoryPane#mouseClicked(MouseClickEvent)}
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button, Screen eventScreen) {
        if (!shouldRender) return false;

        if (screen != null && eventScreen == screen) {
            if (!collapsed) {
                if (hitBox.contains(mouseX, mouseY)) {
                    if (button == 0) {
                        if (HeliosClient.CLICKGUI.clickGUISound.value) {
                            SoundUtils.playInstanceSound(SoundUtils.CLICK_SOUNDEVENT);
                        }
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
                    for (Setting<?> setting : module.quickSettings) {
                        if (!setting.shouldRender()) continue;
                        setting.mouseClicked(mouseX, mouseY, button);
                    }
                }
            }

            if (collapsed) {
                scale = 0.0f;
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

    public float getY() {
        return y;
    }
}
