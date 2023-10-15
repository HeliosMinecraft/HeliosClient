package dev.heliosclient.ui.clickgui;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.module.settings.RGBASetting;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class RGBASettingScreen extends Screen {
    public TextButton backButton = new TextButton("< Back");
    int x, x2, y, windowWidth = 192 , windowHeight;
    private RGBASetting setting;
    public RGBASettingScreen(RGBASetting setting) {
        super(Text.literal(setting.name));
        this.setting = setting;
    }
    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext);
        windowHeight = 142;

        if (drawContext.getScaledWindowHeight() > windowHeight) {
            y = drawContext.getScaledWindowHeight() / 2 - (windowHeight) / 2;
        }

        x = Math.max(drawContext.getScaledWindowWidth() / 2 - windowWidth / 2, 0);
        x2 = x + windowWidth;
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y, windowWidth, windowHeight, 5, ColorUtils.changeAlpha(new Color(ColorManager.INSTANCE.clickGuiPrimary),180).getRGB());
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y, true, true, false, false, windowWidth, 18, 5, ColorManager.INSTANCE.clickGuiPrimary);
        Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y + 16, windowWidth, 2, ColorManager.INSTANCE.clickGuiSecondary());
        Renderer2D.drawFixedString(drawContext.getMatrices(),setting.name, drawContext.getScaledWindowWidth() / 2 - Renderer2D.getFxStringWidth(setting.name) / 2, y + 4, ColorManager.INSTANCE.clickGuiPaneText());
        Renderer2D.drawFixedString(drawContext.getMatrices(), "§o" + setting.description, drawContext.getScaledWindowWidth() / 2 - textRenderer.getWidth("§o" + setting.description) / 2, y + 20, ColorManager.INSTANCE.defaultTextColor());
        backButton.render(drawContext, textRenderer, x + 4, y + 4, mouseX, mouseY);

        setting.height = 100;
        setting.renderSetting(drawContext,x,y+40,mouseX,mouseY,textRenderer);
        setting.height = 25;
        Tooltip.tooltip.render(drawContext, textRenderer, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        backButton.mouseClicked((int) mouseX, (int) mouseY);
        setting.mouse(mouseX,mouseY);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        setting.mouse(mouseX,mouseY);
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            setting.height = 25;
            MinecraftClient.getInstance().setScreen(ClickGUIScreen.INSTANCE);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return  false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
