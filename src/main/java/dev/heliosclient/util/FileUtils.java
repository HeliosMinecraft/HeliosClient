package dev.heliosclient.util;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {
    public static boolean doesSvgIconExist(String iconName) {
        Path path = Paths.get("assets/svgIcons/" + iconName + ".svg");
        return Files.exists(path);
    }

    public static boolean doesFileInPathExist(String FilePath) {
        Path path = Paths.get(FilePath);
        return Files.exists(path);
    }
}
