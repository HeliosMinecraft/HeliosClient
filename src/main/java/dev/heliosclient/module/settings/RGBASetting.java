package dev.heliosclient.module.settings;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
public class RGBASetting extends Setting {

    private Color color;
    private float hue, saturation, brightness, alpha;
    private int handleX, handleY, alphaHandleY, shadeHandleX, shadeHandleY;
    private final int boxHeight = 70;
    private final int boxWidth = 70;
    private final int sliderWidth = 10;
    private final int offsetX = 10; // Offset from the left
    private final int offsetY = 20; // Offset from the top

    public RGBASetting(String name, String description, Color defaultColor) {
        this.name = name;
        this.description = description;
        this.color = defaultColor;
        this.height = 100;
        this.heightCompact = 0;
        float[] hsbvals = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        this.hue = hsbvals[0];
        this.saturation = hsbvals[1];
        this.brightness = hsbvals[2];
        this.alpha = color.getAlpha() / 255f;
        this.handleX = (int) (hue * width);
        this.handleY = (int) ((1 - saturation) * height);
        this.alphaHandleY = (int) ((1 - alpha) * boxHeight);
        this.shadeHandleX = (int) (brightness * boxWidth);
        this.shadeHandleY = (int) ((1 - saturation) * boxHeight);
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        FontManager.fxfontRenderer.drawString(drawContext.getMatrices(),name,x + 3 ,y + 2,-1,10f);

        int color1 =   hoveredOverGradientBox(mouseX,mouseY)? Color.DARK_GRAY.getRGB() :Color.BLACK.brighter().getRGB();
        int color2 =  hoveredOverAlphaSlider(mouseX,mouseY)? Color.DARK_GRAY.getRGB() :Color.BLACK.brighter().getRGB();
        int color3 =  hoveredOverBrightnessSaturationBox(mouseX,mouseY)? Color.DARK_GRAY.getRGB():Color.BLACK.brighter().getRGB();


        drawGradientBox(drawContext, x + offsetX , y + offsetY , brightness,color1);
        drawAlphaSlider(drawContext, x + offsetX + boxWidth + sliderWidth , y + offsetY,color2);
        drawBrightnessSaturationBox(drawContext, x + offsetX + boxWidth + sliderWidth * 3 , y + offsetY , hue,color3);

        FontManager.fxfontRenderer.drawString(drawContext.getMatrices(),"Alpha",x + offsetX + boxWidth + sliderWidth - 2 ,y + 94,-1,5f);

        // Draw the handles
        Renderer2D.drawFilledCircle(drawContext, x + offsetX + handleX , y + offsetY + handleY , 1 ,-1);
        Renderer2D.drawRectangle(drawContext, x + offsetX + boxWidth + sliderWidth - 2 , y + offsetY +alphaHandleY, sliderWidth + 4 , 3 ,-1);
        Renderer2D.drawFilledCircle(drawContext, x + offsetX + boxWidth + sliderWidth * 3 + shadeHandleX , y + offsetY + shadeHandleY , 1 ,-1);

        // Draw the preview box
        Renderer2D.drawRoundedRectangle(drawContext, x + 170  , y + 2 , 20 , 10 ,2, color.getRGB());
    }
    public void drawGradientBox(DrawContext drawContext, int x, int y, float brightness, int color)
    {
        // Draw the color gradient box
        for (int i = 0; i < boxWidth; i++) {
            for (int j = 0; j < boxHeight; j++) {
                float h = i / (float)boxWidth; // Hue varies from 0.0 to 1.0 as you go from left to right in the box
                float s = 1.0f - j / (float)boxHeight; // Saturation varies from 1.0 to 0.0 as you go from top to bottom in the box
                Color c = Color.getHSBColor(h, s, brightness);
                Renderer2D.drawRectangle(drawContext, x + i , y + j ,1 ,1 , c.getRGB());
            }
        }

        Renderer2D.drawOutlineRoundedBox(drawContext,x- 1,y - 1,boxWidth + 2,boxHeight + 2,1,1,color);
    }
    public void drawAlphaSlider(DrawContext drawContext, int x,int y,int color2) {
        // Draw the alpha slider
        for (int j = 0; j < boxHeight; j++) {
            float a = 1.0f - j / (float)boxHeight;
            Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(a * 255));
            Renderer2D.drawRectangle(drawContext, x , y + j , sliderWidth , 1 , c.getRGB());
        }
        Renderer2D.drawOutlineRoundedBox(drawContext,x -1 ,y - 1,sliderWidth+ 2,boxHeight+ 2,1,1,color2);
    }
    public void drawBrightnessSaturationBox(DrawContext drawContext, int x, int y, float hue, int color3)
    {
        // Draw the brightness-saturation gradient box
        for (int i = 0; i < boxWidth; i++) {
            for (int j = 0; j < boxHeight; j++) {
                float b = i / (float)boxWidth; // Brightness varies from 0.0 to 1.0 as you go from left to right in the box
                float s = 1.0f - j / (float)boxHeight; // Saturation varies from 1.0 to 0.0 as you go from top to bottom in the box
                Color c = Color.getHSBColor(hue, s, b);
                Renderer2D.drawRectangle(drawContext, x + i , y + j ,1 ,1 , c.getRGB());
            }
        }
        Renderer2D.drawOutlineRoundedBox(drawContext,x - 1,y- 1,boxWidth + 2,boxHeight + 2,1,1,color3);
    }

    public boolean hoveredOverGradientBox(double mouseX, double mouseY){
        return mouseX > x + offsetX && mouseX < x + offsetX + boxWidth && mouseY > y + offsetY && mouseY < y + offsetY + boxHeight;
    }
    public boolean hoveredOverAlphaSlider(double mouseX, double mouseY){
        return mouseX > x + offsetX + boxWidth + sliderWidth && mouseX < x + offsetX + boxWidth + sliderWidth + sliderWidth && mouseY > y + offsetY && mouseY < y + offsetY + boxHeight ;
    }
    public boolean hoveredOverBrightnessSaturationBox(double mouseX, double mouseY){
        return mouseX >x + offsetX + boxWidth + sliderWidth * 3   && mouseX < x +offsetX + boxWidth + sliderWidth * 3 + boxWidth && mouseY > y + offsetY && mouseY < y + offsetY + boxHeight;
    }
    @Override
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.renderCompact(drawContext, x, y, mouseX, mouseY, textRenderer);
    }
    private void mouse(double mouseX, double mouseY){

        if (hoveredOverGradientBox(mouseX,mouseY)) {
            handleX = (int)(mouseX - x - offsetX);
            handleY = (int)(mouseY - y - offsetY);
            hue = handleX / (float)boxWidth;
            saturation = 1.0f - handleY / (float)boxHeight;
            color = Color.getHSBColor(hue, saturation, brightness);
            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 255));
        }

        else if (hoveredOverAlphaSlider(mouseX,mouseY)) {
            alphaHandleY = (int)(mouseY - y - offsetY);
            alpha = 1.0f - alphaHandleY / (float)boxHeight;
            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 255));
        }
        else if(hoveredOverBrightnessSaturationBox(mouseX,mouseY)){
            shadeHandleX = (int)(mouseX -x - offsetX - boxWidth -sliderWidth * 3);
            shadeHandleY = (int)(mouseY - y - offsetY);
            brightness = shadeHandleX / (float)boxWidth;
            saturation = 1.0f - shadeHandleY / (float)boxHeight;
            color = Color.getHSBColor(hue, saturation, brightness);
            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 255));
        }

    }
    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
            mouse(mouseX, mouseY);
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            mouse(mouseX, mouseY);
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
}

