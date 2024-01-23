package dev.heliosclient.util.render;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class GifTexture extends NativeImageBackedTexture {
    private final File gifFile;
    private final BufferedImage[] frames;
    private int currentFrame;

    public GifTexture(File gifFile) throws IOException {
        super(readGif(gifFile));
        this.gifFile = gifFile;
        this.frames = readFrames(gifFile);
        this.currentFrame = 0;
    }

    private static NativeImage readGif(File gifFile) throws IOException {
        try (ImageInputStream stream = ImageIO.createImageInputStream(gifFile)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                reader.setInput(stream);
                BufferedImage image = reader.read(0); // Read the first frame
                NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), false);
                for (int y = 0; y < image.getHeight(); y++) {
                    for (int x = 0; x < image.getWidth(); x++) {
                        nativeImage.setColor(x, y, image.getRGB(x, y));
                    }
                }
                return nativeImage;
            } else {
                throw new IOException("No suitable ImageReader found for GIF file.");
            }
        }
    }


    private static BufferedImage[] readFrames(File gifFile) throws IOException {
        try (ImageInputStream stream = ImageIO.createImageInputStream(gifFile)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                reader.setInput(stream);
                BufferedImage[] frames = new BufferedImage[reader.getNumImages(true)];
                for (int i = 0; i < frames.length; i++) {
                    frames[i] = reader.read(i);
                }
                return frames;
            } else {
                throw new IOException("No suitable ImageReader found for GIF file.");
            }
        }
    }

    @Override
    public void upload() {
        if (frames.length > 0) {
            BufferedImage image = frames[currentFrame];
            NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), false);
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    nativeImage.setColor(x, y, image.getRGB(x, y));
                }
            }
            setImage(nativeImage);
            super.upload();
            currentFrame = (currentFrame + 1) % frames.length;
        }
    }
}
