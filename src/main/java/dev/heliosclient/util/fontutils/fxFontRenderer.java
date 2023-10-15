package dev.heliosclient.util.fontutils;

import dev.heliosclient.util.ColorUtils;
import me.x150.renderer.font.FontRenderer;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class fxFontRenderer extends FontRenderer {

    /**
     * Initializes a new FontRenderer with the specified fonts
     *
     * @param fonts  The fonts to use. The font renderer will go over each font in this array, search for the glyph, and render it if found. If no font has the specified glyph, it will draw the missing font symbol.
     * @param sizePx The size of the font in minecraft pixel units. One pixel unit = `guiScale` pixels
     */
    public fxFontRenderer(Font[] fonts, float sizePx) {
        super(fonts, sizePx);
    }

    public void drawString(MatrixStack matrixStack, String text, float x, float y, int color) {

        int r = 256 - ColorUtils.getRed(color);
        int g = 256 - ColorUtils.getGreen(color);
        int b = 256 - ColorUtils.getBlue(color);
        int a = 256 - ColorUtils.getAlpha(color);

        // Draw the text at the specified coordinates with the specified color
        //this.drawString(matrixStack, text, x / scaleFactor, y / scaleFactor, r, g, b, a);
        super.drawString(matrixStack, text, x, y, r, g, b, a);
    }

    public void drawCenteredString(MatrixStack stack, String s, float x, float y, int color) {
        int r = 256 - ColorUtils.getRed(color);
        int g = 256 - ColorUtils.getGreen(color);
        int b = 256 - ColorUtils.getBlue(color);
        int a = 256 - ColorUtils.getAlpha(color);

        super.drawCenteredString(stack, s, x , y, r, g, b, a);
    }

}
