package dev.heliosclient.managers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.module.modules.misc.CapeModule;
import dev.heliosclient.system.HeliosExecutor;
import dev.heliosclient.util.animation.AnimationUtils;
import dev.heliosclient.util.cape.CapeTextureManager;
import dev.heliosclient.util.cape.ProfileUtils;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.fontutils.FontLoader;
import dev.heliosclient.util.textures.Texture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CapeManager {
    //Default Elytra Skin
    private static final Identifier SKIN = new Identifier("textures/entity/elytra.png");

    //Where we store and get our capes.
    private static final File CAPE_DIRECTORY = new File(HeliosClient.SAVE_FOLDER, "/capes");
    private static final String DEFAULT_CAPE = "helioscape.png";
    
    //The default cape texture of heliosclient.
    public static final Texture DEFAULT_CAPE_TEXTURE = new Texture("capes/helioscape.png");

    public static CapeManager INSTANCE = new CapeManager();

    //A string of the names of the capes. Used in CapeModule
    public static String[] CAPE_NAMES = new String[]{};

    //List of all capes and elytra textures.
    private static final CapeTextureManager capeTextureManager = new CapeTextureManager();
    

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

            copyDefaultCapeTexture();

            // Get the cape files from `heliosclient/capes` directory in an array
            File[] capeFiles = CAPE_DIRECTORY.listFiles((dir, name) -> name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".gif"));

            if (capeFiles == null) {
                HeliosClient.LOGGER.info("No cape files found");
                return new String[0];
            }

            List<String> capeNames = new ArrayList<>();

            for (File file : capeFiles) {
                try (FileInputStream inputStream = new FileInputStream(file)) {
                    String fileName = file.getName();
                    capeNames.add(fileName);

                    capeTextureManager.registerCapeTextures(inputStream, file, fileName.toLowerCase().trim());
                    HeliosClient.LOGGER.info("Loaded cape: {}", file.getAbsolutePath());
                } catch (IOException e) {
                    HeliosClient.LOGGER.error("An error has occurred while reading cape file: {}{}", ColorUtils.darkGreen, file.getName(), e);
                }
            }
            CAPE_NAMES = capeNames.toArray(new String[0]);
            return CAPE_NAMES;
        });
        try {
            String[] result = future.get();

            //Sets the capes options in the CapeModule with the names of the existing capes, for the user to select.
            ModuleManager.get(CapeModule.class).capes.options = List.of(result);
            return result;
        } catch (InterruptedException | ExecutionException e) {
            HeliosClient.LOGGER.error("An error occurred while loading capes", e);
            return new String[0];
        }
    }

    public static void copyDefaultCapeTexture(){
        File defaultCapeFile = new File(CAPE_DIRECTORY, DEFAULT_CAPE);

        // Do not copy if the default cape file already exists
        if (!defaultCapeFile.exists()) {
            try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("assets/heliosclient/capes/helioscape.png")) {
                if (inputStream == null) {
                    HeliosClient.LOGGER.error("Failed to load open inputStream to default cape resource");
                } else {
                    HeliosClient.LOGGER.info("Copying default cape file in directory");
                    Files.copy(inputStream, defaultCapeFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    HeliosClient.LOGGER.info("Copying completed");
                }
            } catch (Throwable e) {
                HeliosClient.LOGGER.error("An error has occurred while trying to read default cape file", e);
            }
        }
    }

    /**
     * <a href="https://github.com/dragonostic/of-capes/blob/main/src/main/java/net/drago/ofcapes/util/PlayerHandler.java">Logic Credit</a>
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

    public static void getCapes(CapeOrigin type, String profileName, String UUID) throws Exception {
        Future<?> future = HeliosExecutor.submit((Callable<Void>) () -> {
            switch (type) {
                case OPTIFINE -> {
                    if (profileName == null || profileName.isEmpty()) {
                        throw new IllegalArgumentException("Profile name is required for OPTIFINE capes.");
                    }
                    // Get the optifine cape
                    INSTANCE.getOptifineCape(profileName);
                }
                case CRAFATAR, MINECRAFTCAPES -> {
                    if (UUID == null || UUID.isEmpty()) {
                        throw new IllegalArgumentException("Complete and a valid UUID is required.");
                    }
                    if (!ProfileUtils.isValidUUID(UUID)) {
                        throw new IllegalArgumentException("Invalid UUID");
                    }
                    if (type == CapeOrigin.CRAFATAR) {
                        // Get the craftar cape
                        INSTANCE.getCrafatarCape(UUID);
                    } else {
                        // Get the minecraft cape
                        INSTANCE.getMinecraftCapesCape(UUID);
                    }
                }
                case LOCAL -> {
                    return null;
                }
                default -> throw new IllegalArgumentException("Invalid cape type: " + type);
            }
            CAPE_NAMES = loadCapes();
            return null;
        });
        future.get();
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
            String responseMessage = connection.getResponseMessage();
            HeliosClient.LOGGER.error("Connection Message: {}, Response Code: {}", responseMessage, connection.getResponseCode());
            if(connection.getResponseCode() == 404) {
                responseMessage += " [Helios] Given player UUID may not contain a cape";
            }

            throw new HttpResponseException(connection.getResponseCode(), responseMessage);
        }
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
            boolean animated = result.getAsJsonPrimitive("animatedCape").getAsBoolean();

            if (animated) {
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
            HeliosClient.LOGGER.error("Connection Message: {}, Response Code: {}", connection.getResponseMessage(), connection.getResponseCode());
            throw new HttpResponseException(connection.getResponseCode(), connection.getResponseMessage());
        }
    }

    public static Identifier getCurrentCapeTexture() {
        if(HeliosClient.MC.player == null){
            return capeTextureManager.getCurrentTexture(HeliosClient.MC.getGameProfile().getId(),false);
        }
        return capeTextureManager.getCurrentTexture(HeliosClient.MC.player.getUuid(),false);
    }

    public static Identifier getCurrentElytraTexture() {
        if(HeliosClient.MC.player == null){
            return SKIN;
        }
        return capeTextureManager.getCurrentTexture(HeliosClient.MC.player.getUuid(),true);
    }

    public static CapeTextureManager getTextureManager(){
        return capeTextureManager;
    }

    public enum CapeOrigin {
        LOCAL,
        OPTIFINE,
        CRAFATAR,
        MINECRAFTCAPES
    }
}
