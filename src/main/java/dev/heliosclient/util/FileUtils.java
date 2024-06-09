package dev.heliosclient.util;


import dev.heliosclient.HeliosClient;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

public class FileUtils {
    public static final PointerBuffer filter = initFilter();

    public static PointerBuffer initFilter() {
        PointerBuffer temp = BufferUtils.createPointerBuffer(1);

        temp.put(MemoryUtil.memASCII("*.txt"));
        temp.rewind();

        return temp;
    }

    public static void openTinyFileDialog(String defaultFile, @Nullable Consumer<File> result, boolean allowMultipleSelects) {
        String filePath = TinyFileDialogs.tinyfd_openFileDialog(
                "Select a text File",
                new File(HeliosClient.SAVE_FOLDER, defaultFile).getAbsolutePath(),
                filter,
                null,
                allowMultipleSelects);

        if (filePath != null) {
            result.accept(new File(filePath));
        } else {
            result.accept(null);
        }
    }

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

    public static String[] readLines(String filePath) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            return lines.toArray(new String[0]);
        } catch (IOException e) {
            e.printStackTrace();
            return new String[0];
        }
    }

}
