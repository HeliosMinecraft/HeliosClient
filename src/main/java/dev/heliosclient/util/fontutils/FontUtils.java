package dev.heliosclient.util.fontutils;

import java.awt.Font;

public class FontUtils {
    public static void rearrangeFonts(Font[] fonts, Font fontToMove) {
        // Find the index of the fontToMove in the array
        int index = -1;
        for (int i = 0; i < fonts.length; i++) {
            if (fonts[i].equals(fontToMove)) {
                index = i;
                break;
            }
        }

        // If the fontToMove is not found in the array, return
        if (index == -1) {
            return;
        }

        // Move all the fonts one position forward
        for (int i = index; i > 0; i--) {
            fonts[i] = fonts[i - 1];
        }

        // Place the fontToMove at index 0
        fonts[0] = fontToMove;
    }
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

        // Move all the fonts one position forward
        System.arraycopy(fonts, 0, rearrangedFonts, 1, index);

        // Place the fontToMove at index 0
        rearrangedFonts[0] = fontToMove;

        // Move the remaining fonts
        if (fonts.length - (index + 1) >= 0)
            System.arraycopy(fonts, index + 1 - 1, rearrangedFonts, index + 1, fonts.length - (index + 1));

        return rearrangedFonts;
    }
}

