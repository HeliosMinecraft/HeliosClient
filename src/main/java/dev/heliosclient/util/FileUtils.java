package dev.heliosclient.util;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    public static boolean doesFileInPathExist(String FilePath) {
        Path path = Paths.get(FilePath);
        return Files.exists(path);
    }
}
