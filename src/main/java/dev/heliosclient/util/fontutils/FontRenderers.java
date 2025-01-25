package dev.heliosclient.util.fontutils;

import me.x150.renderer.font.FontRenderer;

public class FontRenderers {
    /** This fontRenderer is used by the HUD mainly through the Renderer2D drawing string functions */
    public static FontRenderer fontRenderer;

    /**
     *  These font renderers are standard font renderer for the clickGUI, whose size is modified by the user
     *  (except the iconRenderer which is at 10f)
     */
    public static BetterFontRenderer fxfontRenderer;
    public static BetterFontRenderer iconRenderer;

    /**
     * This fontRenderer uses the DComicFont font for HitEffect Text mode
     * Size: 12f
     */
    public static BetterFontRenderer Comical_fxfontRenderer;


    // -- These fontRenderers have fixed size which cannot be changed. -- //

    /**
     * Size 4f
     */
    public static BetterFontRenderer Super_Small_iconRenderer;
    public static BetterFontRenderer Super_Small_fxfontRenderer;

    /**
     * Size 6f
     */
    public static BetterFontRenderer Small_iconRenderer;
    public static BetterFontRenderer Small_fxfontRenderer;

    /**
     * Size 8f
     */
    public static BetterFontRenderer Mid_iconRenderer;
    public static BetterFontRenderer Mid_fxfontRenderer;

    /**
     * Size 13f
     */
    public static BetterFontRenderer Large_iconRenderer;
    public static BetterFontRenderer Large_fxfontRenderer;

    /**
     * Size 25f
     */
    public static BetterFontRenderer Ultra_Large_iconRenderer;
}
