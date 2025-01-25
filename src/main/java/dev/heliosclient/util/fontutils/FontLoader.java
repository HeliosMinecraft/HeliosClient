package dev.heliosclient.util.fontutils;

import dev.heliosclient.HeliosClient;
import net.minecraft.client.MinecraftClient;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class FontLoader {
    private static final String FONTS_FOLDER = "heliosclient/fonts";
    private static final String ICON_FONTS_FOLDER = "heliosclient/fonts/icons";
    private static final String[] DEFAULT_FONTS = {"Minecraftia.ttf", "Comfortaa.ttf", "JetBrainsMono.ttf", "Nunito.ttf","DComicFont.ttf"};
    private static final String[] DEFAULT_ICON_FONTS = {"fontello.ttf", "icons2.ttf", "icons.ttf"};
    public static Font[] COMICALFONTS = null;
    private static final String COMICAL_FONT_NAME = "DComicFont.ttf";

    /**
     * Loads all the font files (with {@code .ttf and .otf} extension) present in the {@link  #FONTS_FOLDER} directory
     *
     * @return An array of all the fonts created from the files
     */
    public static List<Font> getFonts() {
        return getFonts(FONTS_FOLDER, DEFAULT_FONTS);
    }

    /**
     * Loads all the icon font files (with {@code .ttf and .otf} extension) present in the {@link  #ICON_FONTS_FOLDER} directory
     *
     * @return An array of all the fonts created from the files
     */
    public static List<Font> getIconFonts() {
        return getFonts(ICON_FONTS_FOLDER, DEFAULT_ICON_FONTS);
    }

    /**
     * Loads all the font files (with {@code .ttf and .otf} present in the given folder
     *
     * @param folder       Folder where the fonts are to be searched in
     * @param defaultFonts Default font files in the assets folder to be copied.
     * @return An array of all the fonts created from the files
     */
    private static List<Font> getFonts(String folder, String[] defaultFonts) {
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
            } catch (IOException ignored) {}
        }

        LinkedList<Font> fonts = new LinkedList<>();
        for (File file : Objects.requireNonNull(fontsDir.listFiles())) {
            String fileName = file.getName().toLowerCase();
            if (file.isFile() && (fileName.endsWith(".ttf") || fileName.endsWith(".otf"))) {
                try {
                    Font[] fontArray = Font.createFonts(file);

                    if(file.getName().equalsIgnoreCase(COMICAL_FONT_NAME)){
                        COMICALFONTS = fontArray;
                        
                        //Dont add
                        continue;
                    }

                    Collections.addAll(fonts, fontArray);
                } catch (FontFormatException | IOException e) {
                    HeliosClient.LOGGER.error("An error has occurred while converting file to font format", e);
                }
            }
        }

        return fonts;
    }
}
