package dev.heliosclient.util;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {
    public static boolean doesFileInPathExist(String FilePath) {
        Path path = Paths.get(FilePath);
        return Files.exists(path);
    }
    /**
     * Checks if the given file is empty.
     *
     * @param file The file to check.
     * @return True if the file is empty, else false.
     */
    public static boolean isFileEmpty(File file) {
        return file.length() == 0;
    }

}
