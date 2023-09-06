package dev.heliosclient.util.fontutils;

import net.minecraft.client.MinecraftClient;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FontLoader {
    private static final String FONTS_FOLDER = "heliosclient/fonts";
    private static final String DEFAULT_FONT = "Comfortaa.ttf";

    public static Font[] loadFonts() {
        // Get the Minecraft game directory
        File gameDir = MinecraftClient.getInstance().runDirectory;

        // Create the fonts directory if it doesn't exist
        File fontsDir = new File(gameDir, FONTS_FOLDER);
        if (!fontsDir.exists()) {
            fontsDir.mkdirs();
        }
      // Copy the default font file from the assets folder to the fonts directory
        File defaultFontFile = new File(fontsDir, DEFAULT_FONT);
        try (InputStream inputStream = FontLoader.class.getResourceAsStream("/assets/heliosclient/" + DEFAULT_FONT)) {
            assert inputStream != null;
            Files.copy(inputStream, defaultFontFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load all font files from the fonts directory
        List<Font> fonts = new ArrayList<>();
        for (File file : Objects.requireNonNull(fontsDir.listFiles())) {
            if (file.isFile() && file.getName().endsWith(".ttf")) {
                try {
                    Font[] fontArray = Font.createFonts(file);
                    Collections.addAll(fonts, fontArray);
                } catch (FontFormatException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return fonts.toArray(new Font[0]);
    }
}
