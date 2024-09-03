package dev.heliosclient.altmanager;

import dev.heliosclient.HeliosClient;
import it.unimi.dsi.fastutil.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Using <a href="https://mojang-api-docs.gapple.pw/authentication/msa">Authentication Docs</a>
 */
public class MicrosoftAuthExtractor {
    private static final Pattern PPFT_REGEX = Pattern.compile("sFTTag:[ ]?'.*value=\"([^\"]+)\"/>");
    private static final Pattern URLPOST_REGEX = Pattern.compile("urlPost:[ ]?'([^']+)'");

    /**
     *
     * @return pair, Left is sFTTag and right is urlPost
     */
    public static Pair<String, String> getsFTTagAndUrlPost() {
        try {
            String loginUrl = "https://login.live.com/oauth20_authorize.srf?client_id=000000004C12AE6F&redirect_uri=https://login.live.com/oauth20_desktop.srf&scope=service::user.auth.xboxlive.com::MBI_SSL&display=touch&response_type=token&locale=en";
            String htmlContent = getHtmlContent(loginUrl);

            String ppft = extractValue(htmlContent, PPFT_REGEX);
            String urlPost = extractValue(htmlContent, URLPOST_REGEX);

            if(ppft == null || ppft.isEmpty()){
                throw new Exception("sFTTag is null or empty! This should not happen");
            }
            if(urlPost == null || urlPost.isEmpty()){
                throw new Exception("urlPost is null or empty! This should not happen");
            }

            return Pair.of(ppft,urlPost);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String getHtmlContent(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        conn.disconnect();

        return content.toString();
    }

    private static String extractValue(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static String getAccessToken(String email, String pass, Pair<String,String> ppftAndUrlPost){
        try {
            HttpURLConnection conn = doPostAndGet(ppftAndUrlPost.right());

            String urlParameters = "login=" + email + "&passwd=" + pass + "&PPFT=" + ppftAndUrlPost.left();
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = urlParameters.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Step 3: Handle response and extract accessToken
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            conn.disconnect();

            String response = content.toString();

            if (response.contains("Sign in to")) {
                HeliosClient.LOGGER.error("Incorrect credentials.");
                return null;
            } else if (response.contains("Help us protect your account")) {
                HeliosClient.LOGGER.error("2-factor authentication is enabled. Please disable it or (somehow) enter your security information.");
                return null;
            }

            // Extract tokens from the URL
            String[] urlParts = conn.getURL().toString().split("#");
            if (urlParts.length > 1) {
                String[] params = urlParts[1].split("&");
                Map<String, String> loginData = new HashMap<>();
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        loginData.put(keyValue[0], java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
                    }
                }

                return loginData.get("access_token");
            }
        }catch (IOException e){
            HeliosClient.LOGGER.error("An error occurred while getting access token",e);
        }
        return null;
    }

    public static HttpURLConnection doPostAndGet(String urlPost) throws IOException {
        URL url = new URL(urlPost);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        return conn;
    }
}
