package dev.heliosclient.util.fontutils;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.render.Renderer2D;
import me.x150.renderer.font.FontRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.awt.*;

/**
 * fx means Fixed, originally was made for rendering with fixed sizes.
 */
public class fxFontRenderer extends FontRenderer {

    /**
     * Initializes a new FontRenderer with the specified fonts
     *
     * @param fonts  The fonts to use. The font renderer will go over each font in this array, search for the glyph, and render it if found. If no font has the specified glyph, it will draw the missing font symbol.
     * @param sizePx The size of the font in minecraft pixel units. One pixel unit = `guiScale` pixels
     */
    public fxFontRenderer(Font fonts, float sizePx) {
        super(fonts, sizePx);
    }

    /**
     * Trims a string to fit within a specified width when rendered.
     *
     * @param text  The text to trim.
     * @param width The maximum width of the rendered text.
     * @return The trimmed text.
     *
     * @see dev.heliosclient.util.render.Renderer2D#wrapText(String, int, fxFontRenderer) , int)
     */
    public String trimToWidth(String text, float width) {
        float textWidth = this.getStringWidth(text);
        if (textWidth <= width) {
            return text;
        } else {
            String trimmedText = text;
            while (textWidth > width && !trimmedText.isEmpty()) {
                trimmedText = trimmedText.substring(0, trimmedText.length() - 1);
                textWidth = this.getStringWidth(trimmedText);
            }
            return trimmedText;
        }
    }

    public void drawString(MatrixStack matrixStack, String text, float x, float y, float scale, int color) {
        Renderer2D.scaleAndPosition(matrixStack,x,y,scale);
        this.drawString(matrixStack,text,x,y,color);
        Renderer2D.stopScaling(matrixStack);
    }

    public void drawString(MatrixStack matrixStack, String text, float x, float y, int color) {
        int r = 256 - ColorUtils.getRed(color);
        int g = 256 - ColorUtils.getGreen(color);
        int b = 256 - ColorUtils.getBlue(color);
        int a = 256 - ColorUtils.getAlpha(color);

        // Draw the text at the specified coordinates with the specified color
        //this.drawString(matrixStack, text, x / scaleFactor, y / scaleFactor, r, g, b, a);
        RenderSystem.setShaderColor(r/255.0F,g/255.0F,b/255.0F,a/255.0F);

        try {
            super.drawText(matrixStack, Text.of(text), x,y,a/255.0F);
        } catch (NullPointerException ignored) {}
        RenderSystem.setShaderColor(1.0F,1.0F,1.0F,1.0F);
    }

    public void drawCenteredString(MatrixStack stack, String s, float x, float y, int color) {
        int r = 256 - ColorUtils.getRed(color);
        int g = 256 - ColorUtils.getGreen(color);
        int b = 256 - ColorUtils.getBlue(color);
        int a = 256 - ColorUtils.getAlpha(color);

        RenderSystem.setShaderColor(r/255.0F,g/255.0F,b/255.0F,a/255.0F);
        try {
            super.drawCenteredText(stack, Text.of(s), x,y,a/255.0F);
        } catch (NullPointerException ignored) {
        }
        RenderSystem.setShaderColor(1.0F,1.0F,1.0F,1.0F);
    }

}
