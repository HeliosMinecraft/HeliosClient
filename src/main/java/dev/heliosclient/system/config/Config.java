package dev.heliosclient.system.config;


import dev.heliosclient.HeliosClient;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.hud.HudElementList;
import dev.heliosclient.managers.CategoryManager;
import dev.heliosclient.managers.CommandManager;
import dev.heliosclient.managers.HudManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.ui.clickgui.CategoryPane;
import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import dev.heliosclient.ui.clickgui.hudeditor.HudCategoryPane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.heliosclient.HeliosClient.LOGGER;

/**
 * This is the actual class that manages every config of HeliosClient
 * <p>
 * Though by name {@link ConfigManager} should be managing all configs,
 * but the configManager only manages SubConfigs and does not deal with files outside its given directory
 */
public class Config {
    public ConfigManager moduleConfigManager;
    public ConfigManager otherConfigManager;


    public Config() {
    }

    public void init() {
        ModuleManager.init();

        //Assume that client config file has a key saved with name "config" which represents the config name we were using
        //String defaultFileName = otherConfigManager.getReadData().get("config");
        moduleConfigManager = new ConfigManager(HeliosClient.SAVE_FOLDER.getAbsolutePath() + "/modules", "modules");

        otherConfigManager = new ConfigManager(HeliosClient.SAVE_FOLDER.getAbsolutePath(), "config");
        otherConfigManager.createAndAdd("hud");


        //  loadConfigManagers();
    }

    public void loadEverything() {
        loadConfigManagers();

        load();
    }

    public void saveEverything() {
        writeConfigData();

        otherConfigManager.save(true);
        otherConfigManager.getSubConfig("hud").save(true);
        moduleConfigManager.save(true);
    }

    public void writeConfigData() {
        writeClientConfig();
        writeHudConfig();
        writeModuleConfig();
    }

    public void load() {
        loadClientConfigModules();
        loadHudElements();
        loadModules();

        if (ClickGUIScreen.INSTANCE == null) {
            ClickGUIScreen.INSTANCE = new ClickGUIScreen();
        } else {
            ClickGUIScreen.INSTANCE.reset();
        }
    }


    public void loadConfigManagers() {
        //Loads default config if any of the files are empty

        if (otherConfigManager.checkIfEmpty()) {
            LOGGER.warn("Client config is empty!");
            writeClientConfig();
            otherConfigManager.save(true);
        } else {
            otherConfigManager.load();
        }

        if (moduleConfigManager.checkIfEmpty()) {
            LOGGER.warn("Module config is empty!");
            writeDefaultModuleConfig();
            moduleConfigManager.save(true);
        } else {
            LOGGER.info("Loading Module Config \"{}\"",moduleConfigManager.getCurrentConfig().getName());
            moduleConfigManager.load();
        }

        if (otherConfigManager.checkIfEmpty("hud")) {
            LOGGER.warn("HUD config is empty!");
            writeHudConfig();
            otherConfigManager.getSubConfig("hud").save(true);
        } else {
            otherConfigManager.getSubConfig("hud").load();
        }
    }

    public void loadModules() {
        Map<String, Object> panesMap = cast(moduleConfigManager.getCurrentConfig().getReadData().get("panes"));
        // This is the file structure //
        //     panes                  //
        //       |                    //
        //    category  - x,y values of pane   // 1 - loop
        //       |                    //
        //     modules                // - 1 loop
        //       |                    //
        // module settings            // - 2 loops

        // I should get a darwin award for this

        LOGGER.info("Loading Modules... ");

        CategoryManager.getCategories().forEach((s, category) -> {
            if (category == Categories.SEARCH) return;


            if (panesMap != null && panesMap.containsKey(category.name)) {
                Map<String, Object> trashMap = cast(panesMap.get(category.name));

                for (Module_ m : ModuleManager.getModulesByCategory(category)) {
                    m.loadFromFile(trashMap);
                }
            }
        });
        LOGGER.info("Loading Modules Complete");
    }

    public Map<String, Object> cast(Object e) {
        return (Map<String, Object>) e;
    }

    public void loadClientConfigModules() {
        Map<String, Object> configMap = otherConfigManager.getCurrentConfig().getReadData();

        CommandManager.prefix = (String) configMap.get("prefix");

        LOGGER.info("Loading Client Settings... ");
        Map<String, Object> settingsMap = cast(configMap.get("settings"));

        for (SettingGroup settingGroup : HeliosClient.CLICKGUI.settingGroups) {
            for (Setting<?> setting : settingGroup.getSettings()) {
                if (!setting.shouldSaveAndLoad()) continue;

                if (setting.name != null) {
                    if (settingsMap != null)
                        setting.loadFromFile(settingsMap);

                    if (setting.iSettingChange != null && setting != HeliosClient.CLICKGUI.FontRenderer) {
                        setting.iSettingChange.onSettingChange(setting);
                    }
                }
            }
        }
        LOGGER.info("Loading Client Settings Complete");
    }

    public void writeClientConfig() {
        Map<String, Object> ModuleConfig = new HashMap<>();
        for (SettingGroup settingGroup : HeliosClient.CLICKGUI.settingGroups) {
            for (Setting<?> setting : settingGroup.getSettings()) {
                if (!setting.shouldSaveAndLoad()) continue;

                if (setting.name != null) {
                    ModuleConfig.put(setting.getSaveName(), setting.saveToFile(new ArrayList<>()));
                }
            }
        }
        otherConfigManager.put("settings", ModuleConfig);

        otherConfigManager.put("prefix", ".");
    }

