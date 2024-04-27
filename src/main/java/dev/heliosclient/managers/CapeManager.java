package dev.heliosclient.managers;

import com.google.gson.*;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.module.modules.misc.CapeModule;
import dev.heliosclient.system.HeliosExecutor;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.animation.AnimationUtils;
import dev.heliosclient.util.cape.ProfileUtils;
import dev.heliosclient.util.fontutils.FontLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.http.client.HttpResponseException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CapeManager {
    public static CapeManager INSTANCE = new CapeManager();
    private static final File CAPE_DIRECTORY = new File(HeliosClient.MC.runDirectory, "heliosclient/capes");
    private static final String DEFAULT_CAPE = "helioscape.png";
    public static final Identifier DEFAULT_CAPE_TEXTURE = new Identifier("heliosclient", "capes/" + DEFAULT_CAPE);
    public static String[] capes = new String[]{};
    public static Identifier cape;

    private static final Set<String> registeredTextures = new HashSet<>();
    private static final Map<UUID, Identifier> CAPES = new HashMap<>();
    private static final Map<UUID, Identifier> ELYTRAS = new HashMap<>();

    public static List<Identifier> capeIdentifiers = new ArrayList<>();
    public static List<Identifier> elytraIdentifiers = new ArrayList<>();

    /**
     * Works similar to {@link FontLoader#loadFonts()}
     */
    public static String[] loadCapes() {
        Future<String[]> future = HeliosExecutor.submit(() -> {
        if (!CAPE_DIRECTORY.exists()) {
            HeliosClient.LOGGER.info("Cape directory does not exist");
            if (CAPE_DIRECTORY.mkdirs()) {
                HeliosClient.LOGGER.info("Cape directory created successfully");
            }
        }

        File defaultCapeFile = new File(CAPE_DIRECTORY, DEFAULT_CAPE);

        // Do not copy if the default cape file already exists
        if(!defaultCapeFile.exists()) {
            try (InputStream inputStream = FontLoader.class.getResourceAsStream("/assets/heliosclient/capes/helioscape.png")) {
                if (inputStream == null) {
                    HeliosClient.LOGGER.error("Failed to load open inputStream to default cape resource");
                } else {
                    HeliosClient.LOGGER.info("Copying default cape in directory");
                    Files.copy(inputStream, defaultCapeFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    HeliosClient.LOGGER.info("Copying completed");
                }
            } catch (IOException e) {
                HeliosClient.LOGGER.error("An error has occured while reading default resource asset cape", e);
            }
        }

        // Get the cape files from `heliosclient/capes` directory in an array
        File[] capeFiles = CAPE_DIRECTORY.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));

        if (capeFiles == null) {
            HeliosClient.LOGGER.info("No cape files found");
            return new String[0];
        }

        List<String> capeNames = new ArrayList<>();

        for (File file : capeFiles) {
            try(FileInputStream inputStream = new FileInputStream(file)) {
                String fileName = file.getName();
                String capeName = fileName.substring(0, fileName.lastIndexOf('.'));
                capeNames.add(capeName);

                loadCapeTexture(inputStream, fileName, file);
            } catch (IOException e) {
                HeliosClient.LOGGER.error("An error has occured while reading cape file: " + ColorUtils.darkGreen + file.getName(), e);
            }
        }
        capes = capeNames.toArray(new String[0]);
        return capes;
    });
        try {
            String[] result = future.get();
            CapeModule.get().capes.options = List.of(result);
            return result;
        } catch (InterruptedException | ExecutionException e) {
            HeliosClient.LOGGER.error("An error occurred while loading capes", e);
            return new String[0];
        }
    }

    public static void loadCapeTexture(InputStream inputStream, String fileName, File file) throws IOException {
        if (registeredTextures.contains(fileName)) {
            return; // Skip if texture is already registered
        }

        NativeImage image = NativeImage.read(inputStream);

        HeliosClient.MC.execute(() -> {
            TextureManager textureManager = HeliosClient.MC.getTextureManager();
            Identifier capeIdentifier;
            Identifier elytraIdentifier;
            capeIdentifier = textureManager.registerDynamicTexture("cape_" + fileName.toLowerCase(Locale.ROOT), new NativeImageBackedTexture(parseCape(image)));
            elytraIdentifier = textureManager.registerDynamicTexture("elytra_" + fileName.toLowerCase(Locale.ROOT), new NativeImageBackedTexture(image));


            capeIdentifiers.add(capeIdentifier);
            elytraIdentifiers.add(elytraIdentifier);

            registeredTextures.add(fileName);

            HeliosClient.LOGGER.info("Loaded cape: " + fileName);
        });
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

    public static void getCapes(CapeType type, String profileName, String UUID) throws Exception {
        Future<?> future = HeliosExecutor.submit((Callable<Void>) () -> {
            switch (type) {
                case OPTIFINE:
                    if (profileName == null || profileName.isEmpty()) {
                        throw new IllegalArgumentException("Profile name is required for OPTIFINE capes.");
                    }
                    // Get the optifine cape
                    INSTANCE.getOptifineCape(profileName);
                    break;
                case CRAFATAR:
                case MINECRAFTCAPES:
                    if (UUID == null || UUID.isEmpty()) {
                        throw new IllegalArgumentException("Complete and a valid UUID is required.");
                    }
                    if (!ProfileUtils.isValidUUID(UUID)) {
                        throw new IllegalArgumentException("Invalid UUID");
                    }
                    if (type == CapeType.CRAFATAR) {
                        // Get the craftar cape
                        INSTANCE.getCrafatarCape(UUID);
                    } else {
                        // Get the minecraft cape
                        INSTANCE.getMinecraftCapesCape(UUID);
                    }
                    break;
                case NONE:
                    return null;
                default:
                    throw new IllegalArgumentException("Invalid cape type: " + type);
            }
            loadCapes();
            return null;
        });
        future.get();
    }

    private static void saveCapeFromBase64(String UUID, String url) throws Exception {
        HttpURLConnection connection = INSTANCE.getConnection(url);
        connection.connect();

        if (connection.getResponseCode() / 100 == 2) {
            InputStreamReader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
            JsonObject result = new Gson().fromJson(reader, JsonObject.class);

            if (result == null) {
                throw new JsonParseException("Json result returned is null");
            }
            JsonElement jsonElement = result.getAsJsonObject("textures").get("cape");
            boolean animated = result.getAsJsonObject("animatedCape").getAsBoolean();

            if(animated){
                HeliosClient.LOGGER.error("Animated capes are not supported");
                AnimationUtils.addErrorToast("Animated capes are not supported", false, 1500);
                connection.disconnect();
                return;
            }

            if (jsonElement.isJsonNull()) {
                HeliosClient.LOGGER.error("UUID does not contain any capes: {} If you are sure that this UUID should contain a cape then try other service (i.e Craftar or Minecraft capes", UUID);
                AnimationUtils.addErrorToast("UUID does not contain any capes, Check Logs for details", true, 1500);
                connection.disconnect();
                throw new NullPointerException("UUID does not contain any capes");
            }

            String capeTexture = jsonElement.getAsString();
            byte[] imageBytes = Base64.getDecoder().decode(capeTexture);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));

            String profileName = ProfileUtils.getProfileName(UUID);
            File outputfile = new File(CAPE_DIRECTORY, profileName + ".png");
            ImageIO.write(image, "png", outputfile);
            connection.disconnect();
        } else {
            HeliosClient.LOGGER.error("Connection Message: " + connection.getResponseMessage() + ", Response Code: " + connection.getResponseCode());
            throw new HttpResponseException(connection.getResponseCode(),connection.getResponseMessage());
        }
    }

    private void getOptifineCape(String profileName) throws Exception {
        String url = "http://s.optifine.net/capes/" + profileName + ".png";
        saveCapeFromUrl(profileName, null, url);
    }

    private void getCrafatarCape(String UUID) throws Exception {
        String url = "https://crafatar.com/capes/" + UUID;
        saveCapeFromUrl(null, UUID, url);
    }

    private void getMinecraftCapesCape(String UUID) throws Exception {
        String url = "https://api.minecraftcapes.net/profile/" + UUID.replace("-", "");
        saveCapeFromBase64(UUID, url);
    }

    public HttpURLConnection getConnection(String url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection(MinecraftClient.getInstance().getNetworkProxy());
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        connection.setDoInput(true);
        connection.setDoOutput(false);
        return connection;
    }

    private void saveCapeFromUrl(String profileName, String UUID, String url) throws Exception {
        HttpURLConnection connection = INSTANCE.getConnection(url);
        connection.connect();

        if (connection.getResponseCode() / 100 == 2) {
            BufferedImage image = ImageIO.read(connection.getInputStream());
            if (profileName == null) {
                profileName = ProfileUtils.getProfileName(UUID);
            }
            File outputfile = new File(CAPE_DIRECTORY, profileName + ".png");
            ImageIO.write(image, "png", outputfile);
            connection.disconnect();
        } else {
            HeliosClient.LOGGER.error("Connection Message: " + connection.getResponseMessage() + ", Response Code: " + connection.getResponseCode());
            throw new HttpResponseException(connection.getResponseCode(),connection.getResponseMessage());
        }
    }
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

    public enum CapeType {
        NONE,
        OPTIFINE,
        CRAFATAR,
        MINECRAFTCAPES
    }
}
