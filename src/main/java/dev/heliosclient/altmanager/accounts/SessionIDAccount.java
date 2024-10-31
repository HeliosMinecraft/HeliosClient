package dev.heliosclient.altmanager.accounts;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import dev.heliosclient.altmanager.Account;
import dev.heliosclient.mixin.AccessorMinecraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

public class SessionIDAccount extends Account {
    private String sessionID;
    private GameProfile gameProfile;

    public SessionIDAccount(String sessionID ) {
        super("",null);
        this.sessionID = sessionID;
    }

    @Override
    public boolean login() {
        if(this.sessionID != null && !this.sessionID.isEmpty()){
            gameProfile = fetchProfileFromSession(sessionID);
            Session session = new Session(gameProfile.getName(), gameProfile.getId(),sessionID, Optional.empty(),Optional.empty(), Session.AccountType.MOJANG);
            ((AccessorMinecraftClient) MinecraftClient.getInstance()).setSession(session);
        }

        return false;
    }

    public GameProfile fetchProfileFromSession(String token) {
        try {
            // Setup HTTP connection
            URL url = new URL("https://api.minecraftservices.com/minecraft/profile");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + token);

            // Get response
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new IllegalStateException("Failed to get profile by session, received response code is: " + responseCode);
            }

            // Read response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parse JSON response
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            String id = jsonResponse.get("id").getAsString();
            String name = jsonResponse.get("name").getAsString();

            // Format UUID
            UUID uuid = UUID.fromString(id.replaceFirst(
                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                    "$1-$2-$3-$4-$5"
            ));

            return new GameProfile(uuid, name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getDisplayName() {
        return this.gameProfile == null ? fetchProfileFromSession(sessionID).getName() : this.gameProfile.getName();
    }
}
