package dev.heliosclient.util.fontutils;

import me.x150.renderer.font.FontRenderer;

public class FontRenderers {
    /** This fontRenderer is used by the HUD mainly through the Renderer2D drawing string functions */
    public static FontRenderer fontRenderer;

    /**
     *  These font renderers are standard font renderer for the clickGUI, whose size is modified by the user
     *  (except the iconRenderer which is at 10f)
     */
    public static fxFontRenderer fxfontRenderer;
    public static fxFontRenderer iconRenderer;

    /**
     * This fontRenderer uses the DComicFont font for HitEffect Text mode
     * Size: 12f
     */
    public static fxFontRenderer Comical_fxfontRenderer;


    // -- These fontRenderers have fixed size which cannot be changed. -- //

    /**
     * Size 4f
     */
    public static fxFontRenderer Super_Small_iconRenderer;
    public static fxFontRenderer Super_Small_fxfontRenderer;

    /**
     * Size 6f
     */
    public static fxFontRenderer Small_iconRenderer;
    public static fxFontRenderer Small_fxfontRenderer;

    /**
     * Size 8f
     */
    public static fxFontRenderer Mid_iconRenderer;
    public static fxFontRenderer Mid_fxfontRenderer;

    /**
     * Size 13f
     */
    public static fxFontRenderer Large_iconRenderer;
    public static fxFontRenderer Large_fxfontRenderer;

    /**
     * Size 25f
     */
    public static fxFontRenderer Ultra_Large_iconRenderer;
}
