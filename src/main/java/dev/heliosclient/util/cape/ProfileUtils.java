package dev.heliosclient.util.cape;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ProfileUtils {
    private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", Pattern.CASE_INSENSITIVE);

    public static boolean isValidUUID(String uuid) {
        return UUID_PATTERN.matcher(uuid).matches();
    }

    public static String getProfileName(String uuid) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(PROFILE_URL + uuid.replace("-", "")).openConnection();
        JsonObject response = new JsonParser().parse(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();
        return response.get("name").getAsString();
    }

    public static String getUUID(String profileName) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + profileName).openConnection();
        JsonObject response = new JsonParser().parse(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();
        return insertHyphensToUUID(response.get("id").getAsString());
    }

    private static String insertHyphensToUUID(String uuid) {
        return uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20, 32);
    }

}
