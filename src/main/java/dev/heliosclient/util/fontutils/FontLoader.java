package dev.heliosclient.util.fontutils;

import dev.heliosclient.HeliosClient;
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
    private static final String[] DEFAULT_FONT = {"Minecraft.ttf", "Comfortaa.ttf", "JetBrainsMono.ttf", "Nunito.ttf"};
    private static final String[] DEFAULT_ICON_FONT = {"fontello.ttf", "icons2.ttf", "icons.ttf"};

    /**
     * Loads all the font files (with {@code .ttf and .otf} extension) present in the {@link  #FONTS_FOLDER} directory
     *
     * @return An array of all the fonts created from the files
     */
    public static Font[] loadFonts() {
        return loadFonts(FONTS_FOLDER, DEFAULT_FONT);
    }

    /**
     * Loads all the icon font files (with {@code .ttf and .otf} extension) present in the {@link  #ICON_FONTS_FOLDER} directory
     *
     * @return An array of all the fonts created from the files
     */
    public static Font[] loadIconFonts() {
        return loadFonts(ICON_FONTS_FOLDER, DEFAULT_ICON_FONT);
    }

    /**
     * Loads all the font files (with {@code .ttf and .otf} present in the given folder
     *
     * @param folder       Folder where the fonts are to be searched in
     * @param defaultFonts Default font files in the assets folder to be copied.
     * @return An array of all the fonts created from the files
     */
    private static Font[] loadFonts(String folder, String[] defaultFonts) {
        File gameDir = MinecraftClient.getInstance().runDirectory;
        File fontsDir = new File(gameDir, folder);
        if (!fontsDir.exists()) {
            fontsDir.mkdirs();
        }

        for (String s : defaultFonts) {
            File defaultFontFile = new File(fontsDir, s);
            try (InputStream inputStream = FontLoader.class.getResourceAsStream("/assets/" + FONTS_FOLDER + "/" + s)) {
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
                    HeliosClient.LOGGER.error("An error has occured while converting file to font format", e);
                }
            }
        }

        return fonts.toArray(new Font[0]);
    }

}
