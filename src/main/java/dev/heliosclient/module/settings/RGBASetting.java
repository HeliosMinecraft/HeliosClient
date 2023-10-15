package dev.heliosclient.module.settings;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.FontChangeEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.ui.clickgui.RGBASettingScreen;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.fontutils.fxFontRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public class RGBASetting extends Setting implements Listener {

    private RGBASettingScreen screen = new RGBASettingScreen(this);
    private Color color;
    private float hue, saturation, brightness, alpha;
    private int handleX, handleY, alphaHandleY, shadeHandleX, shadeHandleY;
    private final int boxHeight = 70;
    private final int boxWidth = 70;
    private final int sliderWidth = 10;
    private final int offsetX = 10; // Offset from the left
    private final int offsetY = 20; // Offset from the top
    private boolean rainbow = false;
    private float[] brightnessValues;
    private float[] saturationValues;
    float[] hueValues = new float[boxWidth];


    // Add new fields to store calculated values
    private int gradientBoxX, gradientBoxY, gradientBoxWidth, gradientBoxHeight;
    private int alphaSliderX, alphaSliderY, alphaSliderWidth, alphaSliderHeight;
    private int brightnessSaturationBoxX, brightnessSaturationBoxY, brightnessSaturationBoxWidth, brightnessSaturationBoxHeight;
    private fxFontRenderer fxFontRenderer;

    public RGBASetting(String name, String description, Color defaultColor) {
        this.name = name;
        this.description = description;
        this.color = defaultColor;
        this.height = 25;
        this.heightCompact = 20;
        float[] hsbvals = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        this.hue = hsbvals[0];
        this.saturation = hsbvals[1];
        this.brightness = hsbvals[2];
        this.alpha = color.getAlpha() / 255f;
        this.handleX = (int) (hue * width);
        this.handleY = (int) ((1 - saturation) * (boxHeight - 50));
        this.alphaHandleY = (int) ((1 - alpha) * boxHeight);
        this.shadeHandleX = (int) (brightness * boxWidth);
        this.shadeHandleY = (int) ((1 - saturation) * boxHeight);
        if (MinecraftClient.getInstance().getWindow() != null) {
            fxFontRenderer = new fxFontRenderer(FontManager.fonts, 5f);
        }
        EventManager.register(this);
        value = defaultColor.getRGB();
        // Calculate values once and store them
        this.gradientBoxX = x + offsetX;
        this.gradientBoxY = y + offsetY;
        this.gradientBoxWidth = boxWidth;
        this.gradientBoxHeight = boxHeight - 50;

        this.alphaSliderX = x + offsetX + boxWidth + sliderWidth;
        this.alphaSliderY = y + offsetY;
        this.alphaSliderWidth = sliderWidth;
        this.alphaSliderHeight = boxHeight;

        this.brightnessSaturationBoxX = x + offsetX + boxWidth + sliderWidth * 3;
        this.brightnessSaturationBoxY = y + offsetY;
        this.brightnessSaturationBoxWidth = boxWidth;
        this.brightnessSaturationBoxHeight = boxHeight;
        // Calculate brightness and saturation values once in the constructor
        brightnessValues = new float[boxWidth];
        saturationValues = new float[boxHeight];
        for (int i = 0; i < boxWidth; i++) {
            brightnessValues[i] = i / (float) boxWidth;
        }
        for (int j = 0; j < boxHeight; j++) {
            saturationValues[j] = 1.0f - j / (float) boxHeight;
        }
        for (int i = 0; i < boxWidth; i++) {
            hueValues[i] = i / (float) boxWidth;
        }

    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        Renderer2D.drawFixedString(drawContext.getMatrices(), name, x + 3, y + 2, -1);
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 170, y + 2, 15, 15, 2, color.getRGB());

        if (rainbow) {
            color = ColorUtils.getRainbowColor();
        }
    }

    public void renderSetting(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        Renderer2D.drawFixedString(drawContext.getMatrices(), name, x + 3, y + 2, -1);

        int color1 = hoveredOverGradientBox(mouseX, mouseY) ? Color.DARK_GRAY.getRGB() : Color.BLACK.brighter().getRGB();
        int color2 = hoveredOverAlphaSlider(mouseX, mouseY) ? Color.DARK_GRAY.getRGB() : Color.BLACK.brighter().getRGB();
        int color3 = hoveredOverBrightnessSaturationBox(mouseX, mouseY) ? Color.DARK_GRAY.getRGB() : Color.BLACK.brighter().getRGB();

        if (rainbow) {
            color = ColorUtils.changeAlpha(ColorUtils.getRainbowColor(), color.getAlpha());
            float[] hsbvals = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
            hue = hsbvals[0];
            saturation = hsbvals[1];
            brightness = hsbvals[2];
            handleX = Math.min((int) (hue * boxWidth), boxWidth);
            handleY = Math.min((int) ((1 - saturation) * (boxHeight - 50)), boxHeight - 50);
            shadeHandleX = Math.min((int) (brightness * boxWidth), boxWidth);
            shadeHandleY = Math.min((int) ((1 - saturation) * boxHeight), boxHeight);
        }
        this.gradientBoxX = x + offsetX;
        this.gradientBoxY = y + offsetY;
        this.gradientBoxWidth = boxWidth;
        this.gradientBoxHeight = boxHeight - 50;

        this.alphaSliderX = x + offsetX + boxWidth + sliderWidth;
        this.alphaSliderY = y + offsetY;
        this.alphaSliderWidth = sliderWidth;
        this.alphaSliderHeight = boxHeight;

        this.brightnessSaturationBoxX = x + offsetX + boxWidth + sliderWidth * 3;
        this.brightnessSaturationBoxY = y + offsetY;
        this.brightnessSaturationBoxWidth = boxWidth;
        this.brightnessSaturationBoxHeight = boxHeight;

        drawGradientBox(drawContext, gradientBoxX, gradientBoxY, brightness, color1);
        drawAlphaSlider(drawContext, alphaSliderX, alphaSliderY, color2);
        drawBrightnessSaturationBox(drawContext, brightnessSaturationBoxX, brightnessSaturationBoxY, hue, color3);

        //Draw Rainbow button bg
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + offsetX - 1, y + boxHeight - 26, Renderer2D.getFxStringWidth("Rainbow ") + 1, Renderer2D.getFxStringHeight("Rainbow ") + 1, 2, Color.DARK_GRAY.getRGB());

        //Render the texts
        fxFontRenderer.drawString(drawContext.getMatrices(), "Alpha", x + offsetX + boxWidth + sliderWidth - 2, y + 94, -1);
        Renderer2D.drawFixedString(drawContext.getMatrices(), "Rainbow ", x + offsetX + 1, y + boxHeight - 25, rainbow ? Color.GREEN.getRGB() : Color.RED.getRGB());
        Renderer2D.drawFixedString(drawContext.getMatrices(), "Alpha: " + color.getAlpha(), gradientBoxX, y + boxHeight + Renderer2D.getFxStringHeight() - 23, -1);
        Renderer2D.drawFixedString(drawContext.getMatrices(), "Red: " + color.getRed(), gradientBoxX, y + boxHeight + Renderer2D.getFxStringHeight() - 13, -1);
        Renderer2D.drawFixedString(drawContext.getMatrices(), "Green: " + color.getGreen(), gradientBoxX, y + boxHeight + Renderer2D.getFxStringHeight() - 4, -1);
        Renderer2D.drawFixedString(drawContext.getMatrices(), "Blue: " + color.getBlue(), gradientBoxX, y + boxHeight + Renderer2D.getFxStringHeight() + 5, -1);

        // Draw the handles
        Renderer2D.drawFilledCircle(drawContext.getMatrices().peek().getPositionMatrix(), gradientBoxX + handleX, gradientBoxY + handleY, 1, -1);
        Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), alphaSliderX - 2, alphaSliderY + alphaHandleY, sliderWidth + 4, 3, -1);
        Renderer2D.drawFilledCircle(drawContext.getMatrices().peek().getPositionMatrix(), brightnessSaturationBoxX + shadeHandleX, brightnessSaturationBoxY + shadeHandleY, 1, -1);

        // Draw the preview box
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 170, y + 2, 20, 10, 2, color.getRGB());
        value = color.getRGB();
    }

    public void drawGradientBox(DrawContext drawContext, int x, int y, float brightness, int color) {
        Color color1;

        // Draw the color gradient box
        for (int i = 0; i < boxWidth; i++) {
            color1 = Color.getHSBColor(hueValues[i], 1.0f, 1.0f);
            Renderer2D.drawGradient(drawContext.getMatrices().peek().getPositionMatrix(), x + i, y, 1, boxHeight - 50, color1.getRGB(), color1.getRGB());
        }

        Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x - 1, y - 1, boxWidth + 2, boxHeight - 50 + 2, 1, 1, color);
    }


    public void drawAlphaSlider(DrawContext drawContext, int x, int y, int color3) {
        // Draw the alpha slider
        Color color1 = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
        Color color2 = new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        Renderer2D.drawGradient(drawContext.getMatrices().peek().getPositionMatrix(), x, y, sliderWidth, boxHeight, color1.getRGB(), color2.getRGB());

        Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x - 1, y - 1, sliderWidth + 2, boxHeight + 2, 1, 1, color3);
    }

    public void drawBrightnessSaturationBox(DrawContext drawContext, int x, int y, float hue, int color3) {
        Color color;

        // Draw the brightness-saturation gradient box
        for (int i = 0; i < boxWidth; i++) {
            for (int j = 0; j < boxHeight; j++) {
                color = Color.getHSBColor(hue, saturationValues[j], brightnessValues[i]);
                Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + i, y + j, 1, 1, color.getRGB());
            }
        }

        Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x - 1, y - 1, boxWidth + 2, boxHeight + 2, 1, 1, color3);
    }


    public boolean hoveredOverRainbowBool(double mouseX, double mouseY) {
        return mouseX > gradientBoxX - 1 && mouseX < gradientBoxX - 1 + Renderer2D.getFxStringWidth("Rainbow ") + 3 && mouseY > y + boxHeight - 26 && mouseY < y + boxHeight - 26 + Renderer2D.getFxStringHeight("Rainbow ") + 2;
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
        Renderer2D.drawFixedString(drawContext.getMatrices(), name.substring(0, Math.min(12, name.length())) + "...", x + 3, y + 2, -1);
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 71, y + 2, 10, 10, 2, color.getRGB());

        if (rainbow) {
            color = ColorUtils.getRainbowColor();
        }
    }

    public void mouse(double mouseX, double mouseY) {

        if (hoveredOverGradientBox(mouseX, mouseY)) {
            handleX = (int) (mouseX - x - offsetX);
            handleY = (int) (mouseY - y - offsetY);
            hue = handleX / (float) boxWidth;
            saturation = 1.0f - handleY / (float) (boxHeight - 50);
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
            float[] hsbvals = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
            hue = hsbvals[0];
            saturation = hsbvals[1];
            brightness = hsbvals[2];
            handleX = Math.min((int) (hue * boxWidth), boxWidth);
            handleY = Math.min((int) ((1 - saturation) * (boxHeight - 50)), boxHeight - 50);
            shadeHandleX = Math.min((int) (brightness * boxWidth), boxWidth);
            shadeHandleY = Math.min((int) ((1 - saturation) * boxHeight), boxHeight);
        } else {
            color = Color.getHSBColor(hue, saturation, brightness);
        }
        color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255));

    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (hovered((int) mouseX, (int) mouseY)) {
            MinecraftClient.getInstance().setScreen(screen);
        }
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public float getAlpha() {
        return alpha;
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

    @SubscribeEvent
    public void onFontChange(FontChangeEvent event) {
        fxFontRenderer = new fxFontRenderer(event.getFonts(), 5f);
    }
}