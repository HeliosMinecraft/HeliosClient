package dev.heliosclient.system.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.system.HeliosExecutor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * So the way this works by is that each file is treated as a sub config.
 * A Sub config are just multiple versions of a main config, like for e.g. modules.
 * <p>
 * Each sub-config holds the read data of the file as well the write data which is going to be written into the file during saving.
 * This helps in maintaining different cases although if it goes unnoticed then it could take a lot of memory.
 * By default, the Gson writer is made lenient so any errors in syntax made by user won't cause problems.
 * <p>
 * The `loaded` flag isn't used anywhere, but it can be helpful for debugging.
 *
 * @see ConfigManager
 * @see Config
 */
public class SubConfig {
    private final Gson gson;
    File configFile;
    private Map<String, Object> readData;
    private final Map<String, Object> writeData;

    private boolean loaded;

    public SubConfig(String configName) {
        configFile = new File(configName + ".json");
        gson = new GsonBuilder()
                .setLenient()
                .setPrettyPrinting()
                .create();


        readData = new HashMap<>();
        writeData = new HashMap<>();
        loaded = false;
    }

    public Map<String, Object> getReadData() {
        return readData;
    }

    public Map<String, Object> getWriteData() {
        return writeData;
    }

    public File getConfigFile() {
        return configFile;
    }

    public void setConfigFile(File configFile) {
        this.configFile = configFile;
    }

    public String getName() {
        if(configFile == null){
            return "";
        }
        return configFile.getName();
    }

    public void load() {
        try {
            if (configFile.exists()) {
                try (Reader reader = Files.newBufferedReader(Paths.get(configFile.getAbsolutePath()))) {
                    readData = gson.fromJson(reader, Map.class);
                    loaded = true;
                }
            }
        } catch (IOException e) {
            HeliosClient.LOGGER.error("Error saving config \"{}\"", configFile.getAbsolutePath(), e);
        }
    }

    public boolean save(boolean fixReadData) {
        Future<Boolean> task = HeliosExecutor.submit(() -> {
            if(writeData.isEmpty()) {
                HeliosClient.LOGGER.error("Write data was empty!, Saving interrupted. File Path \"{}\"",configFile.getAbsolutePath());
                return false;
            }

            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(writeData, writer);
            } catch (IOException e) {
                HeliosClient.LOGGER.error("Error saving config \"{}\"", configFile.getAbsolutePath(), e);
                return false;
            }

            //Syncs read data with write data, helps with the hassle of loading again.
            if (fixReadData) {
                readData.clear();
                readData.putAll(writeData);
            }

            return true;
        });

        try {
            //safe
            return task.get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public Object get(String key) {
        return readData.get(key);
    }

    public boolean isLoaded() {
        return loaded;
    }
}