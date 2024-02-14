package dev.heliosclient.ui.clickgui.gui;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.input.MouseClickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.NavBarManager;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import dev.heliosclient.ui.clickgui.navbar.NavBarItem;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.render.Renderer2D;
import dev.heliosclient.util.animation.Easing;
import dev.heliosclient.util.animation.EasingType;
import dev.heliosclient.util.interfaces.IWindowContentRenderer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import java.awt.*;
import java.util.List;

public class Window implements Listener {

    private final IWindowContentRenderer contentRenderer;
    public TextButton backButton = new TextButton("< Back");
    public TextButton collapseButton = new TextButton("+/-");
    public Screen screen;
    int offsetY;
    private int x, y, windowHeight, windowWidth;
    private boolean isCollapsible;
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
            HeliosClient.MC.setScreen(ClickGUIScreen.INSTANCE);
            for (NavBarItem item : NavBarManager.INSTANCE.navBarItems) {
                if (item.name.equalsIgnoreCase("ClickGUI")) {
                    item.target = ClickGUIScreen.INSTANCE;
                }
            }
        };

        EventManager.register(this);
    }

    public Window(int windowHeight, int windowWidth, boolean collapsible, int x, int y, IWindowContentRenderer contentRenderer) {
        this.windowHeight = windowHeight;
        this.windowWidth = windowWidth;
        this.x = x;
        this.y = y;
        offsetY = 0;
        this.isCollapsible = collapsible;
        this.contentRenderer = contentRenderer;
        EventManager.register(this);
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
        this.screen = HeliosClient.MC.currentScreen;
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
            int color = ColorUtils.changeAlpha(new Color(ColorManager.INSTANCE.clickGuiPrimary), 180).getRGB();
            List<String> wrappedText = Renderer2D.wrapText("§o" + description, windowWidth - 2);

            Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y + 20, windowWidth, windowHeight, 5, color);

            int textOffsetY = 0;
            for (String text : wrappedText) {
                int warpedTextWidth = textRenderer.getWidth(text);
                drawContext.drawText(textRenderer, text, x + windowWidth / 2 - warpedTextWidth / 2 + 1, y + 26 + textOffsetY, ColorManager.INSTANCE.defaultTextColor(), false);
                windowHeight += textRenderer.fontHeight + 4;
                textOffsetY += textRenderer.fontHeight + 3;
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

    @SubscribeEvent
    public void mouseClicked(MouseClickEvent event) {
        if (screen != null && event.getScreen() == screen) {
            double mouseX = event.getMouseX();
            double mouseY = event.getMouseY();
            backButton.mouseClicked((int) mouseX, (int) mouseY, backButtonTask);
            if (isCollapsible) {
                collapseButton.mouseClicked((int) mouseX, (int) mouseY, () -> isCollapsed = !isCollapsed);
            }
        }
    }

    public void mouseScrolled(double mouseX, double mouseY, double amount) {
        offsetY += (int) (amount * (Easing.ease(EasingType.QUADRATIC_IN, (float) ClickGUI.ScrollSpeed.value)));
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

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
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
