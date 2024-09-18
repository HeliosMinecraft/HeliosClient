package dev.heliosclient.util;

import dev.heliosclient.HeliosClient;
import net.minecraft.util.math.random.Random;

import java.awt.*;
import java.util.Objects;

/**
 * Utils for working with color and chat formatting.
 */
public class ColorUtils {
    static Random rand = Random.create();

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

    public static int argbToRgb(int argb) {
       return argbToRgb(argb,255);
    }
    public static int argbToRgb(int argb,int injectAlpha) {
        // Extract the individual components
        int red = ColorUtils.getRed(argb);
        int green = ColorUtils.getGreen(argb);
        int blue = ColorUtils.getBlue(argb);

        // Combine the RGB components into a single integer
        return rgbaToInt(red,green,blue,injectAlpha);
    }

    public static int colorToRGB(Color color) {
        int alpha = 255; // Fully opaque
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        // Combine the components into a single integer
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public static Color getRandomColor(){
        float r = rand.nextFloat(); // Red component
        float g = rand.nextFloat(); // Green component
        float b = rand.nextFloat(); // Blue component
        return new Color(r, g, b);
    }

    public static Color getRandomColorWithAlpha(){
        float r = rand.nextFloat(); // Red component
        float g = rand.nextFloat(); // Green component
        float b = rand.nextFloat(); // Blue component
        float a = rand.nextFloat(); // Alpha component

        return new Color(r, g, b,a);
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
        return getRainbowColor(1.0f,1.0f);
    }
    /**
     * Rainbow cycle with the provided brightness and saturation
     *
     * @return Current rainbow color.
     */
    public static Color getRainbowColor(float saturation, float brightness) {
        float hue = (System.currentTimeMillis() % (HeliosClient.CLICKGUI.getRainbowSpeed() * 1000)) / (HeliosClient.CLICKGUI.getRainbowSpeed() * 1000.0f);
        return Color.getHSBColor(hue, saturation, brightness);
    }

    /**
     * Rainbow cycle 2.
     *
     * @return Current rainbow color.
     */
    public static Color getRainbowColor2() {
        float hueOffset = 0.1f;
        float hue = (System.currentTimeMillis() % (HeliosClient.CLICKGUI.getRainbowSpeed() * 1000)) / (HeliosClient.CLICKGUI.getRainbowSpeed() * 1000.0f) + hueOffset;
        hue += (int) hue;
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
     * Changes alpha on integer color only if the alpha value is greater than the given float.
     *
     * @param color Target color.
     * @param alpha Target alpha.
     * @return Color with changed alpha.
     */
    public static Color changeAlpha(Integer color, int alpha, float greaterThanValue) {
        if (color != null && getAlpha(color) > greaterThanValue)
            return new Color(ColorUtils.getRed(color), ColorUtils.getGreen(color), ColorUtils.getBlue(color), alpha);
        else
            return new Color(Objects.requireNonNullElse(color, -1));
    }

    /**
     * Changes alpha on integer color only if the alpha value is less than the given float.
     *
     * @param color Target color.
     * @param changeToAlpha Target alpha.
     * @return Color with changed alpha.
     */
    public static Color changeAlpha(Integer color, float lessThanValue,int changeToAlpha) {
        if (color != null && getAlpha(color) < lessThanValue)
            return new Color(ColorUtils.getRed(color), ColorUtils.getGreen(color), ColorUtils.getBlue(color), changeToAlpha);
        else
            return new Color(Objects.requireNonNullElse(color, -1));
    }



    /**
     * Changes alpha on integer color.
     *
     * @param color Target color.
     * @param alpha Target alpha.
     * @return Color with changed alpha.
     */
    public static Color changeAlpha(Integer color, int alpha) {
        return changeAlpha(color, alpha, (float) -1);
    }
    /**
     * Changes alpha on integer color.
     *
     * @param color Target color.
     * @param alpha Target alpha.
     * @return integer color with changed alpha.
     */
    public static int changeAlphaGetInt(Integer color, int alpha) {
        return argbToRgb(color,alpha);
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

    /**
     * @param value Target value in hex.
     * @return Boolean value if the string is color encoded or not
     */
    public static boolean isHexColor(String value) {
        try {
            Color.decode(value);
            return true;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static float getSaturation(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return hsb[1];
    }

    public static float getBrightness(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return hsb[2];
    }

    public static String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static Color hexToColor(String hex) {
        return new Color(
                Integer.valueOf(hex.substring(1, 3), 16),
                Integer.valueOf(hex.substring(3, 5), 16),
                Integer.valueOf(hex.substring(5, 7), 16)
        );
    }
    public static Color hexToColor(String hex, int alpha) {
        return new Color(
                Integer.valueOf(hex.substring(1, 3), 16),
                Integer.valueOf(hex.substring(3, 5), 16),
                Integer.valueOf(hex.substring(5, 7), 16),
                alpha
        );
    }

    public static Color blend(Color color1, Color color2, float fraction) {
        int r = (int) (color1.getRed() * (1 - fraction) + color2.getRed() * fraction);
        int g = (int) (color1.getGreen() * (1 - fraction) + color2.getGreen() * fraction);
        int b = (int) (color1.getBlue() * (1 - fraction) + color2.getBlue() * fraction);
        return new Color(r, g, b);
    }

    public static int[] blend(int[] color1, int[] color2, float ratio) {
        int red = (int) (color1[0] * (1 - ratio) + color2[0] * ratio);
        int green = (int) (color1[1] * (1 - ratio) + color2[1] * ratio);
        int blue = (int) (color1[2] * (1 - ratio) + color2[2] * ratio);
        int alpha = (int) (color1[3] * (1 - ratio) + color2[3] * ratio);
        return new int[]{red, green, blue, alpha};
    }

    // The fraction parameter is used to adjust the hue of the colors over time, creating a constantly changing effect.
    public static Color[] getDaySkyColors(float fraction) {
        Color color1 = Color.getHSBColor(fraction, 0.6f, 1.0f); // Sky blue
        Color color2 = Color.getHSBColor(fraction, 0.2f, 1.0f); // Light yellow
        return new Color[]{color1, color2};
    }

    public static Color[] getEveningSkyColors(float fraction) {
        Color color1 = Color.getHSBColor(fraction, 0.5f, 0.8f); // Orange
        Color color2 = Color.getHSBColor(fraction, 0.0f, 0.5f); // Dark blue
        return new Color[]{color1, color2};
    }

    public static Color[] getNightSkyColors(float fraction) {
        Color color1 = Color.getHSBColor(fraction, 0.66f, 0.2f); // Dark blue
        Color color2 = Color.getHSBColor(fraction, 0.66f, 0.4f); // Lighter blue
        return new Color[]{color1, color2};
    }

    public static Color toColor(int[] color1) {
        return new Color(color1[0],color1[1],color1[2],color1[3]);
    }

}
