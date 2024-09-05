package dev.heliosclient.altmanager;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Using <a href="https://mojang-api-docs.gapple.pw/authentication/msa">Authentication Docs</a>
 * Untested
 */
public class XboxLiveAuthenticator {
    private static final String XBOX_LIVE_AUTH_URL = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String XSTS_AUTH_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String MINECRAFT_AUTH_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";

    public static Map<String, String> authenticateWithXboxLive(String accessToken) throws Exception {
        String authToken = authenticate(XBOX_LIVE_AUTH_URL, createXboxLiveAuthBody(accessToken));
        String xstsToken = authenticate(XSTS_AUTH_URL, createXstsAuthBody(authToken));
        return authenticateWithMinecraft(xstsToken);
    }

    private static String authenticate(String url, String jsonBody) throws Exception {
        URL authUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) authUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        conn.disconnect();

        HashMap response = new Gson().fromJson(content.toString(), HashMap.class);
        return (String) response.get("Token");
    }

    private static String createXboxLiveAuthBody(String accessToken) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("AuthMethod", "RPS");
        properties.put("SiteName", "user.auth.xboxlive.com");
        properties.put("RpsTicket", accessToken);

        Map<String, Object> body = new HashMap<>();
        body.put("Properties", properties);
        body.put("RelyingParty", "http://auth.xboxlive.com");
        body.put("TokenType", "JWT");

        return new Gson().toJson(body);
    }

    private static String createXstsAuthBody(String authToken) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("SandboxId", "RETAIL");
        properties.put("UserTokens", new String[]{authToken});

        Map<String, Object> body = new HashMap<>();
        body.put("Properties", properties);
        body.put("RelyingParty", "rp://api.minecraftservices.com/");
        body.put("TokenType", "JWT");

        return new Gson().toJson(body);
    }

    private static Map<String, String> authenticateWithMinecraft(String xstsToken) throws Exception {
        URL url = new URL(MINECRAFT_AUTH_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        String jsonBody = new Gson().toJson(Map.of(
                "identityToken", "XBL3.0 x=" + xstsToken,
                "ensureLegacyEnabled", true
        ));

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        conn.disconnect();

        Map<String, Object> response = new Gson().fromJson(content.toString(), HashMap.class);
        Map<String, String> result = new HashMap<>();
        result.put("access_token", (String) response.get("access_token"));
        result.put("username", (String) response.get("username"));

        return result;
    }
}
