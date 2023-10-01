package dev.heliosclient.module.settings;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.InputBox;
import dev.heliosclient.util.MathUtils;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class DoubleSetting extends Setting {
    private final double min, max;
    private final int roundingPlace;
    public double value;
    Module_ module;

    boolean sliding = false;
    private InputBox inputBox;

    public DoubleSetting(String name, String description, Module_ module, double value, double min, double max, int roundingPlace) {
        this.name = name;
        this.description = description;
        this.value = value;
        this.min = min;
        this.max = max;
        this.heightCompact = 24;
        this.module = module;
        this.roundingPlace = roundingPlace;
        inputBox = new InputBox(String.valueOf(max).length() * 6,11,String.valueOf(value),10, InputBox.InputMode.DIGITS);
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        int defaultColor = ColorManager.INSTANCE.defaultTextColor();

        FontManager.fxfontRenderer.drawString(drawContext.getMatrices(),name,x + 2, y + 2, defaultColor,10f);
     //   drawContext.drawText(textRenderer, Text.literal(name), x + 2, y + 2, ColorManager.INSTANCE.defaultTextColor(), false);
        double diff = Math.min(100, Math.max(0, (mouseX - x) / 1.9));

        if (sliding) {
            if (diff == 0) {
                value = min;
            } else {
                value = MathUtils.round(((diff / 100) * (max - min) + min), roundingPlace);
            }
            module.onSettingChange(this);
        }

        float valueWidth = FontManager.fxfontRenderer.getStringWidth(value + ".00") + 3;

        inputBox.render(drawContext,(x + 180) - Math.round(valueWidth),y + 2,mouseX,mouseY,textRenderer);
        // Calculate the width of the input box based on the width of the value
        inputBox.setWidth(Math.round(valueWidth));
        Renderer2D.drawRoundedRectangle(drawContext, x + 2, y + 16, 188, 2, 1, 0xFFAAAAAA);

        int scaledValue = (int) ((value - min) / (max - min) * 188) + 2;
        // Slider background
        Renderer2D.drawRoundedRectangle(drawContext, x + 2, y + 16, scaledValue, 2, 1, ColorManager.INSTANCE.clickGuiSecondary());
        // Slider
        Renderer2D.drawRoundedRectangle(drawContext, x + scaledValue, y + 14, 2, 6, 1, 0xFFFFFFFF);

        if (hovered(mouseX, mouseY)) {
            hovertimer++;
        } else {
            hovertimer = 0;
        }

        if (hovertimer >= 150) {
            Tooltip.tooltip.changeText(description);
        }
        if(!inputBox.isFocused()) {
            inputBox.setValue(String.valueOf(value));
        }
    }

    @Override
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.renderCompact(drawContext, x, y, mouseX, mouseY, textRenderer);
      //  inputBox = null;
        FontManager.fxfontRenderer.drawString(drawContext.getMatrices(),name.substring(0, Math.min(12, name.length())) + "...",x + 2, y + 2, ColorManager.INSTANCE.defaultTextColor(),8f);
        double diff = Math.min(moduleWidth - 10, Math.max(0, (mouseX - x)));

        if (sliding) {
            if (diff == 0) {
                value = min;
            } else {
                value = MathUtils.round(((diff / (moduleWidth - 10)) * (max - min) + min), roundingPlace);
            }
            module.onSettingChange(this);
        }

        String valueString = "" + MathUtils.round(value, roundingPlace);
        FontManager.fxfontRenderer.drawString(drawContext.getMatrices(),valueString,(x + moduleWidth - 10) - FontManager.fxfontRenderer.getStringWidth(valueString), y + 2, ColorManager.INSTANCE.defaultTextColor(),8f);
        Renderer2D.drawRoundedRectangle(drawContext, x + 2, y + 16, moduleWidth - 8, 2, 1, 0xFFAAAAAA);
        int scaledValue = (int) ((value - min) / (max - min) * (moduleWidth - 10)) + 2;
        Renderer2D.drawRoundedRectangle(drawContext, x + 2, y + 16, scaledValue, 2, 1, 0xFF55FFFF);
        Renderer2D.drawRoundedRectangle(drawContext, x + scaledValue, y + 14, 2, 6, 1, 0xFFFFFFFF);
        if (hovered(mouseX, mouseY)) {
            hovertimer++;
        } else {
            hovertimer = 0;
        }

        if (hovertimer >= 150) {
            Tooltip.tooltip.changeText(description);
        }
    }


    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (hovered((int) mouseX, (int) mouseY) && button == 0 && !inputBox.mouseClicked(mouseX, mouseY, button)) {
            this.sliding = true;
        }
        if(!inputBox.mouseClicked(mouseX,mouseY,button)){
            inputBox.setFocused(false);
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        sliding = false;
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
       // inputBox.keyPressed(keyCode, scanCode, modifiers);
        if (keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_ENTER) {
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
            } catch (NumberFormatException e) {
                value = value;
            }
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
                module.onSettingChange(this);
            } catch (NumberFormatException e) {
                value = value;
            }
        }
    }
}
