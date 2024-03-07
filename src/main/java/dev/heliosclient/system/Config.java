package dev.heliosclient.system;

import com.moandjiezana.toml.Toml;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.hud.HudElementList;
import dev.heliosclient.managers.*;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Category;
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

    // A secondary Config Manager is required to allow easy switch between module configs.
    // Not required for default hud and client config since those
    // files are expected to be replaced by the user instead of switching through the client.
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
                paneConfigMap.put(module.name.replace(" ", ""), module.saveToToml(new ArrayList<>()));
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

                    if(category != Categories.SEARCH) {
                        // Put values of each module into the map
                        for (Module_ module : ModuleManager.INSTANCE.getModulesByCategory(category)) {
                            // Put the map of the module into the map of the category pane. Save all values of the module into the pane map.
                            Single_Pane_Map.put(module.name.replace(" ", ""), module.saveToToml(new ArrayList<>()));
                        }
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
            LOGGER.warn("\"" + HUD + "\" config is empty! Creating new default hud config");
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

    /**
     * Loads HudElement settings only for the registered hud-elements from the config
     */
    public void loadHudElementSettings() {
        Toml tomlElementMap = configManager.getTomls().get(HUD).getTable("hudElements");
        if (tomlElementMap != null) {
            Toml toml = tomlElementMap.getTable("elements");
            if(toml != null) {
                toml.toMap().forEach((string, object) -> {
                    // Get the table of the hudElement
                    Toml hudElementTable = toml.getTable(string);

                    for (HudElement element : HudManager.INSTANCE.hudElements) {
                        if (string.equals(element.id.uniqueID)) {
                            for (SettingGroup settingGroup : element.settingGroups) {
                                for (Setting<?> setting : settingGroup.getSettings()) {
                                    if(!setting.shouldSaveAndLoad()) break;

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
            configManager.save();
        }
        if (modulesManager.load()) {
            this.getModuleConfig();
        } else {
            LOGGER.info("Loading default modules config...");
            this.getDefaultModuleConfig();
            modulesManager.save(MODULES);
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
            LOGGER.warn("\"" + MODULES + "\" module config is empty! Creating new default Modules config");
            getDefaultModuleConfig();
            modulesManager.save(MODULES);
        }

        //World's most inefficient code with complexity of O(4)
        Toml panesTable = modulesManager.getTomls().get(MODULES).getTable("panes");
        // This is the file structure //
        //     panes                  //
        //       |                    //
        //    category  - x,y values of pane   // 1 - loop
        //       |                    //
        //     modules                // - 1 loop
        //       |                    //
        // module settings            // - 2 loops

        CategoryManager.getCategories().forEach((s, category) -> {
            if(category == Categories.SEARCH) return;

            if(panesTable != null && panesTable.containsTable(category.name)) {
                Toml categoryTable = panesTable.getTable(category.name);

                for (Module_ m : ModuleManager.INSTANCE.getModulesByCategory(category)) {
                  m.loadFromToml(categoryTable.toMap(),categoryTable);
                }
            }
        });
    }

    public void loadClientConfigModules() {
        if(FileUtils.isFileEmpty(configManager.getConfigFiles().get(CLIENT))){
            LOGGER.warn("\""+CLIENT + "\" config is empty! Creating new default Modules config");
            getDefaultModuleConfig();
            modulesManager.save(MODULES);
        }
        Toml toml = configManager.getTomls().get(CLIENT);
        CommandManager.prefix = toml.getString("prefix");

        for (SettingGroup settingGroup : HeliosClient.CLICKGUI.settingGroups) {
            for (Setting<?> setting : settingGroup.getSettings()) {
                if(!setting.shouldSaveAndLoad()) break;

                if (setting.name != null) {
                    Toml settingsToml = toml.getTable("settings");
                    if (settingsToml != null)
                        setting.loadFromToml(settingsToml.toMap(), settingsToml);

                    if(setting.iSettingChange != null && setting != HeliosClient.CLICKGUI.switchConfigs && setting != HeliosClient.CLICKGUI.FontRenderer){
                        setting.iSettingChange.onSettingChange(setting);
                    }
                }
            }
        }
    }

    public void getClientConfig() {
        getClientConfigMap().put("prefix", ".");
        Map<String, Object> ModuleConfig = new HashMap<>();
        for (SettingGroup settingGroup : HeliosClient.CLICKGUI.settingGroups) {
            for (Setting<?> setting : settingGroup.getSettings()) {
                if(!setting.shouldSaveAndLoad()) break;

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
