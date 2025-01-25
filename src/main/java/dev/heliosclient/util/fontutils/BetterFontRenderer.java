package dev.heliosclient.util.fontutils;

import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.render.Renderer2D;
import me.x150.renderer.font.FontRenderer;
import me.x150.renderer.font.Glyph;
import me.x150.renderer.font.GlyphPageManager;
import me.x150.renderer.util.RendererUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.awt.*;

/**
 * fx means Fixed, originally was made for rendering with fixed sizes.
 */
public class BetterFontRenderer extends FontRenderer {

    private final Font roboto;
    private GlyphPageManager pageManager;

    /**
     * Initializes a new FontRenderer with the specified fonts
     *
     * @param fonts  The fonts to use. The font renderer will go over each font in this array, search for the glyph, and render it if found. If no font has the specified glyph, it will draw the missing font symbol.
     * @param sizePx The size of the font in minecraft pixel units. One pixel unit = `guiScale` pixels
     */
    public BetterFontRenderer(Font fonts, float sizePx) {
        super(fonts, sizePx);
        /* A quick dirty fix for an crash. Should be removed when everything works again */
        roboto = new Font("Arial Bold",Font.PLAIN,(int) sizePx);
        pageManager.close();
        pageManager = new GlyphPageManager(roboto, 33);
    }

    @Override
    protected void init(Font fonts, float sizePx) {
        int gs = RendererUtils.getGuiScale();
        if (gs != this.previousGameScale && pageManager != null) {
            pageManager.close();
        }
        if(!this.initialized){
            synchronized(this) {
                pageManager = new GlyphPageManager(roboto, 33);
            }
        }
        super.init(fonts, sizePx);
    }

    /**
     * Trims a string to fit within a specified width when rendered.
     *
     * @param text  The text to trim.
     * @param width The maximum width of the rendered text.
     * @return The trimmed text.
     *
     * @see dev.heliosclient.util.render.Renderer2D#wrapText(String, int, BetterFontRenderer) , int)
     */
    public String trimToWidth(String text, float width) {
        float textWidth = this.getTextWidth(Text.of(text));
        if (textWidth <= width) {
            return text;
        } else {
            String trimmedText = text;
            while (textWidth > width && !trimmedText.isEmpty()) {
                trimmedText = trimmedText.substring(0, trimmedText.length() - 1);
                textWidth = this.getTextWidth(Text.of(trimmedText));
            }
            return trimmedText;
        }
    }

    public void drawString(MatrixStack matrixStack, String text, float x, float y, float scale, int color) {
        Renderer2D.scaleAndPosition(matrixStack,x,y,scale);
        this.drawString(matrixStack,text,x,y,color);
        Renderer2D.stopScaling(matrixStack);
    }

    public void drawString(MatrixStack stack, String text, float x, float y, int color) {
        // Draw the text at the specified coordinates with the specified color
        try {
            this.drawText(stack, Text.literal(text).styled((it) -> it.withParent(Style.EMPTY.withColor(color))), x, y - 1, ColorUtils.getAlpha(color)/255.0F);
        } catch (NullPointerException ignored) {}
    }

    @Override
    protected Glyph locateGlyphFailsafe(char glyph, boolean bold, boolean italic) {
        Glyph original = this.locateGlyph0(glyph, bold, italic);
        if (original != null) {
            return original;
        }else{
            return pageManager.getOrCreateMap('?').getGlyph('?');
        }
    }

    @Override
    public float getStringWidth(String text) {
        return super.getTextWidth(Text.of(text));
    }
    @Override
    public float getStringHeight(String text) {
        return super.getTextHeight(Text.of(text));
    }

    public void drawCenteredString(MatrixStack stack, String text, float x, float y, int color) {
        try {
            this.drawCenteredText(stack, Text.literal(text).styled((it) -> it.withParent(Style.EMPTY.withColor(color))), x, y - 1, ColorUtils.getAlpha(color)/255.0F);
        } catch (NullPointerException ignored) {}
    }

}
