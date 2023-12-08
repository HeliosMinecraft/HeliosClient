package dev.heliosclient.util;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    public static boolean doesFileInPathExist(String FilePath) {
        Path path = Paths.get(FilePath);
        return Files.exists(path);
    }
}
