package dev.heliosclient.managers;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.render.GifTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;

public class CapeManager {
    private static final File CAPE_DIRECTORY = new File(HeliosClient.MC.runDirectory, "heliosclient/capes");
    public static String[] capes = new String[]{};
    public static Identifier cape;
    public static List<Identifier> capeIdentifiers = new ArrayList<>();
    public static List<Identifier> elytraIdentifiers = new ArrayList<>();

    private static final String DEFAULT_CAPE = "helioscape.png";
    public static final Identifier CAPE_TEXTURE = new Identifier("heliosclient", "capes/" + DEFAULT_CAPE);
    public static final Identifier DEFAULT_CAPE_TEXTURE = new Identifier("heliosclient", "capes/" + DEFAULT_CAPE);


    /**
     * Works similar to FontLoader#loadFonts
     */
    public static String[] loadCapes() {
        if (!CAPE_DIRECTORY.exists()) {

            HeliosClient.LOGGER.info("Cape directory does not exist");
            CAPE_DIRECTORY.mkdirs();
        }

        File defaultCapeFile = new File(CAPE_DIRECTORY, DEFAULT_CAPE);
        try (InputStream inputStream = CapeManager.class.getResourceAsStream("/assets/heliosclient/capes/" + DEFAULT_CAPE)) {
            assert inputStream != null;
            Files.copy(inputStream, defaultCapeFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            HeliosClient.LOGGER.info("Copying default cape in directory");
        } catch (IOException e) {
            e.printStackTrace();
        }

        File[] capeFiles = CAPE_DIRECTORY.listFiles((dir, name) -> name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".gif"));

        if (capeFiles == null) {
            HeliosClient.LOGGER.info("No cape files found");
            return new String[0];
        }

        capeIdentifiers.clear();
        elytraIdentifiers.clear();

        List<String> capeNames = new ArrayList<>();

        for (File file : capeFiles) {
            try {
                FileInputStream inputStream = new FileInputStream(file);
                String fileName = file.getName();
                String capeName = fileName.substring(0, fileName.lastIndexOf('.'));
                capeNames.add(capeName);

                MinecraftClient.getInstance().execute(() -> {
                    try {
                        TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
                        Identifier capeIdentifier;
                        Identifier elytraIdentifier;
                        if (fileName.endsWith(".gif")) {
                            capeIdentifier = textureManager.registerDynamicTexture("cape_" + fileName, new GifTexture(file));
                            elytraIdentifier = capeIdentifier;
                        } else {
                            NativeImage image = NativeImage.read(inputStream);
                            capeIdentifier = textureManager.registerDynamicTexture("cape_" + fileName, new NativeImageBackedTexture(parseCape(image)));
                            elytraIdentifier = textureManager.registerDynamicTexture("elytra_" + fileName, new NativeImageBackedTexture(image));
                        }
                        elytraIdentifiers.add(elytraIdentifier);
                        capeIdentifiers.add(capeIdentifier);
                        HeliosClient.LOGGER.info("Loaded cape: " + fileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String[] capeNamesArray =  capeNames.toArray(new String[0]);
        capes = capeNamesArray;
        return capeNamesArray;
    }

    /**
     * <a href="https://github.com/dragonostic/of-capes/blob/main/src/main/java/net/drago/ofcapes/util/PlayerHandler.java">Credit</a>
     *
     * @param image Native image to be parsed
     * @return parsed image
     */
    public static NativeImage parseCape(NativeImage image) {
        int imageWidth = 64;
        int imageHeight = 32;
        int imageSrcWidth = image.getWidth();
        int srcHeight = image.getHeight();

        for (int imageSrcHeight = image.getHeight(); imageWidth < imageSrcWidth
                || imageHeight < imageSrcHeight; imageHeight *= 2) {
            imageWidth *= 2;
        }

        NativeImage imgNew = new NativeImage(imageWidth, imageHeight, true);
        for (int x = 0; x < imageSrcWidth; x++) {
            for (int y = 0; y < srcHeight; y++) {
                imgNew.setColor(x, y, image.getColor(x, y));
            }
        }
        return imgNew;
    }




    private static byte[] toByteArray(BufferedImage bufferedImage, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, format, baos);
        return baos.toByteArray();
    }


    public static void loadCape(PlayerEntity player, Identifier capeTexture) {
        if(HeliosClient.MC.getResourceManager() != null) {
            try (InputStream stream = MinecraftClient.getInstance().getResourceManager().getResource(capeTexture).orElseThrow().getInputStream()) {
                NativeImage image = NativeImage.read(stream);
                NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
                MinecraftClient.getInstance().getTextureManager().registerTexture(capeTexture, texture);
                CapeManager.setCapeAndElytra(player, capeTexture,capeTexture);
            } catch (IOException e) {
                e.printStackTrace();
                // If the cape texture fails to load, set the player's cape to the default cape
                CapeManager.setCapeAndElytra(player, DEFAULT_CAPE_TEXTURE,DEFAULT_CAPE_TEXTURE);
            }
        }
    }

    public static void loadCape() {
        if(HeliosClient.MC.getResourceManager() != null) {
            try (InputStream stream = MinecraftClient.getInstance().getResourceManager().getResource(CAPE_TEXTURE).orElseThrow().getInputStream()) {
                NativeImage image = NativeImage.read(stream);
                NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
                MinecraftClient.getInstance().getTextureManager().registerTexture(CAPE_TEXTURE, texture);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Identifier getCape(PlayerEntity player) {
        // Check if the player has a custom cape in the cape directory
        File capeFile = new File(CAPE_DIRECTORY, player.getUuidAsString() + ".png");
        if (capeFile.exists()) {
            // If they do, load that cape texture
            return capeIdentifiers.get(0); // TODO: Fix with correct implementation.
        }

        // Otherwise, return a default cape texture or null
        return null;
    }

    private static final Map<UUID, Identifier> CAPES = new HashMap<>();
    private static final Map<UUID, Identifier> ELYTRAS = new HashMap<>();


    public static boolean shouldPlayerHaveCape(PlayerEntity player) {
        return CAPES.containsKey(player.getUuid()) || ELYTRAS.containsKey(player.getUuid());
    }

    public static Identifier getCapeTexture(PlayerEntity player) {
        return CAPES.get(player.getUuid());
    }
    public static Identifier getElytraTexture(PlayerEntity player) {
        return ELYTRAS.get(player.getUuid());
    }

    public static void setCapeAndElytra(PlayerEntity player, Identifier texture, Identifier elytraTexture) {
        CAPES.put(player.getUuid(), texture);
        ELYTRAS.put(player.getUuid(), elytraTexture);
    }
    public static void setElytra(PlayerEntity player, Identifier texture) {
        ELYTRAS.put(player.getUuid(), texture); // New method for elytra textures
    }
}
