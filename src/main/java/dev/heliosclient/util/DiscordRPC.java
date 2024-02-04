package dev.heliosclient.util;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityType;
import dev.heliosclient.HeliosClient;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static dev.heliosclient.HeliosClient.LOGGER;

public class DiscordRPC {
    public static DiscordRPC INSTANCE = new DiscordRPC();
    public GameState currentGameState = GameState.MAINMENU;
    public boolean isRunning = false;
    File discordLibrary = null;
    private Thread callbackThread;
    private Core discordCore;
    private Activity activity;

    /**
     * File should be around 25mb. Any more and there is some error while downloading. Please report to devs.
     *
     * @return
     * @throws IOException
     */
    public static File downloadDiscordGameSDK() throws IOException {
        // Find out which name Discord's library has (.dll for Windows, .so for Linux)
        String name = "discord_game_sdk";
        String suffix;

        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);

        if (osName.contains("windows")) {
            suffix = ".dll";
        } else if (osName.contains("linux")) {
            suffix = ".so";
        } else if (osName.contains("mac os")) {
            suffix = ".dylib";
        } else {
            throw new RuntimeException("Cannot determine OS type: " + osName);
        }

        if (arch.equals("amd64"))
            arch = "x86_64";

        String zipPath = "lib/" + arch + "/" + name + suffix;

        URL downloadUrl = new URL("https://dl-game-sdk.discordapp.net/2.5.6/discord_game_sdk.zip");

        HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();

        connection.setRequestProperty("User-Agent", "discord-game-sdk4j (https://github.com/JnCrMx/discord-game-sdk4j)");
        ZipInputStream zin = new ZipInputStream(connection.getInputStream());

        ZipEntry entry;
        while ((entry = zin.getNextEntry()) != null) {
            if (entry.getName().equals(zipPath)) {
                File tempDir = new File(System.getProperty("java.io.tmpdir"), "java-" + name + System.nanoTime());
                if (!tempDir.mkdir())
                    throw new IOException("Cannot create temporary directory");
                tempDir.deleteOnExit();

                File temp = new File(tempDir, name + suffix);
                temp.deleteOnExit();

                Files.copy(zin, temp.toPath());

                zin.close();

                return temp;
            }
            zin.closeEntry();
        }
        zin.close();

        return null;
    }

    public void getLibrary() {
        try {
            discordLibrary = downloadDiscordGameSDK();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (discordLibrary == null) {
            LOGGER.error("Error downloading Discord SDK.");
        }
    }

    public void runPresence() {
        callbackThread = new Thread(() -> {
        LOGGER.info("Discord Rich Presence is running!");
        Core.init(discordLibrary);
        try (CreateParams params = new CreateParams()) {
            params.setClientID(1203402546626957373L);
            params.setFlags(CreateParams.getDefaultFlags());
            // Create the Core
            discordCore = new Core(params);
            activity = new Activity();

            updateActivity();
            activity.timestamps().setStart(Instant.now());


                // Run callbacks forever
                isRunning = true;
                while (true) {
                    discordCore.runCallbacks();
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        }
        });
        callbackThread.setName("Discord-Callback");
        callbackThread.start();
    }

    public void stopPresence() {
        if (!isRunning || !discordCore.isOpen()) {
            LOGGER.error("Discord RPC is not running");
            return;
        }

        callbackThread.interrupt();
        activity.close();
        discordCore.close();
        isRunning = false;
        LOGGER.info("Discord Presence has stopped");
    }

    public void updateActivity() {
        if (HeliosClient.MC.player != null) {
            activity.setDetails("Playing as " + HeliosClient.MC.player.getName().getString() + " on: " + HeliosClient.MC.getCurrentServerEntry().address);
        } else {
            activity.setDetails("Idle");
        }

        activity.setState("Currently on: " + currentGameState.getName());
        activity.setType(ActivityType.PLAYING);


        activity.assets().setLargeText("Playing with HeliosClient");
        activity.assets().setLargeImage("heliosdevelopment");

        activity.assets().setSmallImage("icon");

        // Finally, update the current activity to our activity
        discordCore.activityManager().updateActivity(activity);
        isRunning = true;
    }

    public enum GameState {
        MAINMENU("Main Menu"),
        REALMS("Realms Screen"),
        SINGLEPLAYER("SinglePlayer world"),
        MULTIPLAYER("Multi-Player server"),
        SINGLEPLAYER_SCREEN("SinglePlayer Screen"),
        MULTIPLAYER_SCREEN("Multi-Player Screen");

        private final String name;

        GameState(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }
}
