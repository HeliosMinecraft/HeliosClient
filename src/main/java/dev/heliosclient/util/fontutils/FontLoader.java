package dev.heliosclient.util.fontutils;

import net.minecraft.client.MinecraftClient;

import java.awt.*;
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
    private static final String ICON_FONTS_FOLDER = "heliosclient/fonts/icons";
    private static final String[] DEFAULT_FONT = {"Minecraft.ttf", "Comfortaa.ttf", "JetBrainsMono.ttf", "Nunito.ttf",};
    private static final String[] DEFAULT_ICON_FONT = {"icons2.ttf", "icons.ttf"};

    public static Font[] loadFonts() {
        // Get the Minecraft game directory
        File gameDir = MinecraftClient.getInstance().runDirectory;

        // Create the fonts directory if it doesn't exist
        File fontsDir = new File(gameDir, FONTS_FOLDER);
        if (!fontsDir.exists()) {
            fontsDir.mkdirs();
        }
        // Copy the default font file from the assets folder to the fonts directory
        for (String s : DEFAULT_FONT) {
            File defaultFontFile = new File(fontsDir, s);
            try (InputStream inputStream = FontLoader.class.getResourceAsStream("/assets/heliosclient/fonts/" + s)) {
                assert inputStream != null;
                Files.copy(inputStream, defaultFontFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
            }
        }
        // Load all font files from the fonts directory
        List<Font> fonts = new ArrayList<>();
        for (File file : Objects.requireNonNull(fontsDir.listFiles())) {
            if (file.isFile() && (file.getName().toLowerCase().endsWith(".ttf") || file.getName().toLowerCase().endsWith(".otf"))) {
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

    public static Font[] loadIconFonts() {
        File gameDir = MinecraftClient.getInstance().runDirectory;

        File fontsDir = new File(gameDir, ICON_FONTS_FOLDER);
        if (!fontsDir.exists()) {
            fontsDir.mkdirs();
        }

        for (String s : DEFAULT_ICON_FONT) {
            File defaultFontFile = new File(fontsDir, s);
            try (InputStream inputStream = FontLoader.class.getResourceAsStream("/assets/heliosclient/fonts/" + s)) {
                assert inputStream != null;
                Files.copy(inputStream, defaultFontFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
            }
        }

        List<Font> fonts = new ArrayList<>();
        for (File file : Objects.requireNonNull(fontsDir.listFiles())) {
            if (file.isFile() && (file.getName().toLowerCase().endsWith(".ttf") || file.getName().toLowerCase().endsWith(".otf"))) {
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
