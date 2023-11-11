package dev.heliosclient.util;

import dev.heliosclient.module.sysmodules.ClickGUI;

import java.awt.*;
import java.util.Random;

/**
 * Utils for working with color and chat formatting.
 */
public class ColorUtils {

    //Aliases for chat formatting
    public static String colorChar = "\247";
    public static String black = "\2470";
    public static String darkBlue = "\2471";
    public static String darkGreen = "\2472";
    public static String darkAqua = "\2473";
    public static String darkRed = "\2474";
    public static String darkMagenta = "\2475";
    public static String gold = "\2476";
    public static String gray = "\2477";
    public static String darkGray = "\2478";
    public static String blue = "\2479";
    public static String green = "\247a";
    public static String aqua = "\247b";
    public static String red = "\247c";
    public static String magenta = "\247d";
    public static String yellow = "\247e";
    public static String white = "\247f";

    public static String underline = "\247n";
    public static String bold = "\247l";
    public static String italic = "\247o";
    public static String strikethrough = "\247m";
    public static String obfuscated = "\247k";
    public static String reset = "\247r";

    /**
     * Converts RGBA hex code to integer.
     *
     * @param r
     * @param g
     * @param b
     * @param a
     * @return Converted integer.
     */
    public static int rgbaToInt(int r, int g, int b, int a) {
        return ((a & 0x0ff) << 24) | ((r & 0x0ff) << 16) | ((g & 0x0ff) << 8) | (b & 0x0ff);
    }

    /**
     * Converts integer to RGB gex code.
     *
     * @param r
     * @param g
     * @param b
     * @return Converted code.
     */
    public static int rgbToInt(int r, int g, int b) {
        return ((r & 0x0ff) << 16) | ((g & 0x0ff) << 8) | (b & 0x0ff);
    }

    /**
     * Converts integer to color object.
     *
     * @param argb
     * @return Color converted to Color data type.
     */
    public static Color intToColor(int argb) {
        int alpha = (argb >> 24) & 0xFF;
        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue = argb & 0xFF;
        return new Color(red, green, blue, alpha);
    }

    /**
     * Rainbow color with custom speed.
     *
     * @param speed
     * @return Current rainbow color.
     */
    public static Color getRainbowColor(int speed) {
        float hue = (System.currentTimeMillis() % (speed * 100)) / (speed * 100.0f);
        return Color.getHSBColor(hue, 1.0f, 1.0f);
    }

    /**
     * Rainbow cycle.
     *
     * @return Current rainbow color.
     */
    public static Color getRainbowColor() {
        float hue = (System.currentTimeMillis() % (ClickGUI.RainbowSpeed.value.intValue() * 1000)) / (ClickGUI.RainbowSpeed.value.intValue() * 1000.0f);
        return Color.getHSBColor(hue, 1.0f, 1.0f);
    }

    /**
     * Changes alpha on color.
     *
     * @param color Target color.
     * @param alpha Target alpha.
     * @return Color with changed alpha.
     */
    public static Color changeAlpha(Color color, int alpha) {
        if (color != null)
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        else
            return new Color(0);
    }

    /**
     * @param color Target color.
     * @return Alpha of the color.
     */
    public static int getAlpha(int color) {
        return (color >> 24) & 0xFF;
    }

    /**
     * @param color Target color.
     * @return Red value of the color.
     */
    public static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }

    /**
     * @param color Target color.
     * @return Green value of the color.
     */
    public static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }

    /**
     * @param color Target color.
     * @return Blue value of the color.
     */
    public static int getBlue(int color) {
        return color & 0xFF;
    }

    public static Color getRandomColor() {
        Color[] colors = {Color.MAGENTA, Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.RED};
        int randomIndex = new Random().nextInt(colors.length);
        return colors[randomIndex];
    }
}
