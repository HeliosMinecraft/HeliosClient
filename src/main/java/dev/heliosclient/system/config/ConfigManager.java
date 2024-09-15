package dev.heliosclient.system.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a config manager which holds a collection of configs and manages their them.
 * It also provides some null-safe methods
 */
public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path configDir;
    private final ConcurrentHashMap<String, SubConfig> configs;
    private SubConfig currentConfig;
    private final List<String> configNames;
    private String defaultFileName;

    public ConfigManager(String configDir, String defaultFileName) {
        this(Path.of(configDir), defaultFileName);
    }

    public ConfigManager(Path configDir, String defaultFileName) {
        this.configDir = configDir;
        this.configs = new ConcurrentHashMap<>();
        this.configNames = new ArrayList<>();
        this.defaultFileName = defaultFileName;

        if (!Files.exists(configDir)) {
            try {
                Files.createDirectories(configDir);
                HeliosClient.LOGGER.warn("File directory not found, \"{}\" . Creating a new directory", configDir.toAbsolutePath());
            } catch (IOException e) {
                HeliosClient.LOGGER.error("Failed to create config directory", e);
            }
        }
        load();
    }

    private void loadConfigs(String defaultFileName) {
        configNames.clear();
        try {
            Files.list(configDir)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString().replace(".json", "");
                        SubConfig config = loadConfig(fileName);
                        configs.put(fileName, config);
                        configNames.add(fileName);

                        if (fileName.equals(defaultFileName)) {
                            currentConfig = config;
                        }
                        HeliosClient.LOGGER.info("SubConfig found, \"{}\"", path.toAbsolutePath());
                    });

            if (currentConfig == null && !defaultFileName.isEmpty()) {
                Path defaultPath = configDir.resolve(defaultFileName + ".json");
                Files.createFile(defaultPath);
                currentConfig = new SubConfig(defaultFileName, new HashMap<>(), defaultPath);
                configs.put(defaultFileName, currentConfig);
                configNames.add(defaultFileName);
            }
        } catch (IOException e) {
            HeliosClient.LOGGER.error("Error while loading configs", e);
        }
    }

    private SubConfig loadConfig(String name) {
        Path configPath = configDir.resolve(name + ".json");
        if (Files.exists(configPath)) {
            SubConfig newConfig = new SubConfig(name,new HashMap<>(),configPath);
            newConfig.load();
            return newConfig;
        }
        return new SubConfig(name, new HashMap<>(), configPath);
    }

    public void put(String key, Object value) {
        if (currentConfig != null) {
            currentConfig.set(key, value);
        }
    }

    public boolean checkIfEmpty() {
        return checkIfEmpty(currentConfig);
    }

    public boolean checkIfEmpty(SubConfig config) {
        return config != null && FileUtils.isFileEmpty(config.getConfigFile());
    }

    public boolean checkIfEmpty(String fileName) {
        SubConfig config = configs.get(fileName);
        return checkIfEmpty(config);
    }

    public boolean save() {
        return currentConfig != null && currentConfig.save();
    }
    public void loadCurrent() {
        if (currentConfig != null) {
            currentConfig.load();
        }
    }
    public void load() {
        loadConfigs(defaultFileName);
        loadCurrent();
    }

    public void switchConfig(String configName, boolean saveCurrent) {
        SubConfig newConfig = configs.get(configName);
        if (newConfig != null && currentConfig != newConfig) {
            if (saveCurrent && currentConfig != null) {
                if (currentConfig.save()) {
                    currentConfig.clearReadData();
                    newConfig.load();
                    currentConfig = newConfig;
                }
            } else {
                if (currentConfig != null) {
                    currentConfig.clearReadData();
                }
                newConfig.load();
                currentConfig = newConfig;
            }
        } else if (currentConfig != newConfig) {
            HeliosClient.LOGGER.error("SubConfig with name \"{}\" was not found while switching", configName);
        }
    }

    public void checkDirectoryAgain() {
        configs.clear();
        loadConfigs(currentConfig != null ? currentConfig.getName() : "");
    }

    public void createAndAdd(String fileName) {
        Path configPath = configDir.resolve(fileName + ".json");
        SubConfig config = new SubConfig(fileName, new HashMap<>(), configPath);

        if (!Files.exists(configPath)) {
            try {
                Files.createFile(configPath);
                HeliosClient.LOGGER.info("Created file successfully \"{}\"", configPath.toAbsolutePath());
            } catch (IOException e) {
                HeliosClient.LOGGER.error("Error while creating new file: \"{}\"", fileName, e);
                return;
            }
        }

        configs.put(fileName, config);
        configNames.add(fileName);
    }

    public SubConfig getSubConfig(String configName) {
        return configs.get(configName);
    }

    public SubConfig getCurrentConfig() {
        return currentConfig;
    }

    public List<String> getConfigNames() {
        return configNames;
    }

    public static class SubConfig {
        private final String name;
        private final Map<String, Object> data;
        private Path configFile;

        private SubConfig(String name, Map<String, Object> data, Path configFile) {
            this.name = name;
            this.data = data;
            this.configFile = configFile;
        }

        public void setConfigFile(File file) {
            this.configFile = file.toPath();
        }

        public String getName() {
            return name;
        }
        public boolean create() {
            try {
                return configFile.toFile().createNewFile();
            } catch (IOException e) {
                HeliosClient.LOGGER.warn("Error while creating config", e);
                return false;
            }
        }

        public Map<String, Object> getData() {
            return data;
        }

        public Path getConfigPath() {
            return configFile;
        }
        public File getConfigFile() {
            return configFile.toFile();
        }

        public boolean save() {
            try (Writer writer = Files.newBufferedWriter(configFile)) {
                GSON.toJson(data, writer);
                return true;
            } catch (IOException e) {
                HeliosClient.LOGGER.error("Failed to save config: {}", name, e);
                return false;
            }
        }

        public void load() {
            try (Reader reader = Files.newBufferedReader(configFile)) {
                Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
                Map<String, Object> loadedData = GSON.fromJson(reader, type);
                data.clear();
                if(loadedData == null) return;
                data.putAll(loadedData);
            } catch (IOException e) {
                HeliosClient.LOGGER.error("Failed to load config: {}", name, e);
            }
        }
        public <T> T get(String key, Class<T> type) {
            Object value = data.get(key);
            if (type.isInstance(value)) {
                return type.cast(value);
            }
            return null;
        }

        public <T> T get(String key, Class<T> type, T defaultValue) {
            T value = get(key, type);
            return value != null ? value : defaultValue;
        }

        public void set(String key, Object value) {
            data.put(key, value);
        }

        public boolean has(String key) {
            return data.containsKey(key);
        }

        public void remove(String key) {
            data.remove(key);
        }

        public void clearReadData() {
            data.clear();
        }

        public Map<String, Object> getReadData() {
            return new HashMap<>(data);
        }

        public Map<String, Object> getWriteData() {
            return data;
        }
    }
}