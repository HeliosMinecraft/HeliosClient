package dev.heliosclient.util.render;

import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Kernel;

// https://github.com/Pan4ur/ThunderHack-Recode/blob/main/src/main/java/thunder/hack/utility/render/GaussianFilter.java
public class GaussianBlur {
    private final float blurRadius;
    private final Kernel blurKernel;

    public GaussianBlur(float blurRadius) {
        this.blurRadius = blurRadius;
        this.blurKernel = generateKernel(blurRadius);
    }

    private static Kernel generateKernel(float radius) {
        int size = (int) Math.ceil(radius) * 2 + 1;
        float[] weights = new float[size];
        float sigma = radius / 3.0F;
        float sigma22 = 2.0F * sigma * sigma;
        float sigmaPi2 = 2.0F * (float) Math.PI * sigma;
        float sqrtSigmaPi2 = (float) Math.sqrt(sigmaPi2);
        float radius2 = radius * radius;
        float total = 0.0F;
        int index = 0;
        for (int row = -size / 2; row <= size / 2; row++) {
            float distance = row * row;
            if (distance > radius2) {
                weights[index] = 0.0F;
            } else {
                weights[index] = (float) Math.exp(-distance / sigma22) / sqrtSigmaPi2;
            }
            total += weights[index];
            index++;
        }
        for (int i = 0; i < size; i++) {
            weights[i] /= total;
        }
        return new Kernel(size, 1, weights);
    }

    public static void convolveAndTranspose(@NotNull Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha, boolean premultiply, boolean unpremultiply, int edgeAction) {
        float[] matrix = kernel.getKernelData(null);
        int cols = kernel.getWidth();
        int cols2 = cols / 2;
        for (int y = 0; y < height; y++) {
            int index = y;
            int ioffset = y * width;
            for (int x = 0; x < width; x++) {
                float r = 0.0F, g = 0.0F, b = 0.0F, a = 0.0F;
                int moffset = cols2;
                for (int col = -cols2; col <= cols2; col++) {
                    float f = matrix[moffset + col];
                    if (f != 0.0F) {
                        int ix = x + col;
                        if (ix < 0) {
                            if (edgeAction == 1) {
                                ix = 0;
                            } else if (edgeAction == 2) {
                                ix = (x + width) % width;
                            }

                        } else if (ix >= width) {
                            if (edgeAction == 1) {
                                ix = width - 1;
                            } else if (edgeAction == 2) {
                                ix = (x + width) % width;
                            }
                        }

                        int rgb = inPixels[ioffset + ix];
                        int pa = rgb >> 24 & 0xFF;
                        int pr = rgb >> 16 & 0xFF;
                        int pg = rgb >> 8 & 0xFF;
                        int pb = rgb & 0xFF;

                        if (premultiply) {
                            float a255 = pa * 0.003921569F;
                            pr = (int) (pr * a255);
                            pg = (int) (pg * a255);
                            pb = (int) (pb * a255);
                        }

                        a += f * pa;
                        r += f * pr;
                        g += f * pg;
                        b += f * pb;
                    }
                }

                if (unpremultiply && a != 0.0F && a != 255.0F) {
                    float f = 255.0F / a;
                    r *= f;
                    g *= f;
                    b *= f;
                }

                int ia = alpha ? clamp((int) (a + 0.5D)) : 255;
                int ir = clamp((int) (r + 0.5D));
                int ig = clamp((int) (g + 0.5D));
                int ib = clamp((int) (b + 0.5D));
                outPixels[index] = ia << 24 | ir << 16 | ig << 8 | ib;
                index += height;
            }
        }
    }

    public static int clamp(int c) {
        if (c < 0) return 0;
        return Math.min(c, 255);
    }

    public BufferedImage applyFilter(BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();
        if (dst == null) {
            dst = createDestImage(src, null);
        }
        int[] srcPixels = new int[width * height];
        int[] dstPixels = new int[width * height];
        src.getRGB(0, 0, width, height, srcPixels, 0, width);
        if (this.blurRadius > 0.0F) {
            convolveAndTranspose(this.blurKernel, srcPixels, dstPixels, width, height, true, true, false, 1);
            convolveAndTranspose(this.blurKernel, dstPixels, srcPixels, height, width, true, false, true, 1);
        }
        dst.setRGB(0, 0, width, height, srcPixels, 0, width);
        return dst;
    }

    public BufferedImage createDestImage(BufferedImage src, ColorModel dstCM) {
        if (dstCM == null) {
            dstCM = src.getColorModel();
        }
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), dstCM.isAlphaPremultiplied(), null);
    }

}
