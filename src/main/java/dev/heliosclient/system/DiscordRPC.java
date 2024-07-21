package dev.heliosclient.system;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.GameSDKException;
import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityType;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.module.Module_;
import dev.heliosclient.util.animation.AnimationUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static dev.heliosclient.HeliosClient.LOGGER;
import static dev.heliosclient.HeliosClient.MC;

public class DiscordRPC {
    public static DiscordRPC INSTANCE = new DiscordRPC();
    static File tempDir = new File(System.getProperty("java.io.tmpdir"), "java");
    public GameState currentGameState = GameState.MAINMENU;
    public boolean isRunning = false;
    private Thread callbackThread;
    private Core discordCore;
    private Activity activity;

    public void init() {
        try {
            File sdkFile = this.getSDKLibrary();

            // Initialize the Core with the existing or downloaded file

            File tempJNIDir = new File(System.getProperty("java.io.tmpdir") + "/java", "jni");

            if (!tempJNIDir.isDirectory() && !tempJNIDir.mkdir())
                throw new RuntimeException(new IOException("Cannot create temporary JNI directory"));

            tempJNIDir.deleteOnExit();

            initInternal(sdkFile, tempJNIDir);

        } catch (IOException | RuntimeException e) {
            AnimationUtils.addErrorToast("Discord Core init failed. Check logs for details.", false, 1500);
            HeliosClient.LOGGER.error("Discord Core init failed.", e);
        }
    }

    /**
     * The {@link Core#init(File, File)} is not memory efficient and creates stores new "discord_game_sdk_jni" for every run.
     * It builds up space really fast and I got to `80mb` in 40 runs.
     *<p>
     * So in here, we are returning if the file in the temp directory already exists.
     */
    private static void initInternal(File discordLibrary, File tempDir)
    {
        String name = "discord_game_sdk_jni";
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);

        String objectName;

        if(osName.contains("windows"))
        {
            osName = "windows";
            objectName = name + ".dll";

            // the Discord native library needs to be loaded before our JNI library on Windows
            System.load(discordLibrary.getAbsolutePath());
        }
        else if(osName.contains("linux"))
        {
            osName = "linux";
            objectName = "lib" + name + ".so";
        }
        else if(osName.contains("mac os"))
        {
            osName = "macos";
            objectName = "lib" + name + ".dylib";
        }
        else
        {
            throw new RuntimeException("cannot determine OS type: "+osName);
        }

		/*
		Some systems (e.g. Mac OS X) might report the architecture as "x86_64" instead of "amd64".
		While it would be possible to store the MacOS dylib as "x86_x64" instead of "amd64",
		I personally prefer to keep the system architecture consistent.
		 */
        if(arch.equals("x86_64"))
            arch = "amd64";

        File temp = new File(tempDir, objectName);
        if(temp.exists()){
            System.load(temp.getAbsolutePath());
            Core.initDiscordNative(discordLibrary.getAbsolutePath());
            LOGGER.info("(DiscordRPC) JNI directory exists");
            temp.deleteOnExit();
            return;
        }

        String path = "/native/"+osName+"/"+arch+"/"+objectName;
        InputStream in = Core.class.getResourceAsStream(path);
        if(in == null)
            throw new RuntimeException(new FileNotFoundException("cannot find native library at "+path));

        try
        {
            Files.copy(in, temp.toPath());
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }

        System.load(temp.getAbsolutePath());
        Core.initDiscordNative(discordLibrary.getAbsolutePath());
    }
    /**
     * This is the modified version of {@link Core#downloadDiscordLibrary()}
     * In this version, it checks if the required file already exists in the Temp folder,
     * If not then it will proceed to download and extract a new one.
     * <p>
     * Modified because the original downloads the file everytime and fills the temp folder quickly.
     * </p>
     *
     * @return Temp file needed
     * @throws IOException
     */
    private File getSDKLibrary() throws IOException {
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
            throw new RuntimeException("cannot determine OS type: " + osName);
        }

        if (arch.equals("amd64"))
            arch = "x86_64";

        // Path of Discord's library inside the ZIP
        String zipPath = "lib/" + arch + "/" + name + suffix;

        // Create a new temporary directory
        File temp = new File(tempDir, name + suffix);

        // Check if the file already exists
        if (!temp.exists()) {
            // If not, download discord sdk and extract it
            URL downloadUrl = new URL("https://dl-game-sdk.discordapp.net/2.5.6/discord_game_sdk.zip");
            ZipInputStream zin = new ZipInputStream(downloadUrl.openStream());

            // Search for the right file inside the ZIP
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                if (entry.getName().equals(zipPath)) {
                    if (!tempDir.mkdir())
                        throw new IOException("Cannot create temporary directory");

                    tempDir.deleteOnExit();

                    // Copy the file in the ZIP to our temporary file
                    Files.copy(zin, temp.toPath());

                    // We are done, so close the input stream
                    zin.close();

                    return temp;
                }

                // Next entry
                zin.closeEntry();
            }
            // Close if not found and throw new error
            zin.close();
            throw new FileNotFoundException("Required GameSDK file was not found");
        }
        return temp;
    }

    public void runPresence(Module_ module) throws GameSDKException {
        callbackThread = new Thread(() -> {
            LOGGER.info("Discord Rich Presence is running!");

            try (CreateParams params = new CreateParams()) {
                // Please don't hack me ಥ_ಥ
                params.setClientID(1203402546626957373L);
                params.setFlags(CreateParams.Flags.NO_REQUIRE_DISCORD);

                // Create the Core
                try {
                    discordCore = new Core(params);
                } catch (GameSDKException e) {
                    AnimationUtils.addErrorToast("Discord Application is not running. Toggling off", false, 1500);
                    module.toggle();
                    return;
                }

                activity = new Activity();

                updateActivity();
                activity.timestamps().setStart(Instant.now());


                // Run callbacks forever
                isRunning = true;
                while (true) {
                    discordCore.runCallbacks();
                    try {
                        //More delay == More CPU happy
                        Thread.sleep(50);
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
        if (HeliosClient.MC.player == null) {
            activity.setDetails("Idle");
        } else {
            activity.setDetails("Playing as " + HeliosClient.MC.player.getName().getString() + " on: " + (Objects.requireNonNull(MC.getCurrentServerEntry()).address == null ? "----" : HeliosClient.MC.getCurrentServerEntry().address));
        }

        activity.setState("Currently on: " + currentGameState.getStateName());
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

        public String getStateName() {
            return this.name;
        }
    }
}
