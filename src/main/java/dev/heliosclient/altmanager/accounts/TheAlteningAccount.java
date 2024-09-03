package dev.heliosclient.altmanager.accounts;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.altmanager.Account;
import dev.heliosclient.mixin.AccessorMinecraftClient;
import dev.heliosclient.util.cape.ProfileUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

public class TheAlteningAccount extends Account {
    static final String ALTENING_AUTH = "http://authserver.thealtening.com";

    private String alteningToken = null;

    public TheAlteningAccount(String alteningToken) {
        super("",null);
        this.alteningToken = alteningToken;
    }

    @Override
    public boolean login() {
        try {
            Session session = YggdrasilUserAuthentication.doAuthenticate(alteningToken, "HeliosClient");
            ((AccessorMinecraftClient) MinecraftClient.getInstance()).setSession(session);
            setUsername(session.getUsername());

        }catch (Exception e){
            HeliosClient.LOGGER.error("Error while logging in TheAltening",e);
            return false;
        }
        return false;
    }

    @Override
    public String getDisplayName() {
        return getUsername();
    }

    //Original Authors LiquidBounce mc-authLib in kotlin
    //Similar Wiki for the authserver:  https://wiki.vg/Legacy_Mojang_Authentication
    public static class YggdrasilUserAuthentication {
        private static final String clientIdentifier = UUID.randomUUID().toString();

        public enum Agent {
            @SerializedName("name")
            MINECRAFT("Minecraft", 1);

            private final String agentName;
            private final int version;

            Agent(String agentName, int version) {
                this.agentName = agentName;
                this.version = version;
            }

            public String getAgentName() {
                return agentName;
            }

            public int getVersion() {
                return version;
            }
        }
        public static class AuthenticationRequest {
            private final Agent agent;
            private final String username;
            private final String password;
            //ClientToken is actually optional and it can work without it but just to be safe.
            private final String clientToken;

            //Request user is false by default (and optional) but we want true.
            private final boolean requestUser;

            public AuthenticationRequest(String username, String password) {
                this.agent = Agent.MINECRAFT;
                this.username = username;
                this.password = password;
                this.clientToken = clientIdentifier;
                this.requestUser = true;
            }
        }
        public static Session doAuthenticate(String username, String password) throws Exception {
            if (username == null || username.isBlank()) {
                throw new IllegalArgumentException("Username cannot be blank");
            }

            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("Password cannot be blank");
            }

            // Create the authentication request body
            AuthenticationRequest authenticationRequest = new AuthenticationRequest(username,password);
            String jsonBody = new Gson().toJson(authenticationRequest);

            // Send the authentication request
            HttpURLConnection conn = getHttpURLConnection(jsonBody);

            InputStream stream;
            if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 299){
                stream = conn.getInputStream();
            }else{
                stream = conn.getErrorStream();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(stream));
            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            conn.disconnect();

            // Parse the authentication response
            AuthenticationResponse response = new Gson().fromJson(content.toString(), AuthenticationResponse.class);

            if (!response.clientToken.equals(clientIdentifier)) {
                throw new IllegalStateException("Client identifier mismatch");
            }

            AuthenticationResponse.Profile selectedProfile = response.selectedProfile;

            if (selectedProfile == null || response.availableProfiles.length == 0) {
                throw new IllegalStateException("Minecraft account not purchased");
            }

            String profileName = selectedProfile.name;
            UUID profileUUID = UUID.fromString(ProfileUtils.insertHyphensToUUID(selectedProfile.id));
            String accessToken = response.accessToken;

            return new Session(profileName, profileUUID, accessToken, Optional.empty(),Optional.empty(), Session.AccountType.MOJANG);
        }

        private static @NotNull HttpURLConnection getHttpURLConnection(String jsonBody) throws IOException {
            URL url = new URL(ALTENING_AUTH + "/authenticate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36");
            conn.setDoOutput(true);
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(10000);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            return conn;
        }

        private record AuthenticationResponse(String accessToken, String clientToken,
                                              YggdrasilUserAuthentication.AuthenticationResponse.Profile[] availableProfiles,
                                              YggdrasilUserAuthentication.AuthenticationResponse.Profile selectedProfile) {

            private record Profile(String id, String name) {
            }
        }
    }

}
