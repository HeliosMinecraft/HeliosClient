package dev.heliosclient.util.fontutils;

import java.awt.*;

public class FontUtils {

    public static Font[] rearrangeFontsArray(Font[] fonts, Font fontToMove) {
        // Find the index of the fontToMove in the array
        int index = -1;
        for (int i = 0; i < fonts.length; i++) {
            if (fonts[i].equals(fontToMove)) {
                index = i;
                break;
            }
        }

        // If the fontToMove is not found in the array, return the original array
        if (index == -1) {
            return fonts;
        }

        // Create a new array with the same length as the original array
        Font[] rearrangedFonts = new Font[fonts.length];

        // Place the fontToMove at index 0
        rearrangedFonts[0] = fontToMove;

        // Move all the fonts one position forward
        System.arraycopy(fonts, 0, rearrangedFonts, 1, index);
        System.arraycopy(fonts, index + 1, rearrangedFonts, index + 1, fonts.length - index - 1);

        return rearrangedFonts;
    }

}

