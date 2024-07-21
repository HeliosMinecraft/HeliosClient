package dev.heliosclient.system.config;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This is a config manager which holds a collection of sub configs and manages their them.
 * It also provides some null-safe methods
 */
public class ConfigManager {
    private final List<SubConfig> subConfigs;
    private final List<String> configNames = new ArrayList<>();

    private SubConfig currentConfig;
    private final File configDir;

    public ConfigManager(String configDir, String defaultFileName) {
        this(new File(configDir), defaultFileName);
    }

    public ConfigManager(File dir, String defaultFileName) {
        subConfigs = new ArrayList<>();
        this.configDir = dir;

        if (!dir.exists()) {
            HeliosClient.LOGGER.warn("File directory not found, \"{}\" . Creating a new directory", dir.getAbsolutePath());
            dir.mkdirs();
        }

        loadConfigs(defaultFileName);
    }

    /**
     * Puts the key and value in the current config if not null
     */
    public void put(String key, Object value) {
        if (currentConfig != null) {
            currentConfig.getWriteData().put(key, value);
        }
    }


    /**
     * Checks if the current config is empty
     */
    public boolean checkIfEmpty() {
        if (currentConfig != null) {
            return FileUtils.isFileEmpty(currentConfig.configFile);
        }

        return false;
    }

    /**
     * Checks if the given config is empty
     */
    public boolean checkIfEmpty(SubConfig config) {
        if (config != null) {
            return FileUtils.isFileEmpty(config.configFile);
        }

        return false;
    }

    /**
     * Checks if the given config is empty
     */
    public boolean checkIfEmpty(String fileName) {
        for (SubConfig config : subConfigs) {
            if (config.configFile.getName().replace(".json", "").equals(fileName)) {
                return checkIfEmpty(config);
            }
        }

        return false;
    }

    /**
     * Calls the save method of the current config if it is not null.
     * Remember to write data before saving else it won't save!
     *
     * @return whether it has been saved or not
     */
    public boolean save(boolean fixReadData) {
        if (currentConfig != null) {
            return currentConfig.save(fixReadData);
        }

        return false;
    }

    /**
     * Calls the load method of the current config if it is not null
     */
    public void load() {
        if (currentConfig != null) {
            currentConfig.load();
        }
    }

    /**
     * Loads each file ending with `.json` extension in the directory. If it finds a file with the given name, then it would be set as the current config.
     * Otherwise, an empty file with the same name would be created.
     */
    private void loadConfigs(String defaultFileName) {
        configNames.clear();
        for (File file : Objects.requireNonNull(configDir.listFiles((dir1, name) -> name.endsWith(".json")))) {
            String fileName = file.getName().replace(".json", "");
            SubConfig subConfig = new SubConfig(file.getAbsolutePath().replace(".json", ""));
            HeliosClient.LOGGER.info("SubConfig found, \"{}\"", file.getAbsolutePath());
            subConfigs.add(subConfig);
            configNames.add(fileName);

            if (fileName.equals(defaultFileName)) {
                currentConfig = subConfig;
            }
        }

        if (currentConfig == null && !defaultFileName.isEmpty()) {
            File file = new File(configDir, defaultFileName + ".json");
            try {
                file.createNewFile();
                currentConfig = new SubConfig(file.getAbsolutePath().replace(".json", ""));
                subConfigs.add(currentConfig);
            } catch (IOException e) {
                HeliosClient.LOGGER.error("Error while creating new default file: \"{}\"", defaultFileName, e);
            }
        }
    }

    /**
     * Advised to always save current but, sometimes it may not be needed
     */
    public void switchConfig(String configName, boolean saveCurrent) {
        SubConfig newConfig = getSubConfig(configName);
        if (newConfig != null && currentConfig != newConfig) {
            if (saveCurrent && currentConfig != null) {
                if (currentConfig.save(false)) {

                    //Free our precious memory. We can read it again anyway. And we have stored what we wanted
                    currentConfig.getReadData().clear();

                    newConfig.load();
                    currentConfig = newConfig;
                }
            } else {
                //We will probably read it again so why not clear it now
                if (currentConfig != null) {
                    currentConfig.getReadData().clear();
                }

                newConfig.load();
                currentConfig = newConfig;
            }
        }else if(currentConfig != newConfig){
            HeliosClient.LOGGER.error("Config with name \"{}\" was not found while switching",configName);
        }
    }

    public void checkDirectoryAgain() {
        subConfigs.clear();
        loadConfigs(currentConfig != null ? currentConfig.configFile.getName().replace(".json", "") : "");
    }

    public void addSubConfig(SubConfig subConfig) {
        subConfigs.removeIf(sc -> sc.configFile == subConfig.configFile);
        subConfigs.add(subConfig);
    }

    public void createAndAdd(String fileName) {
        SubConfig subConfig = new SubConfig(configDir.getAbsolutePath() + "/" + fileName);

        if (!subConfig.configFile.exists() && subConfig.create()) {
            HeliosClient.LOGGER.info("Created file successfully \"{}\"", subConfig.configFile.getAbsolutePath());
        }

        addSubConfig(subConfig);
    }


    public SubConfig getSubConfig(String configName) {
        for (SubConfig subConfig : subConfigs) {
            if (subConfig.configFile.getName().replace(".json", "").equals(configName)) {
                return subConfig;
            }
        }
        return null;
    }

    public SubConfig getCurrentConfig() {
        return currentConfig;
    }

    public List<String> getConfigNames() {
        return configNames;
    }
}