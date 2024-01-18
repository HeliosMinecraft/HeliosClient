package dev.heliosclient.managers;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.FileUtils;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

// ConfigManager is responsible for managing the configuration files.
public class ConfigManager {
    // Stores the configuration maps.
    private final Map<String, Map<String, Object>> configMaps = new HashMap<>();
    // Stores the Toml objects.
    private final Map<String, Toml> tomls = new HashMap<>();
    // Stores the configuration files.
    private final Map<String, File> configFiles = new HashMap<>();
    // TomlWriter to write the configuration to the file.
    private final TomlWriter tomlWriter = new TomlWriter.Builder()
            .indentTablesBy(2)
            .build();
    // Directory where the configuration files are stored.
    private final File configDir;

    // Constructor initializes the configuration directory.
    public ConfigManager(MinecraftClient mc) {
        this.configDir = new File(mc.runDirectory.getPath() + "/heliosclient");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
    }

    // Registers a new configuration.
    public void registerConfig(String name, Map<String,Object> hashmap) {
        configMaps.put(name, hashmap);
        tomls.put(name, new Toml());
        configFiles.put(name, new File(configDir, name + ".toml"));
    }

    public Map<String, Map<String, Object>> getConfigMaps() {
        return configMaps;
    }

    public Map<String, Toml> getTomls() {
        return tomls;
    }

    public Map<String, File> getConfigFiles() {
        return configFiles;
    }

    public File getConfigDir() {
        return configDir;
    }

    // Loads the configurations from the files.
    public boolean load() {
        for (String name : configMaps.keySet()) {
            try {
                tomls.put(name, new Toml().read(configFiles.get(name)));
                if (tomls.get(name) != null) {
                    configMaps.put(name, tomls.get(name).toMap());
                } else {
                    throw new FileNotFoundException();
                }
            } catch (Exception e) {
                HeliosClient.LOGGER.error("Error occurred while loading config. Load default config.", e);
                return false;
            }
        }
        return true;
    }

    // Saves the configurations to the files.
    public void save() {
        for (String name : configMaps.keySet()) {
            try {
                if (!FileUtils.doesFileInPathExist(configFiles.get(name).getPath())) {
                    configFiles.get(name).createNewFile();
                }
                tomlWriter.write(configMaps.get(name), configFiles.get(name));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
