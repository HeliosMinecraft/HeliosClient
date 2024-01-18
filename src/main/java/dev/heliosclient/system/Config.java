package dev.heliosclient.system;

import com.google.gson.Gson;
import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.hud.HudElementList;
import dev.heliosclient.managers.CategoryManager;
import dev.heliosclient.managers.ConfigManager;
import dev.heliosclient.managers.HudManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.ui.clickgui.CategoryPane;
import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import dev.heliosclient.util.FileUtils;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mojang.text2speech.Narrator.LOGGER;

// Config class is responsible for managing the configuration of modules, client, and HUD.
public class Config {
    // Constants for the names of the configurations.
    public static String MODULES = "modules";
    public static String CLIENT = "config";
    public static String HUD = "hud";

    // MinecraftClient instance.
    MinecraftClient mc = MinecraftClient.getInstance();

    // Directory where the configuration files are stored.
    public File configDir = new File(mc.runDirectory.getPath() + "/heliosclient");

    // ConfigManager instance.
    public ConfigManager configManager;

    // Constructor initializes the ConfigManager and registers the configurations.
    public Config() {
        this.configManager = new ConfigManager(HeliosClient.MC);
        this.configManager.registerConfig(MODULES, new HashMap<>());
        this.configManager.registerConfig(CLIENT, new HashMap<>());
        this.configManager.registerConfig(HUD, new HashMap<>());

        // Create the configuration directory if it doesn't exist.
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
    }

    // Generates the default configuration for the modules.
    public void getDefaultModuleConfig() {
        final AtomicInteger[] xOffset = {new AtomicInteger(4)};
        final int[] yOffset = {4};
        Map<String, Object> categoryPaneMap = new HashMap<>();
        CategoryManager.getCategories().forEach((s, category) -> {
            Map<String, Object> paneConfigMap = new HashMap<>();
            if (xOffset[0].get() > 400) {
                xOffset[0].set(4);
                yOffset[0] = 128;
            }
            paneConfigMap.put("x", xOffset[0].get());
            paneConfigMap.put("y", yOffset[0]);
            paneConfigMap.put("collapsed", false);
            for (Module_ module : ModuleManager.INSTANCE.getModulesByCategory(category)) {
                Map<String, Object> ModuleConfig = new HashMap<>();
                for (SettingGroup settingGroup : module.settingGroups) {
                    for (Setting setting : settingGroup.getSettings()) {
                        if (setting.name != null) {
                            ModuleConfig.put(setting.name.replace(" ", ""), setting.saveToToml(new ArrayList<>()));
                        }
                    }
                }
                paneConfigMap.put(module.name.replace(" ", ""), ModuleConfig);
            }
            categoryPaneMap.put(category.name, paneConfigMap);
            getModuleMap().put("panes", categoryPaneMap);
            xOffset[0].addAndGet(100);
        });
    }

    public Map<String, Object> getModuleMap() {
        return configManager.getConfigMaps().get(MODULES);
    }
    public Map<String, Object> getClientConfigMap() {
        return configManager.getConfigMaps().get(CLIENT);
    }
    public Map<String, Object> getHudElementMap() {
        return configManager.getConfigMaps().get(HUD);
    }


    // Gets the configuration for the modules.
    public void getModuleConfig() {
        try {
            Map<String, Object> categoryPaneMap = new HashMap<>();
            CategoryManager.getCategories().forEach((s, category) -> {
                Map<String, Object> paneConfigMap = new HashMap<>();
                CategoryPane categoryPane = CategoryManager.findCategoryPane(category);
                if (categoryPane != null) {
                    paneConfigMap.put("x", categoryPane.x);
                    paneConfigMap.put("y", categoryPane.y);
                    paneConfigMap.put("collapsed", categoryPane.collapsed);
                    for (Module_ module : ModuleManager.INSTANCE.getModulesByCategory(category)) {
                        Map<String, Object> ModuleConfig = new HashMap<>();
                        for (SettingGroup settingGroup : module.settingGroups) {
                            for (Setting setting : settingGroup.getSettings()) {
                                if (setting.name != null) {
                                    ModuleConfig.put(setting.name.replace(" ", ""), setting.saveToToml(new ArrayList<>()));
                                }
                            }
                        }
                        paneConfigMap.put(module.name.replace(" ", ""), ModuleConfig);
                    }
                    categoryPaneMap.put(category.name, paneConfigMap);
                    getModuleMap().put("panes", categoryPaneMap);
                } else {
                    this.getDefaultModuleConfig();
                }
            });
        } catch (Exception e) {
            LOGGER.error("Error occurred while getting module config. Loading default config.", e);
            this.getDefaultModuleConfig();
        }
    }

