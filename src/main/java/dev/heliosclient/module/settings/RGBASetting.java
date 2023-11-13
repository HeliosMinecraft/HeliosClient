package dev.heliosclient.module.settings;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.FontChangeEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.ui.clickgui.RGBASettingScreen;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.InputBox;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.fontutils.fxFontRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.function.BooleanSupplier;

public class RGBASetting extends Setting<Color> implements Listener {

    private final RGBASettingScreen screen = new RGBASettingScreen(this);
    private float hue, saturation, brightness, alpha;
    private int handleX, handleY, alphaHandleY, shadeHandleX, shadeHandleY;
    private final int boxHeight = 70;
    private final int boxWidth = 70;
    private final int sliderWidth = 10;
    private final int offsetX = 10; // Offset from the left
    private final int offsetY = 20; // Offset from the top
    private boolean rainbow = false;
    private final boolean defaultRainbow;
    public Color value;

    // Add new fields to store calculated values
    private int gradientBoxX, gradientBoxY, gradientBoxWidth, gradientBoxHeight;
    private int alphaSliderX, alphaSliderY, alphaSliderWidth, alphaSliderHeight;
    private int brightnessSaturationBoxX, brightnessSaturationBoxY, brightnessSaturationBoxWidth, brightnessSaturationBoxHeight;
    private fxFontRenderer fxFontRenderer;
    private Screen parentScreen = null;
    private final Module_ module;
    public InputBox hexInput;

    public RGBASetting(String name, String description, Color defaultColor, boolean rainbow, Module_ module, BooleanSupplier shouldRender) {
        super(shouldRender, defaultColor);
        this.name = name;
        this.description = description;
        this.value = defaultColor;
        this.rainbow = rainbow;
        this.defaultRainbow = rainbow;
        this.height = 25;
        this.heightCompact = 17;
        float[] hsbvals = Color.RGBtoHSB(value.getRed(), value.getGreen(), value.getBlue(), null);
        this.hue = hsbvals[0];
        this.saturation = hsbvals[1];
        this.brightness = hsbvals[2];
        this.alpha = value.getAlpha() / 255f;
        this.handleX = (int) (hue * width);
        this.handleY = (int) ((1 - saturation) * (boxHeight - 60));
        this.alphaHandleY = (int) ((1 - alpha) * boxHeight);
        this.shadeHandleX = (int) (brightness * boxWidth);
        this.shadeHandleY = (int) ((1 - saturation) * boxHeight);
        this.module = module;
        if (MinecraftClient.getInstance().getWindow() != null) {
            fxFontRenderer = new fxFontRenderer(FontManager.fonts, 5f);
        }
        EventManager.register(this);
        // Calculate values once and store them
        this.gradientBoxX = x + offsetX;
        this.gradientBoxY = y + offsetY;
        this.gradientBoxWidth = boxWidth;
        this.gradientBoxHeight = boxHeight - 60;

        this.alphaSliderX = x + offsetX + boxWidth + sliderWidth;
        this.alphaSliderY = y + offsetY;
        this.alphaSliderWidth = sliderWidth;
        this.alphaSliderHeight = boxHeight;

        this.brightnessSaturationBoxX = x + offsetX + boxWidth + sliderWidth * 3;
        this.brightnessSaturationBoxY = y + offsetY;
        this.brightnessSaturationBoxWidth = boxWidth;
        this.brightnessSaturationBoxHeight = boxHeight;

        hexInput = new InputBox(50, 11, ColorUtils.colorToHex(value), 7, InputBox.InputMode.ALL);
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        this.x = x;
        this.y = y;
        Renderer2D.drawFixedString(drawContext.getMatrices(), name, x + 3, y + 2, -1);
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 170, y + 2, 15, 15, 2, value.getRGB());

