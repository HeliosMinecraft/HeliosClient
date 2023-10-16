package dev.heliosclient.module.settings;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.ui.clickgui.CategoryPane;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.function.BooleanSupplier;

public abstract class Setting {
    public String name;
    public String description;
    public int height = 24;
    public int width = 192;
    public int heightCompact = 24;
    public int widthCompact = CategoryPane.getWidth();
    public Object value;
    public Object defaultValue;
    public boolean quickSettings = false;
    protected int moduleWidth = CategoryPane.getWidth();
    int x = 0, y = 0;
    int hovertimer = 0;
    private int hoverAnimationTimer = 0;
    protected BooleanSupplier shouldRender = () -> true; // Default to true

    public Setting(BooleanSupplier shouldRender) {
        this.shouldRender = shouldRender;
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
        int fillColor = ColorUtils.changeAlpha(new Color(ColorManager.INSTANCE.ClickGuiPrimary()), 181).getRGB();
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y, width, height, 2, fillColor);
    }

    /**
     * Compact rendering for clickGUI.
     * @param drawContext
     * @param x
     * @param y
     * @param mouseX
     * @param mouseY
     * @param textRenderer
     */
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        this.x = x;
        this.y = y;
        if (hovered(mouseX, mouseY)) {
            hoverAnimationTimer = Math.min(hoverAnimationTimer + 1, 40);
        } else {
            hoverAnimationTimer = Math.max(hoverAnimationTimer - 1, 0);
        }
        int fillColor = ColorUtils.changeAlpha(new Color(ColorManager.INSTANCE.ClickGuiPrimary()), 200).getRGB();
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y, widthCompact, heightCompact, 2, fillColor);
    }

    public void setVisibilityCondition(BooleanSupplier shouldRender) {
        this.shouldRender = shouldRender;
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

    // Credits: Meteor client
    public abstract static class SettingBuilder<B, V, S> {
        protected String name = "undefined", description = "";
        protected V value;
        protected BooleanSupplier shouldRender = () -> true; // Default to true

        protected SettingBuilder(V value) {
            this.value = value;
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

        public abstract S build();
    }
}
