package dev.heliosclient.module.settings;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.FontChangeEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.ui.clickgui.CategoryPane;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.fontutils.fxFontRenderer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.function.BooleanSupplier;

public abstract class Setting<T> implements Listener {
    public String name;
    public String description;
    public int height = 24;
    public int width = 192;
    protected static fxFontRenderer compactFont;
    public int widthCompact = CategoryPane.getWidth();
    public final T defaultValue;
    public T value;
    public boolean quickSettings = false;
    protected int moduleWidth = CategoryPane.getWidth();
    int x = 0, y = 0;
    int hovertimer = 0;
    private int hoverAnimationTimer = 0;
    protected BooleanSupplier shouldRender = () -> true; // Default to true
    public int heightCompact = 18;

    public Setting(BooleanSupplier shouldRender, T defaultValue) {
        this.shouldRender = shouldRender;
        this.defaultValue = defaultValue;
        EventManager.register(this);
        if (HeliosClient.MC.getWindow() != null) {
            compactFont = new fxFontRenderer(FontManager.fonts, 6);
        }
    }

    @SubscribeEvent
    private void onFontChange(FontChangeEvent event) {
        compactFont = new fxFontRenderer(FontManager.fonts, 6);
    }

    /**
     * Renders setting in GUI.
     * @param drawContext
     * @param x
     * @param y
     * @param mouseX
     * @param mouseY
     * @param textRenderer
     */
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        this.x = x;
        this.y = y;
        if (hovered(mouseX, mouseY)) {
            hoverAnimationTimer = Math.min(hoverAnimationTimer + 1, 40);
        } else {
            hoverAnimationTimer = Math.max(hoverAnimationTimer - 1, 0);
        }
        if (hoveredSetting(mouseX, mouseY)) {
            Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 195, y + (float) this.height / 2 - 5.5f, 11, 11, Color.black.getRGB());
            Renderer2D.drawOutlineBox(drawContext.getMatrices().peek().getPositionMatrix(), x + 195, y + (float) this.height / 2 - 5.5f, 11, 11, 0.4f, (hoveredOverReset(mouseX, mouseY)) ? Color.WHITE.getRGB() : Color.GRAY.getRGB());

            drawContext.drawText(textRenderer, "↻", (int) (x + 199.5f), (int) (y + height / 2 - 5f), -1, true);
        }
    }

    public boolean hoveredOverReset(double mouseX, double mouseY) {
        return mouseX >= x + 195 && mouseX <= x + 206 && mouseY >= y + (double) this.height / 2 - 5.5f && mouseY <= y + (double) this.height / 2 + 5.5f;
    }
    /**
     * Compact rendering for clickGUI.
     */
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        this.x = x;
        this.y = y;
        if (hovered(mouseX, mouseY)) {
            hoverAnimationTimer = Math.min(hoverAnimationTimer + 1, 40);
        } else {
            hoverAnimationTimer = Math.max(hoverAnimationTimer - 1, 0);
        }
    }

    /**
     * If setting should render, Meant to be used for overrides for settings that are meant to be shown only if condition is met.
     * @return If should render.
     */
    public boolean shouldRender() {
        return shouldRender.getAsBoolean();
    }

    /**
     * Called every time mouse gets clicked.
     * @param mouseX X coordinate of mouse.
     * @param mouseY Y coordinate of mouse.
     * @param button Which button is being clicked.
     */
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (hoveredSetting((int) mouseX, (int) mouseY) && hoveredOverReset(mouseX, mouseY)) {
            value = defaultValue;
        }
    }

    /**
     * Called every time mouse gets released.
     * @param mouseX X coordinate of mouse.
     * @param mouseY Y coordinate of mouse,
     * @param button Which button got released.
     */
    public void mouseReleased(double mouseX, double mouseY, int button) {
    }

    /**
     * Called every time key gets pressed.
     * @param keyCode GLFW keycode of key that got pressed.
     * @param scanCode Scan code.
     * @param modifiers Modifiers eg. Ctrl, Shift.
     */
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
    }

    /**
     * Gets called every time mouse gets dragged.
     * @param mouseX
     * @param mouseY
     * @param button
     * @param deltaX
     * @param deltaY
     */
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
    }

    /**
     * Gets called every time key gets released.
     * @param keyCode GLFW keycode of key that got pressed.
     * @param scanCode Scan code.
     * @param modifiers Modifiers eg. Ctrl, Shift.
     */
    public void keyReleased(int keyCode, int scanCode, int modifiers) {
    }

    /**
     * Gets called every time char gets typed.
     * @param chr Char in question.
     * @param modifiers Modifiers eg. Ctrl, Shift.
     */
    public void charTyped(char chr, int modifiers) {
    }

    /**
     * Boolean that tells if mouse is hovering this setting.
     * @param mouseX Current mouseX.
     * @param mouseY Current mouseY.
     * @return If mouse is hovering.
     */
    protected boolean hovered(int mouseX, int mouseY) {
        //Different hit-boxes for quick and regular settings.
        if (quickSettings) {
            return mouseX > x && mouseX < x + widthCompact && mouseY > y && mouseY < y + heightCompact;
        } else {
            return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
        }
    }

    protected boolean hoveredSetting(int mouseX, int mouseY) {
        return mouseX > x && mouseX < x + width + 40 && mouseY > y && mouseY < y + height;
    }

    // Credits: Meteor client
    public abstract static class SettingBuilder<B, V, S> {
        protected String name = "null", description = "";
        protected V value;
        protected BooleanSupplier shouldRender = () -> true; // Default to true
        protected V defaultValue;
        protected SettingBuilder(V value) {
            this.value = value;
            this.defaultValue = value;
        }

        public B name(String name) {
            this.name = name;
            return (B) this;
        }

        public B description(String description) {
            this.description = description;
            return (B) this;
        }

        public B value(V value) {
            this.value = value;
            return (B) this;
        }

        public B shouldRender(BooleanSupplier shouldRender) {
            this.shouldRender = shouldRender;
            return (B) this;
        }

        public B defaultValue(V defaultValue) {
            this.defaultValue = defaultValue;
            return (B) this;
        }

        public abstract S build();
    }
}
