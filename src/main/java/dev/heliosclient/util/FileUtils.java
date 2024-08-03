package dev.heliosclient.util;


import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.cape.GifDecoder;
import net.minecraft.client.texture.NativeImage;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
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

    public static LinkedList<NativeImage> readGifFrames(File gifFile) throws IOException {
        LinkedList<NativeImage> frames = new LinkedList<>();
        GifDecoder decoder = new GifDecoder();
        int status = decoder.read(gifFile.getAbsolutePath());
        if (status != GifDecoder.STATUS_OK) {
            throw new IOException("Failed to read GIF file: " + gifFile.getName());
        }

        for (int i = 0; i < decoder.getFrameCount(); i++) {
            NativeImage nativeImage = convertToNativeImage(decoder.getFrame(i));
            frames.add(nativeImage);
        }

        System.out.println("Successfully read " + frames.size() + " frames from GIF file: " + gifFile.getName());
        return frames;
    }
    private static NativeImage convertToNativeImage(BufferedImage image) {
        NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), false);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                nativeImage.setColor(x, y, argb);
            }
        }
        return nativeImage;
    }

}
