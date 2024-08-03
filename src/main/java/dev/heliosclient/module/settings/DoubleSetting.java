package dev.heliosclient.module.settings;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.InputBox;
import dev.heliosclient.util.MathUtils;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.interfaces.ISettingChange;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

public class DoubleSetting extends Setting<Double> {
    private final double min, max;
    private final int roundingPlace;
    private final InputBox inputBox;
    public double value;
    boolean sliding = false;

    public DoubleSetting(String name, String description, ISettingChange ISettingChange, double value, double min, double max, int roundingPlace, BooleanSupplier shouldRender, double defaultValue) {
        super(shouldRender, defaultValue);
        this.name = name;
        this.description = description;
        this.value = value;
        this.min = min;
        this.max = max;
        this.heightCompact = 20;
        this.iSettingChange = ISettingChange;
        this.roundingPlace = roundingPlace;
        inputBox = new InputBox(String.valueOf(max).length() * 6, 11, String.valueOf(value), 10, InputBox.InputMode.DIGITS);
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        int defaultColor = ColorManager.INSTANCE.defaultTextColor();

        Renderer2D.drawFixedString(drawContext.getMatrices(), name, x + 2, y + 2, defaultColor);
        //I dont understand these calculations.
        double diff = Math.min(100, Math.max(0, (mouseX - x) / 1.9));

        if (sliding) {
            if (diff <= 0) {
                value = min;
            } else {
                value = MathUtils.round(((diff / 100) * (max - min) + min), roundingPlace);
            }
            postSettingChange();
        }

        value = MathHelper.clamp(value, min, max);
        float valueWidth = Renderer2D.getFxStringWidth(value + ".00") + 3;

        inputBox.render(drawContext, (x + 180) - Math.round(valueWidth), y + 2, mouseX, mouseY, textRenderer);
        // Calculate the width of the input box based on the width of the value
        inputBox.setWidth(Math.round(valueWidth));
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 2, y + 16, 188, 2, 1, 0xFFAAAAAA);

        int scaledValue = (int) ((value - min) / (max - min) * 188) + 2;
        // Slider background
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 2, y + 16, scaledValue, 2, 1, ColorManager.INSTANCE.clickGuiSecondary());
        // Slider
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + scaledValue, y + 14f, 2, 6, 1, 0xFFFFFFFF);
        Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + scaledValue, y + 19f, 2, 1, ColorUtils.changeAlpha(Color.DARK_GRAY, 105).getRGB());

        if (hovered(mouseX, mouseY)) {
            hovertimer++;
        } else {
            hovertimer = 0;
        }

        if (hovertimer >= 10) {
            Tooltip.tooltip.changeText(description);
        }
        if (!inputBox.isFocused()) {
            inputBox.setValue(String.valueOf(value));
        }
    }

    @Override
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.renderCompact(drawContext, x, y, mouseX, mouseY, textRenderer);
        //  inputBox = null;

        String trimmedName = FontRenderers.Small_fxfontRenderer.trimToWidth(name,moduleWidth - 12);
        FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), trimmedName, x + 2, y + 2, ColorManager.INSTANCE.defaultTextColor());
        double diff = Math.min(moduleWidth - 10, Math.max(0, (mouseX - x)));

        if (sliding) {
            if (diff <= 0) {
                value = min;
            } else {
                value = MathUtils.round(((diff / (moduleWidth - 10)) * (max - min) + min), roundingPlace);
            }
            postSettingChange();
        }
        value = MathHelper.clamp(value, min, max);

        //Draw the value beside the name
        String valueString = "" + MathUtils.round(value, roundingPlace);
        FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), valueString, (x + moduleWidth - 10) - FontRenderers.Small_fxfontRenderer.getStringWidth(valueString), y + 2, ColorManager.INSTANCE.defaultTextColor());

        //Draw the slider itself and the background fill
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 2, y + 16, moduleWidth - 8, 2, 1, 0xFFAAAAAA);
        int scaledValue = (int) ((value - min) / (max - min) * (moduleWidth - 10)) + 2;
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 2, y + 16, scaledValue, 2, 1, 0xFF55FFFF);

        //Slider Bar which moves and cartoony shadow
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + scaledValue, y + 14, 2, 6, 1, 0xFFFFFFFF);
        Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + scaledValue, y + 19, 2, 1, ColorUtils.changeAlpha(Color.DARK_GRAY, 155).getRGB());
        if (hovered(mouseX, mouseY)) {
            hovertimer++;
        } else {
            hovertimer = 0;
        }

        if (hovertimer >= 50) {
            Tooltip.tooltip.changeText(description);
        }
    }


    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (hoveredSetting((int) mouseX, (int) mouseY) && hoveredOverReset(mouseX, mouseY)) {
            value = defaultValue;
            postSettingChange();
        }
        if (hovered((int) mouseX, (int) mouseY) && button == 0 && !inputBox.isFocused() && !inputBox.isFocusedHover(mouseX, mouseY)) {
            this.sliding = true;
        }
        if (!inputBox.isFocused()) {
            inputBox.setFocused(false);
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        sliding = false;
        postSettingChange();
    }

    @Override
    public Double get() {
        return this.value;
    }

    @Override
    public Object saveToFile(List<Object> objectList) {
        return value;
    }

    @Override
    public void loadFromFile(Map<String, Object> MAP) {
        if (MAP.get(getSaveName()) == null) {
            value = defaultValue;
        } else {
            value = (double) MAP.get(getSaveName());
        }
        postSettingChange();
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
        if ((keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_ENTER) && inputBox.isFocused()) {
            try {
                double newVal = Double.parseDouble(inputBox.getValue());
                if (newVal <= min) {
                    newVal = min;
                }
                if (newVal >= max) {
                    newVal = max;
                }
                value = newVal;
                inputBox.setValue(String.valueOf(value));
            } catch (NumberFormatException ignored) {
            }
            inputBox.setFocused(false);
        }
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        super.charTyped(chr, modifiers);
        //  inputBox.charTyped(chr, modifiers);
        if (inputBox.isFocused()) {
            try {
                double newVal = Double.parseDouble(inputBox.getValue());
                if (newVal <= min) {
                    newVal = min;
                }
                if (newVal >= max) {
                    newVal = max;
                }
                value = newVal;
                inputBox.setValue(String.valueOf(value));
                postSettingChange();
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public static class Builder extends SettingBuilder<Builder, Double, DoubleSetting> {
        ISettingChange ISettingChange;
        double min, max;
        int roundingPlace;

        public Builder() {
            super(0.0D);
        }

        public Builder onSettingChange(ISettingChange ISettingChange) {
            this.ISettingChange = ISettingChange;
            return this;
        }

        public Builder roundingPlace(int roundingPlace) {
            this.roundingPlace = roundingPlace;
            return this;
        }

        public Builder min(double min) {
            this.min = min;
            return this;
        }

        public Builder max(double max) {
            this.max = max;
            return this;
        }

        public Builder range(double min, double max) {
            this.min = min;
            this.max = max;
            return this;
        }

        public Builder defaultValue(float val) {
            this.defaultValue = (double) val;
            return this;
        }

        @Override
        public DoubleSetting build() {
            if (defaultValue == null) {
                defaultValue = value;
            }
            return new DoubleSetting(name, description, ISettingChange, value, min, max, roundingPlace, shouldRender, defaultValue);
        }
    }
}
