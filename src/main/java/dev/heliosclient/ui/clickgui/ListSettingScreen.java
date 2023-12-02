package dev.heliosclient.ui.clickgui;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.module.settings.ListSetting;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.ui.clickgui.gui.AbstractSettingScreen;
import dev.heliosclient.ui.clickgui.gui.TextButton;
import dev.heliosclient.ui.clickgui.gui.Window;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.interfaces.IWindowContentRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class ListSettingScreen extends AbstractSettingScreen {
    static int offsetY = 0;
    private final ListSetting listSetting;
    private final Screen parentScreen;
    private final int[] hoverAnimationTimers;
    int windowWidth = 180, windowHeight;

    public ListSettingScreen(ListSetting listSetting, Screen parentScreen) {
        super(Text.literal(listSetting.name), listSetting,0,180);
        this.listSetting = listSetting;
        this.parentScreen = parentScreen;
        offsetY = 0;
        hoverAnimationTimers = new int[listSetting.options.size()];
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        offsetY += (int) (verticalAmount * 7);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext, mouseX, mouseY, delta);

        windowHeight = 52;
        windowHeight += listSetting.options.size() * 25;


        window.setWindowWidth(windowWidth);
        window.setWindowHeight(windowHeight);
        super.render(drawContext, mouseX, mouseY, delta);
        Tooltip.tooltip.render(drawContext, textRenderer, mouseX, mouseY);

    }

    @Override
    public void updateSetting() {

    }

    public boolean value(String option) {
        return listSetting.value.contains(option);
    }

    public void toggleValue(String option) {
        if (listSetting.value.contains(option)) {
            listSetting.value.remove(option);
        } else {
            listSetting.value.add(option);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int yOffset = y + 55;
        for (String string : listSetting.options) {
            int hitboxY = yOffset - 25 / 2 + 10 / 2;
            if (mouseX >= x && mouseX <= x + windowWidth && mouseY >= hitboxY && mouseY <= hitboxY + 25) {
                toggleValue(string);
            }
            yOffset += 25;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            MinecraftClient.getInstance().setScreen(parentScreen);
        }
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void renderContent(Window window, DrawContext drawContext, int x, int y, int mouseX, int mouseY) {
        int x2 = x + windowWidth;

        int yOffset = y + 55;
        for (int i = 0; i < listSetting.options.size(); i++) {
            String string = listSetting.options.get(i);
            int hitboxY = yOffset - 25 / 2 + 10 / 2;
            if (mouseX >= x && mouseX <= x2 && mouseY >= hitboxY && mouseY <= hitboxY + 25) {
                hoverAnimationTimers[i] = Math.min(hoverAnimationTimers[i] + 1, 40);
            } else {
                hoverAnimationTimers[i] = Math.max(hoverAnimationTimers[i] - 1, 0);
            }
            int fillColor = (int) (34 + 0.85 * hoverAnimationTimers[i]);
            Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, hitboxY, windowWidth, 25, 5, new Color(fillColor, fillColor, fillColor).getRGB());
            drawContext.drawText(textRenderer, string, x + 16, yOffset + 2, ColorManager.INSTANCE.defaultTextColor(), false);
            drawContext.fill(x2 - 26, yOffset, x2 - 16, yOffset + 10, 0xFFFFFFFF);
            drawContext.fill(x2 - 25, yOffset + 1, x2 - 17, yOffset + 9, 0xFF222222);
            drawContext.fill(x2 - 24, yOffset + 2, x2 - 18, yOffset + 8, value(string) ? 0xFF55FFFF : 0xFF222222);
            yOffset += 25;
        }
    }
}