package dev.heliosclient.util;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.module.sysmodules.ClickGUI;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.util.GlAllocationUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.nio.ByteBuffer;

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
        float hue = (System.currentTimeMillis() % ((int) ClickGUI.RainbowSpeed.value * 1000)) / ((int) ClickGUI.RainbowSpeed.value * 1000.0f);
        return Color.getHSBColor(hue, 1.0f, 1.0f);
    }
    /**
     * Rainbow cycle 2.
     *
     * @return Current rainbow color.
     */
    public static Color getRainbowColor2() {
        float hue = (System.currentTimeMillis() % ((int) ClickGUI.RainbowSpeed.value * 1025)) / ((int) ClickGUI.RainbowSpeed.value * 1025.0f);
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

    /**
     * @param value Target value in hex.
     * @return Boolean value if the string is color encoded or not
     */
    public static boolean isHexColor(String value) {
        if (value.startsWith("#")) {
            value = value.substring(1);
        }
        if (value.length() != 6) {
            return false;
        }
        try {
            Color.decode("#" + value);
            return true;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Color getColorAtPixel(double mouseX, double mouseY) {
        Framebuffer framebuffer = HeliosClient.MC.getFramebuffer();
        int x = (int) (mouseX * framebuffer.textureWidth / HeliosClient.MC.getWindow().getScaledWidth());
        int y = (int) ((HeliosClient.MC.getWindow().getScaledHeight() - mouseY) * framebuffer.textureHeight / HeliosClient.MC.getWindow().getScaledHeight());

        ByteBuffer buffer = GlAllocationUtils.allocateByteBuffer(4);
        GL11.glReadPixels(x, y, 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        int red = buffer.get(0) & 0xFF;
        int green = buffer.get(1) & 0xFF;
        int blue = buffer.get(2) & 0xFF;
        int alpha = buffer.get(3) & 0xFF;

        return new Color(red, green, blue, alpha);
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

    public static Color blend(Color color1, Color color2, float fraction) {
        int r = (int) (color1.getRed() * (1 - fraction) + color2.getRed() * fraction);
        int g = (int) (color1.getGreen() * (1 - fraction) + color2.getGreen() * fraction);
        int b = (int) (color1.getBlue() * (1 - fraction) + color2.getBlue() * fraction);
        return new Color(r, g, b);
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
}
