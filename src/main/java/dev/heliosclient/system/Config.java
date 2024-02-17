package dev.heliosclient.system;

import com.moandjiezana.toml.Toml;
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
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.ui.clickgui.CategoryPane;
import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import dev.heliosclient.ui.clickgui.hudeditor.HudCategoryPane;
import dev.heliosclient.ui.clickgui.hudeditor.HudElementButton;
import dev.heliosclient.util.FileUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mojang.text2speech.Narrator.LOGGER;

// Config class is responsible for managing the configuration of modules, client, and HUD.
public class Config {
    // Constants for the names of the configurations.
    public static String MODULES = "modules";
    public static String CLIENT = "config";
    public static String HUD = "hud";

    // ConfigManager instance.
    public ConfigManager configManager;
    public ConfigManager modulesManager;
    public List<String> CONFIGS, MODULE_CONFIGS;

    // Constructor initializes the ConfigManager and registers the configurations.
    public Config() {
        this.configManager = new ConfigManager(HeliosClient.MC);
        this.modulesManager = new ConfigManager(HeliosClient.MC.runDirectory + "/heliosclient/modules");

        this.modulesManager.registerConfig(MODULES, new HashMap<>());

        this.configManager.registerConfig(CLIENT, new HashMap<>());
        this.configManager.registerConfig(HUD, new HashMap<>());

        init();
    }

