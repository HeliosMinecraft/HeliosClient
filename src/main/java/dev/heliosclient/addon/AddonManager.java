package dev.heliosclient.addon;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.heliosclient.HeliosClient;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddonManager {
    public static final List<HeliosAddon> addons = new ArrayList<>();
    private final Gson gson = new Gson();

    public static void initializeAddons() {
        for ( HeliosAddon addon : addons) {
            addon.onInitialize();
        }
    }

    public void loadAddons() {
        HeliosClient.LOGGER.info("Loading Addons....");
        // Get the current working directory
        String currentWorkingDir = System.getProperty("user.dir");

        File addonsDir = new File(currentWorkingDir);
        if (!addonsDir.exists()) {
            HeliosClient.LOGGER.info("Current folder directory missing for some reason");
            return;
        }

        for (File file : Objects.requireNonNull(addonsDir.listFiles())) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                try {
                    URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});

                    // Parse fabric.mod.json
                    JsonObject jsonObject = gson.fromJson(new FileReader(new File(file, "fabric.mod.json")), JsonObject.class);
                    String mainClassPath = jsonObject.getAsJsonObject("entrypoints").getAsJsonArray("main").get(0).getAsString();

                    Class<?> clazz = Class.forName(mainClassPath, true, classLoader);
                    if (!HeliosAddon.class.isAssignableFrom(clazz)) {
                        continue;  // Skip this file if it's not a HeliosAddon
                    }

                    HeliosAddon addon = (HeliosAddon) clazz.getDeclaredConstructor().newInstance();
                    addon.name = jsonObject.get("name").getAsString();
                    if (addon.name == null || addon.name.isEmpty()) {
                        throw new RuntimeException("The name field in fabric.mod.json cannot be empty");
                    }

                    addon.authors = gson.fromJson(jsonObject.get("authors"), String[].class);
                    if (addon.authors == null || addon.authors.length == 0) {
                        throw new RuntimeException("The authors field in fabric.mod.json must contain at least one author");
                    }

                    addons.add(addon);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