        if (rainbow) {
            value = ColorUtils.changeAlpha(ColorUtils.getRainbowColor(), value.getAlpha());
        }
    }

    public void renderSetting(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        this.x = x;
        this.y = y;

        Renderer2D.drawFixedString(drawContext.getMatrices(), name, x + 3, y + 2, -1);
        Renderer2D.drawRoundedRectangleWithShadow(drawContext.getMatrices(), x + 170, y + 2, 15, 15, 2, 4, value.getRGB());

        int value1 = hoveredOverGradientBox(mouseX, mouseY) ? Color.DARK_GRAY.getRGB() : Color.BLACK.brighter().getRGB();
        int value2 = hoveredOverAlphaSlider(mouseX, mouseY) ? Color.DARK_GRAY.getRGB() : Color.BLACK.brighter().getRGB();
        int value3 = hoveredOverBrightnessSaturationBox(mouseX, mouseY) ? Color.DARK_GRAY.getRGB() : Color.BLACK.brighter().getRGB();

        if (rainbow) {
            updateHandles();
        }
        this.gradientBoxX = x + offsetX;
        this.gradientBoxY = y + offsetY;
        this.gradientBoxWidth = boxWidth;
        this.gradientBoxHeight = boxHeight - 60;

        this.alphaSliderX = x + offsetX + boxWidth + sliderWidth;
        this.alphaSliderY = y + offsetY;
        this.alphaSliderWidth = sliderWidth;
        this.alphaSliderHeight = boxHeight;

        this.brightnessSaturationBoxX = x + offsetX + boxWidth + sliderWidth * 3;
        this.brightnessSaturationBoxY = y + offsetY;
        this.brightnessSaturationBoxWidth = boxWidth;
        this.brightnessSaturationBoxHeight = boxHeight;

        drawGradientBox(drawContext, gradientBoxX, gradientBoxY, value1);
        drawAlphaSlider(drawContext, alphaSliderX, alphaSliderY, value2);
        drawBrightnessSaturationBox(drawContext, brightnessSaturationBoxX, brightnessSaturationBoxY, hue, value3);

        //Draw Rainbow button bg
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + offsetX - 1, y + offsetY + gradientBoxHeight + 6, Renderer2D.getFxStringWidth("Rainbow ") + 1, Renderer2D.getFxStringHeight("Rainbow ") + 1, 2, Color.DARK_GRAY.getRGB());
        Renderer2D.drawFixedString(drawContext.getMatrices(), "Rainbow ", x + offsetX + 1, y + offsetY + gradientBoxHeight + 7, rainbow ? Color.GREEN.getRGB() : Color.RED.getRGB());


        //Render the texts
        Renderer2D.drawFixedString(drawContext.getMatrices(), "Alpha: " + value.getAlpha(), gradientBoxX, y + offsetY + gradientBoxHeight + Renderer2D.getFxStringHeight() + 9, -1);
        Renderer2D.drawFixedString(drawContext.getMatrices(), "Red: " + value.getRed(), gradientBoxX, y + offsetY + gradientBoxHeight + Renderer2D.getFxStringHeight() + 18, -1);
        Renderer2D.drawFixedString(drawContext.getMatrices(), "Green: " + value.getGreen(), gradientBoxX, y + offsetY + gradientBoxHeight + Renderer2D.getFxStringHeight() + 27, -1);
        Renderer2D.drawFixedString(drawContext.getMatrices(), "Blue: " + value.getBlue(), gradientBoxX, y + offsetY + gradientBoxHeight + Renderer2D.getFxStringHeight() + 36, -1);

        // Draw the handles
        Renderer2D.drawFilledCircle(drawContext.getMatrices().peek().getPositionMatrix(), gradientBoxX + handleX, gradientBoxY + handleY, 1, -1);
        Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), alphaSliderX - 2, alphaSliderY + alphaHandleY, sliderWidth + 4, 3, -1);
        Renderer2D.drawFilledCircle(drawContext.getMatrices().peek().getPositionMatrix(), brightnessSaturationBoxX + shadeHandleX, brightnessSaturationBoxY + shadeHandleY, 1, -1);

        hexInput.render(drawContext, gradientBoxX, Math.round(y + offsetY + gradientBoxHeight + Renderer2D.getFxStringHeight() + 47), mouseX, mouseY, textRenderer);
        if (!hexInput.isFocused()) {
            hexInput.setText(ColorUtils.colorToHex(value));
        }
    }

    public void drawGradientBox(DrawContext drawContext, int x, int y, int value) {

        // Draw the value gradient box
        Renderer2D.drawRainbowGradient(drawContext.getMatrices().peek().getPositionMatrix(), x, y, boxWidth, gradientBoxHeight);

        Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x - 1, y - 1, boxWidth + 2, gradientBoxHeight + 2, 1, 1, value);
    }


    public void drawAlphaSlider(DrawContext drawContext, int x, int y, int value3) {
        // Draw the alpha slider
        Color value1 = new Color(value.getRed(), value.getGreen(), value.getBlue(), 0);
        Color value2 = new Color(value.getRed(), value.getGreen(), value.getBlue(), 255);
        Renderer2D.drawGradient(drawContext.getMatrices().peek().getPositionMatrix(), x, y, sliderWidth, boxHeight, value1.getRGB(), value2.getRGB());

        Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x - 1, y - 1, sliderWidth + 2, boxHeight + 2, 1, 1, value3);

        fxFontRenderer.drawString(drawContext.getMatrices(), "Alpha", x - 1, y + boxHeight + 1, -1); // Below the alpha slider
    }

    public void drawBrightnessSaturationBox(DrawContext drawContext, int x, int y, float hue, int value3) {
        Renderer2D.drawRoundedGradientRectangleWithShadow(drawContext.getMatrices(), x, y, boxWidth, boxHeight, Color.BLACK, Color.WHITE, Color.getHSBColor(hue, 1.0f, 1.0f), Color.BLACK, 2, 4, getColor());

        Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x - 1, y - 1, boxWidth + 2, boxHeight + 2, 3, 1, value3);
    }

    public void updateHandles() {
        float[] hsbvals = Color.RGBtoHSB(value.getRed(), value.getGreen(), value.getBlue(), null);
        this.hue = hsbvals[0];
        this.saturation = hsbvals[1];
        this.brightness = hsbvals[2];
        this.alpha = value.getAlpha() / 255f;
        this.handleX = (int) (hue * width);
        this.handleY = (int) ((1 - saturation) * gradientBoxHeight);
        this.alphaHandleY = (int) ((1 - alpha) * boxHeight);
        this.shadeHandleX = (int) (brightness * boxWidth);
        this.shadeHandleY = (int) ((1 - saturation) * boxHeight);
    }


    public boolean hoveredOverRainbowBool(double mouseX, double mouseY) {
        return mouseX > gradientBoxX - 1 && mouseX < gradientBoxX - 1 + Renderer2D.getFxStringWidth("Rainbow ") + 3 && mouseY > y + offsetY + gradientBoxHeight + 5 && mouseY < y + offsetY + gradientBoxHeight + 6 + Renderer2D.getFxStringHeight("Rainbow ") + 2;
    }

    public boolean hoveredOverGradientBox(double mouseX, double mouseY) {
        return mouseX > gradientBoxX && mouseX < gradientBoxX + gradientBoxWidth && mouseY > gradientBoxY && mouseY < gradientBoxY + gradientBoxHeight;
    }

    public boolean hoveredOverAlphaSlider(double mouseX, double mouseY) {
        return mouseX > alphaSliderX && mouseX < alphaSliderX + alphaSliderWidth && mouseY > alphaSliderY && mouseY < alphaSliderY + alphaSliderHeight;
    }

    public boolean hoveredOverBrightnessSaturationBox(double mouseX, double mouseY) {
        return mouseX > brightnessSaturationBoxX && mouseX < brightnessSaturationBoxX + brightnessSaturationBoxWidth && mouseY > brightnessSaturationBoxY && mouseY < brightnessSaturationBoxY + brightnessSaturationBoxHeight;
    }

    @Override
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.renderCompact(drawContext, x, y, mouseX, mouseY, textRenderer);
        compactFont.drawString(drawContext.getMatrices(), name.substring(0, Math.min(12, name.length())) + "...", x + 3, y + 1, -1);
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 71, y + 1, 10, 10, 2, value.getRGB());

        if (rainbow) {
            value = ColorUtils.getRainbowColor();
        }
    }

    public void mouse(double mouseX, double mouseY, int button) {
        hexInput.mouseClicked(mouseX, mouseY, button);
        if (hoveredOverGradientBox(mouseX, mouseY)) {
            handleX = (int) (mouseX - x - offsetX);
            handleY = (int) (mouseY - y - offsetY);
            hue = handleX / (float) boxWidth;
            saturation = 1.0f - (float) handleY / gradientBoxHeight;
        } else if (hoveredOverAlphaSlider(mouseX, mouseY)) {
            alphaHandleY = (int) (mouseY - y - offsetY);
            alpha = 1.0f - alphaHandleY / (float) boxHeight;
        } else if (hoveredOverBrightnessSaturationBox(mouseX, mouseY)) {
            shadeHandleX = (int) (mouseX - x - offsetX - boxWidth - sliderWidth * 3);
            shadeHandleY = (int) (mouseY - y - offsetY);
            brightness = shadeHandleX / (float) boxWidth;
            saturation = 1.0f - shadeHandleY / (float) boxHeight;
        } else if (hoveredOverRainbowBool(mouseX, mouseY)) {
            rainbow = !rainbow;
        }
        if (rainbow) {
            updateHandles();
        } else {
            value = Color.getHSBColor(hue, saturation, brightness);
        }
        value = new Color(value.getRed(), value.getGreen(), value.getBlue(), (int) (alpha * 255));
    }

    public void keyPress(int keyCode) {
        if ((keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_ENTER) && hexInput.isFocused()) {
            if (ColorUtils.isHexColor(hexInput.getValue())) {
                try {
                    value = ColorUtils.hexToColor(hexInput.getValue());
                    updateHandles();
                } catch (Exception e) {
                    e.printStackTrace();
                    // Something went wrong
                }
            }
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (hoveredSetting((int) mouseX, (int) mouseY) && hoveredOverReset(mouseX, mouseY)) {
            value = defaultValue;
            rainbow = defaultRainbow;
            updateHandles();
        }
        super.mouseClicked(mouseX, mouseY, button);
        if (hovered((int) mouseX, (int) mouseY)) {
            MinecraftClient.getInstance().setScreen(screen);
        }
    }

    public Color getColor() {
        return value;
    }

    public void setColor(Color value) {
        this.value = value;
    }

    public float getBrightness() {
        return brightness;
    }

    public float getHue() {
        return hue;
    }

    public float getSaturation() {
        return saturation;
    }

    public boolean isRainbow() {
        return rainbow;
    }

    public boolean isDefaultRainbow() {
        return defaultRainbow;
    }

    @SubscribeEvent
    public void onFontChange(FontChangeEvent event) {
        fxFontRenderer = new fxFontRenderer(event.getFonts(), 5f);
    }

    public Screen getParentScreen() {
        return parentScreen;
    }

    public void setParentScreen(Screen parentScreen) {
        this.parentScreen = parentScreen;
    }

    public static class Builder extends SettingBuilder<Builder, Color, RGBASetting> {
        boolean rainbow = false;
        Module_ module;
        public Builder() {
            super(new Color(-1));
        }

        public Builder rainbow(boolean rainbow) {
            this.rainbow = rainbow;
            return this;
        }

        public Builder module(Module_ module) {
            this.module = module;
            return this;
        }
        @Override
        public RGBASetting build() {
            return new RGBASetting(name, description, value, rainbow, module, shouldRender);
        }
    }
}