    /**
     * Gets the configuration for the client.
     */
    public void writeHudConfig() {
        try {
            Map<String, Object> pane = new HashMap<>();
            Map<String, Object> hudElements = new HashMap<>();

            for (HudElement hudElement : HudManager.INSTANCE.hudElements) {
                hudElements.put(hudElement.id.uniqueID, hudElement.saveToFile(new ArrayList<>()));
            }
            pane.put("x", HudCategoryPane.INSTANCE == null ? 0 : HudCategoryPane.INSTANCE.x);
            pane.put("y", HudCategoryPane.INSTANCE == null ? 0 : HudCategoryPane.INSTANCE.y);

            pane.put("elements", hudElements.isEmpty() ? new HashMap<>() : hudElements);

            otherConfigManager.getSubConfig("hud").getWriteData().put("hudElements", pane);
        } catch (Exception e) {
            LOGGER.error("Error occurred while getting Hud config.", e);
        }
    }


    // Generates the default configuration for the modules.
    public void writeDefaultModuleConfig() {
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
            paneConfigMap.put("collapsed", category != Categories.SEARCH);
            for (Module_ module : ModuleManager.getModulesByCategory(category)) {
                paneConfigMap.put(module.name.replace(" ", ""), module.saveToFile(new ArrayList<>()));
            }
            categoryPaneMap.put(category.name, paneConfigMap);
            moduleConfigManager.put("panes", categoryPaneMap);
            xOffset[0].addAndGet(100);
        });
    }

    public void loadHudElements() {
        LOGGER.info("Loading Hud Elements... ");

        HudManager.INSTANCE.hudElements.clear();
        Map<String, Object> hudElementsPaneMap = getHudElementsPaneMap();
        if (hudElementsPaneMap != null) {
            Map<String, Object> hudElementsMap = cast(hudElementsPaneMap.get("elements"));
            if (hudElementsMap != null) {
                hudElementsMap.forEach(this::loadHudElement);
            }
        }

        loadHudElementSettings();

        LOGGER.info("Loading Hud Elements Complete");
    }

    private Map<String, Object> getHudElementsPaneMap() {
        return cast(otherConfigManager.getSubConfig("hud").getReadData().get("hudElements"));
    }

    private void loadHudElement(String string, Object object) {
        Map<String, Object> elementMap = cast(object);
        if (elementMap.containsKey("name")) {
            HudElementData<?> hudElementData = HudElementList.INSTANCE.elementDataMap.get((String) elementMap.get("name"));
            HudElement hudElement = hudElementData.create();
            if (hudElement != null) {

                try {
                    hudElement.loadFromFile(elementMap);
                    hudElement.id.setUniqueID(string);
                    HudManager.INSTANCE.addHudElement(hudElement);
                }catch (Exception e){
                    LOGGER.error("An error occurred while loading hud element {}",hudElement.name);
                }
            }
        }
    }

    public void loadHudElementSettings() {
        Map<String, Object> hudElementsPaneMap = getHudElementsPaneMap();
        if (hudElementsPaneMap != null) {
            Map<String, Object> hudElementsMap = cast(hudElementsPaneMap.get("elements"));
            if (hudElementsMap != null) {
                hudElementsMap.forEach(this::loadHudElementSettings);
            }
        }
    }

    private void loadHudElementSettings(String string, Object object) {
        Map<String, Object> elementMap = cast(object);
        for (HudElement element : HudManager.INSTANCE.hudElements) {
            if (string.equals(element.id.uniqueID)) {
                for (SettingGroup settingGroup : element.settingGroups) {
                    for (Setting<?> setting : settingGroup.getSettings()) {
                        if (!setting.shouldSaveAndLoad()) continue;

                        setting.loadFromFile(elementMap);
                    }
                }
            }
        }
    }

    // Gets the configuration for the modules.
    public void writeModuleConfig() {
        try {
            // Map for all categoryPanes
            Map<String, Object> All_Category_Panes_Map = new HashMap<>();

            // Run a loop for each category
            CategoryManager.getCategories().forEach((s, category) -> {
                // Store values of each category pane
                Map<String, Object> Single_Pane_Map = new HashMap<>();
                CategoryPane categoryPane = CategoryManager.findCategoryPane(category);
                if (categoryPane != null) {
                    // Put values of each category pane into the map
                    Single_Pane_Map.put("x", categoryPane.x);
                    Single_Pane_Map.put("y", categoryPane.y);
                    Single_Pane_Map.put("collapsed", categoryPane.collapsed);

                    if (category != Categories.SEARCH) {
                        // Put values of each module into the map
                        for (Module_ module : ModuleManager.getModulesByCategory(category)) {
                            // Put the map of the module into the map of the category pane. Save all values of the module into the pane map.
                            Single_Pane_Map.put(module.name.replace(" ", ""), module.saveToFile(new ArrayList<>()));
                        }
                    }
                    // Put all the data of the paneConfigMap into the universal CategoryPaneMap keyed with the name of the category.
                    All_Category_Panes_Map.put(category.name, Single_Pane_Map);

                    // Finally put all the panes into the module config map to be saved or loaded.
                    moduleConfigManager.put("panes", All_Category_Panes_Map);
                }
            });
        } catch (Exception e) {
            LOGGER.error("Error occurred while getting module config. Loading default config.", e);
            this.writeDefaultModuleConfig();
        }
    }

    public ConfigManager getModuleConfigManager() {
        return moduleConfigManager;
    }

    public ConfigManager getOtherConfigManager() {
        return otherConfigManager;
    }
}