    public void init() {
        CONFIGS = configManager.getTomlFiles(configManager.getConfigDir());
        MODULE_CONFIGS = modulesManager.getTomlFiles(modulesManager.getConfigDir());

        for (String string : CONFIGS) {
            configManager.registerConfig(string.replace(".toml", ""), new HashMap<>());
        }
        for (String string : MODULE_CONFIGS) {
            modulesManager.registerConfig(string.replace(".toml", ""), new HashMap<>());
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
                    for (Setting<?> setting : settingGroup.getSettings()) {
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
        return modulesManager.getConfigMaps().get(MODULES);
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
            // Map for all categoryPanes
            Map<String, Object> All_Category_Panes_Map = new HashMap<>();

            // Run a loop for each category
            CategoryManager.getCategories().forEach((s, category) -> {
                // Store values of each category pane
                Map<String, Object> Single_Pane_Map = new HashMap<>();
                CategoryPane categoryPane = CategoryManager.findCategoryPane(category);
                if (categoryPane == null) {
                    // Load default config if there is no category pane to ensure that the config is not corrupted and all categories are present.
                    this.getDefaultModuleConfig();
                } else {
                    // Put values of each category pane into the map
                    Single_Pane_Map.put("x", categoryPane.x);
                    Single_Pane_Map.put("y", categoryPane.y);
                    Single_Pane_Map.put("collapsed", categoryPane.collapsed);
                    // Put values of each module into the map
                    for (Module_ module : ModuleManager.INSTANCE.getModulesByCategory(category)) {

                        // Map for storing the values of each module
                        Map<String, Object> ModuleConfig = new HashMap<>();
                        for (SettingGroup settingGroup : module.settingGroups) {
                            for (Setting<?> setting : settingGroup.getSettings()) {
                                if (setting.name != null) {
                                    // Put the value of each setting into the map. Call the setting saveToToml method to get the value of the setting.
                                    ModuleConfig.put(setting.name.replace(" ", ""), setting.saveToToml(new ArrayList<>()));
                                }
                            }
                        }
                        // Put the map of the module into the map of the category pane. Save all values of the module into the pane map.
                        Single_Pane_Map.put(module.name.replace(" ", ""), ModuleConfig);
                    }
                    // Put all the data of the paneConfigMap into the universal CategoryPaneMap keyed with the name of the category.
                    All_Category_Panes_Map.put(category.name, Single_Pane_Map);

                    // Finally put all the panes into the module config map to be saved or loaded.
                    getModuleMap().put("panes", All_Category_Panes_Map);
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
            Map<String, Object> pane = new HashMap<>();
            Map<String, Object> hudElements = new HashMap<>();

            for (HudElement hudElement : HudManager.INSTANCE.hudElements) {
                hudElements.put(hudElement.id.uniqueID, hudElement.saveToToml(new ArrayList<>()));
            }
            pane.put("x", HudCategoryPane.INSTANCE == null? 0: HudCategoryPane.INSTANCE.x);
            pane.put("y", HudCategoryPane.INSTANCE == null? 0: HudCategoryPane.INSTANCE.y);

            pane.put("elements", hudElements.isEmpty()? new HashMap<>() :hudElements);

            getHudElementMap().put("hudElements", pane);
        } catch (Exception e) {
            LOGGER.error("Error occurred while getting Hud config.", e);
        }
    }

    /**
     * Loads the HudElements from the config.
     */
    public void loadHudElements() {
        if(FileUtils.isFileEmpty(configManager.getConfigFiles().get(HUD))){
            LOGGER.warn(HUD + " config is empty! Creating new default hud config");
            getHudConfig();
            configManager.save();
        }

        LOGGER.info("Loading Hud Elements... ");
        HudManager.INSTANCE.hudElements.clear();
        // Get the hudElements table from the config
        Toml tomlElementMap = configManager.getTomls().get(HUD).getTable("hudElements");
        if (tomlElementMap != null) {
            Toml toml = tomlElementMap.getTable("elements");
            if(toml != null) {
                // Maps the toml.
                toml.toMap().forEach((string, object) -> {
                    // Get the table of the hudElement
                    Toml hudElementTable = toml.getTable(string);
                    // Get the name of the hudElement and check if it exists
                    if (hudElementTable.contains("name")) {
                        // Creates the hudElement provided by the hudElementData Supplier.
                        HudElementData<?> hudElementData = HudElementList.INSTANCE.elementDataMap.get(hudElementTable.getString("name"));
                        HudElement hudElement = hudElementData.create();

                        // Load the hudElement from the toml and add it to the hudElements list.
                        if (hudElement != null) {
                            hudElement.loadFromToml(hudElementTable.toMap(), hudElementTable);
                            hudElement.id.setUniqueID(string);
                            HudManager.INSTANCE.addHudElement(hudElement);
                        }
                    }
                });
            }
        }
        if(HudCategoryPane.INSTANCE != null) {
            if(tomlElementMap != null) {
                HudCategoryPane.INSTANCE.x = Math.toIntExact(tomlElementMap.getLong("x"));
                HudCategoryPane.INSTANCE.y = Math.toIntExact(tomlElementMap.getLong("y"));
            }
            HudCategoryPane.INSTANCE.hudElementButtons.forEach(HudElementButton::updateCount);
        }
        loadHudElementSettings();
    }

    public void loadHudElementSettings() {
        Toml tomlElementMap = configManager.getTomls().get(HUD).getTable("hudElements");
        if (tomlElementMap != null) {
            Toml toml = tomlElementMap.getTable("elements");
            if(toml != null) {
                tomlElementMap.toMap().forEach((string, object) -> {
                    // Get the table of the hudElement
                    Toml hudElementTable = toml.getTable(string);


                    for (HudElement element : HudManager.INSTANCE.hudElements) {
                        if (string.equals(element.id.uniqueID)) {
                            for (SettingGroup settingGroup : element.settingGroups) {
                                for (Setting<?> setting : settingGroup.getSettings()) {
                                    setting.loadFromToml(hudElementTable.toMap(), hudElementTable);
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * Gets the configuration for the client.
     */
    public void loadConfig() {
        ModuleManager.INSTANCE = new ModuleManager();
        if (configManager.load()) {
            this.getModuleConfig();
        } else {
            LOGGER.info("Loading default config...");
            this.getDefaultModuleConfig();
            this.save();
        }
        if (modulesManager.load()) {
            this.getModuleConfig();
        } else {
            LOGGER.info("Loading default modules config...");
            this.getDefaultModuleConfig();
            this.save();
        }

        this.getClientConfig();
        this.getHudConfig();
        if (ClickGUIScreen.INSTANCE == null) {
            ClickGUIScreen.INSTANCE = new ClickGUIScreen();
        } else {
            ClickGUIScreen.INSTANCE.reset();
        }
    }

    public void loadModules() {
        if(FileUtils.isFileEmpty(modulesManager.getConfigFiles().get(MODULES))){
            LOGGER.warn(MODULES + " module config is empty! Creating new default Modules config");
            getDefaultModuleConfig();
            this.save();
        }

        //Worlds most ineffcient code with complexity of O(4)
        Toml panesToml = modulesManager.getTomls().get(MODULES).getTable("panes");

        CategoryManager.getCategories().forEach((s, category) -> {
            for (Module_ m : ModuleManager.INSTANCE.getModulesByCategory(category)) {
                for (SettingGroup settingGroup : m.settingGroups) {
                    for (Setting<?> setting : settingGroup.getSettings()) {
                        Toml newToml = panesToml.getTable(category.name).getTable(m.name.replace(" ", ""));
                        if (newToml != null) {
                            setting.loadFromToml(newToml.toMap(), newToml);
                        }
                        if (setting == m.active && m.isActive()) {
                            m.onEnable();
                        }
                    }
                }
            }
        });
    }

    public void loadClientConfigModules() {
        if(FileUtils.isFileEmpty(configManager.getConfigFiles().get(CLIENT))){
            LOGGER.warn(CLIENT + " client config is empty! Creating new default Modules config");
            getDefaultModuleConfig();
            this.save();
        }

        for (SettingGroup settingGroup : HeliosClient.CLICKGUI.settingGroups) {
            for (Setting<?> setting : settingGroup.getSettings()) {
                if (setting.name != null) {
                    Toml settingsToml = configManager.getTomls().get(CLIENT).getTable("settings");
                    if (settingsToml != null)
                        setting.loadFromToml(settingsToml.toMap(), settingsToml);

                    if(setting.iSettingChange != null && setting != HeliosClient.CLICKGUI.switchConfigs && setting != ClickGUI.FontRenderer){
                        setting.iSettingChange.onSettingChange(setting);
                    }
                }
            }
        }

        HeliosClient.CLICKGUI.onLoad();
    }

    public void getClientConfig() {
        getClientConfigMap().put("prefix", ".");
        Map<String, Object> ModuleConfig = new HashMap<>();
        for (SettingGroup settingGroup : HeliosClient.CLICKGUI.settingGroups) {
            for (Setting<?> setting : settingGroup.getSettings()) {
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
        modulesManager.save(MODULES);
    }
}
