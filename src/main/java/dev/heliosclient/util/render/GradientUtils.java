package dev.heliosclient.util.render;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GradientUtils {
    private final List<Color> colors = new ArrayList<>();

    // Creates a gradient from a string
    public static GradientUtils fromString(String s) {
        GradientUtils gradient = new GradientUtils();
        String[] parts = s.split(" ");
        for (String part : parts) {
            gradient.addColor(new Color(Integer.parseInt(part)));
        }
        return gradient;
    }

    // Adds a color to the gradient
    public void addColor(Color color) {
        colors.add(color);
    }

    // Returns the color at the specified position in the gradient
    public Color getColor(float position) {
        if (colors.isEmpty()) {
            throw new IllegalStateException("No colors in gradient");
        }
        if (position <= 0) {
            return colors.get(0);
        }
        if (position >= 1) {
            return colors.get(colors.size() - 1);
        }
        float scaledPosition = position * (colors.size() - 1);
        int index = (int) scaledPosition;
        float fraction = scaledPosition - index;
        return blend(colors.get(index), colors.get(index + 1), fraction);
    }

    // Blends two colors together
    private Color blend(Color color1, Color color2, float fraction) {
        int r = (int) (color1.getRed() * (1 - fraction) + color2.getRed() * fraction);
        int g = (int) (color1.getGreen() * (1 - fraction) + color2.getGreen() * fraction);
        int b = (int) (color1.getBlue() * (1 - fraction) + color2.getBlue() * fraction);
        return new Color(r, g, b);
    }

    // Converts the gradient to a string
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Color color : colors) {
            sb.append(color.getRGB()).append(" ");
        }
        return sb.toString().trim();
    }
}