    /**
     * Gets the configuration for the client.
     */
    public void getHudConfig() {
        try {
            Map<String, Object> hudElements = new HashMap<>();

            int a = 0;
            for (HudElement hudElement : HudManager.INSTANCE.hudElements) {
                hudElements.put("element_" + a, hudElement.saveToToml(new ArrayList<>()));
                a++;
            }

            getHudElementMap().put("hudElements", hudElements);
        } catch (Exception e) {
            LOGGER.error("Error occurred while getting Hud config.", e);
        }
    }

    /**
     * Loads the HudElements from the config.
     */
    public void loadHudElements() {
        HudManager.INSTANCE.hudElements.clear();
        Toml toml = configManager.getTomls().get(HUD).getTable("hudElements");
        if (toml != null) {
            toml.toMap().forEach((string, object) -> {
                Toml hudElementTable =  toml.getTable(string);
                if(hudElementTable.contains("name")) {
                    HudElementData hudElementData = HudElementList.INSTANCE.elementDataMap.get(hudElementTable.getString("name"));
                   HudElement hudElement = hudElementData.create();
                   if(hudElement != null){
                       hudElement.loadFromToml(hudElementTable.toMap(),hudElementTable);
                       HudManager.INSTANCE.addHudElement(hudElement);
                   }
                }
            });
        }
    }

    /**
     * Gets the configuration for the client.
     */
    public void loadConfig() {
        ModuleManager.INSTANCE = new ModuleManager();
        if (!configManager.load()){
            LOGGER.info("Loading default config...");
            this.getDefaultModuleConfig();
            this.save();
        } else {
            this.getModuleConfig();
        }

        this.getClientConfig();
        this.getHudConfig();
        ClickGUIScreen.INSTANCE = new ClickGUIScreen();
    }

    public void loadModules() {
        CategoryManager.getCategories().forEach((s, category) -> {
            for (Module_ m : ModuleManager.INSTANCE.getModulesByCategory(category)) {
                for (SettingGroup settingGroup : m.settingGroups) {
                    for (Setting setting : settingGroup.getSettings()) {
                        Toml newToml = configManager.getTomls().get(MODULES).getTable("panes").getTable(category.name).getTable(m.name.replace(" ", ""));
                        if (newToml != null) {
                            setting.loadFromToml(newToml.toMap(), newToml);
                        }
                    }
                }
            }
        });
    }

    public void loadClientConfigModules() {
        for (SettingGroup settingGroup : HeliosClient.CLICKGUI.settingGroups) {
            for (Setting setting : settingGroup.getSettings()) {
                if (setting.name != null) {
                    Toml settingsToml = configManager.getTomls().get(CLIENT).getTable("settings");
                    if (settingsToml != null)
                        setting.loadFromToml(settingsToml.toMap(), settingsToml);
                }
            }
        }
    }

    public void getClientConfig() {
        getClientConfigMap().put("prefix", ".");
        Map<String, Object> ModuleConfig = new HashMap<>();
        for (SettingGroup settingGroup : HeliosClient.CLICKGUI.settingGroups) {
            for (Setting setting : settingGroup.getSettings()) {
                if (setting.name != null) {
                    ModuleConfig.put(setting.name.replace(" ", ""), setting.saveToToml(new ArrayList<>()));
                }
            }
        }
        getClientConfigMap().put("settings", ModuleConfig);
    }

    public void save() {
        getHudConfig();
        configManager.save();
    }
}
