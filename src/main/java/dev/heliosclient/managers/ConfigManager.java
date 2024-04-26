package dev.heliosclient.managers;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.FileUtils;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manager class for configs. Manages multiple config TOMLs and their maps for saving/loading.
 */
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
    private File configDir;

    // Constructor initializes the configuration directory.
    public ConfigManager(MinecraftClient mc) {
        this.configDir = new File(mc.runDirectory.getPath() + "/heliosclient");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
    }

    public ConfigManager(String pathName) {
        this.configDir = new File(pathName);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
    }

    /**
     * Registers a new configuration.
     *
     * @param name    The name of the configuration.
     * @param hashmap The configuration map.
     */
    public void registerConfig(String name, Map<String, Object> hashmap) {
        if (configMaps.containsKey(name) || tomls.containsKey(name) || configFiles.containsKey(name)) return;
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

    public void setConfigDir(File configDir) {
        this.configDir = configDir;
    }

    /**
     * Loads the configurations from the files.
     *
     * @return True if the configurations were loaded successfully and false if an error occurred (FileNotFoundException).
     */
    public boolean load() {
        for (String name : configMaps.keySet()) {
            try {
                Toml toml = new Toml().read(configFiles.get(name));
                tomls.put(name, toml);
                if (tomls.get(name) == null) {
                    throw new FileNotFoundException();
                } else {
                    configMaps.put(name, tomls.get(name).toMap());
                }
            } catch (Exception e) {
                HeliosClient.LOGGER.error("Error occurred while loading config. Loading default config....", e);

                //Save the config,i.e load a new empty config.
                save(name);
                return false;
            }
        }
        return true;
    }

    /**
     * Saves the configurations to the files.
     */
    public void save() {
        for (String name : configMaps.keySet()) {
            try {
                if (!FileUtils.doesFileInPathExist(configFiles.get(name).getPath())) {
                    configFiles.get(name).createNewFile();
                }
                tomlWriter.write(configMaps.get(name), configFiles.get(name));
            } catch (Exception e) {
                HeliosClient.LOGGER.error("There was a error during saving file: " + name, e);
            }
        }
    }

    /**
     * Saves the configurations to the files.
     */
    public void save(String... names) {
        for (String name : names) {
            try {
                if (!configFiles.containsKey(name) || !configMaps.containsKey(name)) {
                    HeliosClient.LOGGER.warn(name + " not found for saving configuration");
                    throw new FileNotFoundException();
                }
                if (!FileUtils.doesFileInPathExist(configFiles.get(name).getPath())) {
                    configFiles.get(name).createNewFile();
                }
                tomlWriter.write(configMaps.get(name), configFiles.get(name));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns a sorted list of all .toml files in a directory.
     * "modules" always appears in first
     *
     * @param dir The path of the directory.
     * @return A sorted list of all .toml files in the directory.
     */
    public List<String> getTomlFiles(File dir) {
        return Arrays.stream(Objects.requireNonNull(dir.listFiles()))
                .map(File::getName)
                .filter(name -> name.endsWith(".toml"))
                .sorted((name1, name2) -> {
                    if (name1.equals("modules")) {
                        return -69;
                    } else if (name2.equals("modules")) {
                        return 0;
                    } else {
                        return name1.compareTo(name2);
                    }
                })
                .collect(Collectors.toList());
    }
    /**
     * Checks if any of the TOML files is empty.
     *
     * @return The empty file if found, else null.
     */
    public File checkEmptyFiles() {
        for (File file : configFiles.values()) {
            if (file.length() == 0) {
                return file;
            }
        }
        return null;
    }


    public void clear() {
        configFiles.clear();
        configMaps.clear();
        tomls.clear();
    }

}
