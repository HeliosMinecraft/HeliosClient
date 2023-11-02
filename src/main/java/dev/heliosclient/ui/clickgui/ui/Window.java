package dev.heliosclient.ui.clickgui.ui;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.animation.Easing;
import dev.heliosclient.util.animation.EasingType;
import dev.heliosclient.util.interfaces.IWindowContentRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.List;

public class Window {

    private final IWindowContentRenderer contentRenderer;
    public TextButton backButton = new TextButton("< Back");
    public TextButton collapseButton = new TextButton("+/-");
    int offsetY = 0;
    private int x, y, windowHeight, windowWidth;
    private boolean isCollapsible = false;
    private boolean isCollapsed = false;
    private Runnable backButtonTask;

    public Window(int windowHeight, int windowWidth, boolean collapsible, IWindowContentRenderer contentRenderer) {
        this.windowHeight = windowHeight;
        this.windowWidth = windowWidth;
        if (HeliosClient.MC.getWindow() != null) {
            this.x = (HeliosClient.MC.getWindow().getScaledWidth() / 2) - windowWidth / 2;
            this.y = 5;
        }
        offsetY = 0;
        this.isCollapsible = collapsible;
        this.contentRenderer = contentRenderer;
        backButtonTask = () -> {
            ClickGUIScreen.INSTANCE.onLoad();
            MinecraftClient.getInstance().setScreen(ClickGUIScreen.INSTANCE);
        };
    }

    public Window(int windowHeight, int windowWidth, boolean collapsible, int x, int y, IWindowContentRenderer contentRenderer) {
        this.windowHeight = windowHeight;
        this.windowWidth = windowWidth;
        this.x = x;
        this.y = y;
        offsetY = 0;
        this.isCollapsible = collapsible;
        this.contentRenderer = contentRenderer;
    }

    public void setCollapsible(boolean collapsible) {
        isCollapsible = collapsible;
    }

    public void init() {
        if (HeliosClient.MC.getWindow() != null) {
            this.x = (HeliosClient.MC.getWindow().getScaledWidth() / 2) - windowWidth / 2;
            this.y = 5;
        }
    }

    public void render(DrawContext drawContext, int mouseX, int mouseY, String name, String description, TextRenderer textRenderer) {
        int screenHeight = drawContext.getScaledWindowHeight();

        if (screenHeight > windowHeight) {
            offsetY = 0;
            y = drawContext.getScaledWindowHeight() / 2 - (windowHeight) / 2;
        } else {
            offsetY = Math.max(Math.min(offsetY, 0), screenHeight - windowHeight);
            y = offsetY;
        }

        x = Math.max(drawContext.getScaledWindowWidth() / 2 - windowWidth / 2, 0);

        if (!isCollapsed && contentRenderer != null) {
            // Calculate the color and text width only once
            int color = ColorUtils.changeAlpha(new Color(ColorManager.INSTANCE.clickGuiPrimary), 180).getRGB();
            List<String> wrappedText = Renderer2D.wrapText("§o" + description, windowWidth - 2);

            Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y + 20, windowWidth, windowHeight, 5, color);

            for (String text : wrappedText) {
                int warpedTextWidth = textRenderer.getWidth(text);
                drawContext.drawText(textRenderer, text, x + windowWidth / 2 - warpedTextWidth / 2 + 1, y + 26, ColorManager.INSTANCE.defaultTextColor(), false);
                windowHeight += textRenderer.fontHeight + 2;
            }

            contentRenderer.renderContent(this, drawContext, x, y + 30 + (wrappedText.size() * textRenderer.fontHeight), mouseX, mouseY);
        }

        // Draw the screen
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y, true, true, false, false, windowWidth, 18, 5, ColorManager.INSTANCE.clickGuiPrimary);
        Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y + 16, windowWidth, 2, ColorManager.INSTANCE.clickGuiSecondary());

        drawContext.drawText(textRenderer, name, x + windowWidth / 2 - textRenderer.getWidth(name) / 2, y + 4, ColorManager.INSTANCE.clickGuiPaneText(), false);
        // Render the back button
        backButton.render(drawContext, textRenderer, x + 4, y + 4, mouseX, mouseY);

        if (isCollapsible) {
            // Render the collapse button
            collapseButton.render(drawContext, textRenderer, x + windowWidth - collapseButton.width - 4, y + 4, mouseX, mouseY);
        }

    }

    public void mouseClicked(double mouseX, double mouseY) {
        backButton.mouseClicked((int) mouseX, (int) mouseY, backButtonTask);
        if (isCollapsible) {
            collapseButton.mouseClicked((int) mouseX, (int) mouseY, () -> isCollapsed = !isCollapsed);
        }
    }

    public void mouseScrolled(double mouseX, double mouseY, double amount) {
        offsetY += (int) (amount * (Easing.ease(EasingType.QUADRATIC_IN, ClickGUI.ScrollSpeed.value.floatValue())));
    }

    private boolean hoveredOver(double mouseX, double mouseY) {
        return mouseX > x - 2 && mouseX < x + windowWidth && mouseY > y - 1 && mouseY < y + 22;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public TextButton getBackButton() {
        return backButton;
    }

    public IWindowContentRenderer getContentRenderer() {
        return contentRenderer;
    }

    public Runnable getBackButtonTask() {
        return backButtonTask;
    }

    public void setBackButtonTask(Runnable backButtonTask) {
        this.backButtonTask = backButtonTask;
    }

    public int getOffsetY() {
        return offsetY;
    }
}
