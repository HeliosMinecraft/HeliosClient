package dev.heliosclient.system.config;


import dev.heliosclient.HeliosClient;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.hud.HudElementList;
import dev.heliosclient.managers.*;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.system.Friend;
import dev.heliosclient.ui.clickgui.CategoryPane;
import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import dev.heliosclient.ui.clickgui.hudeditor.HudCategoryPane;
import dev.heliosclient.util.misc.MapReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        // In HeliosClient.java, we switch to the config which was last chosen in the client-config.
        moduleConfigManager = new ConfigManager(HeliosClient.SAVE_FOLDER.getAbsolutePath() + "/modules", "modules");

        otherConfigManager = new ConfigManager(HeliosClient.SAVE_FOLDER.getAbsolutePath(), "config");
        otherConfigManager.createAndAdd("hud");
        otherConfigManager.createAndAdd("friends");
    }

    public void loadEverything() {
        loadConfigManagers();

        load();
    }

    public void saveEverything() {
        writeConfigData();

        otherConfigManager.save();
        otherConfigManager.getSubConfig("hud").save();
        otherConfigManager.getSubConfig("friends").save();
        moduleConfigManager.save();
    }

    public void writeConfigData() {
        writeClientConfig();
        writeHudConfig();
        writeModuleConfig();
        writeFriendConfig();
    }

    public void load() {
        loadClientConfigModules();
        loadHudElements();
        loadModules();
        loadFriendConfig();

        initClickGUIScreen();
    }

    public boolean nullCheck(){
        return moduleConfigManager == null || otherConfigManager  == null;
    }

    public void loadConfigManagers() {
        //Loads default config if any of the files are empty

        if (otherConfigManager.checkIfEmpty()) {
            LOGGER.warn("Client config is empty!");
            writeClientConfig();
            otherConfigManager.save();
        } else {
            otherConfigManager.load();
        }

        if (moduleConfigManager.checkIfEmpty()) {
            LOGGER.warn("Module config is empty!");
            writeDefaultModuleConfig();
            moduleConfigManager.save();
        } else {
            LOGGER.info("Loading Module SubConfig \"{}\"",moduleConfigManager.getCurrentConfig().getName());
            moduleConfigManager.load();
        }

        if (otherConfigManager.checkIfEmpty("hud")) {
            LOGGER.warn("HUD config is empty!");
            writeHudConfig();
            otherConfigManager.getSubConfig("hud").save();
        } else {
            otherConfigManager.getSubConfig("hud").load();
        }

        if (otherConfigManager.checkIfEmpty("friends")) {
            writeFriendConfig();
            otherConfigManager.getSubConfig("friends").save();
        } else {
            otherConfigManager.getSubConfig("friends").load();
        }
    }

    public void loadModules() {
        MapReader panesMap = new MapReader(moduleConfigManager.getCurrentConfig().get("panes",Map.class));
        // This is the file structure //
        //     panes                  //
        //       |                    //
        //    category  - x,y values of pane   // 1 - loop
        //       |                    //
        //     modules                // - 1 loop
        //       |                    //
        // module settings            // - 2 loops (setting groups and the setting list)

        // Very space efficient ik.

        // I should get a darwin award for this

        LOGGER.info("Loading Modules... ");

        CategoryManager.getCategories().forEach((s, category) -> {
            if (category == Categories.SEARCH) return;

            if (panesMap.map() != null && panesMap.has(category.name)) {
                MapReader trashMap = panesMap.getMap(category.name);

                for (Module_ m : ModuleManager.getModulesByCategory(category)) {
                    m.loadFromFile(trashMap.getMap(m.name.replace(" ","")));
                }
            }
        });
        LOGGER.info("Loading Modules Complete");
    }

    public static Map<String, Object> cast(Object e) {
        return (Map<String, Object>) e;
    }

    public void loadClientConfigModules() {
        MapReader mapReader = new MapReader(otherConfigManager.getCurrentConfig().getReadData());
        CommandManager.prefix =  mapReader.getString("prefix",".");

        LOGGER.info("Loading Client Settings... ");
        MapReader settingsMap = mapReader.getMap("settings");

        for (SettingGroup settingGroup : HeliosClient.CLICKGUI.settingGroups) {
            for (Setting<?> setting : settingGroup.getSettings()) {
                if (!setting.shouldSaveAndLoad()) continue;

                if (setting.name != null) {
                    try {
                        if (settingsMap != null)
                            setting.loadFromFile(settingsMap);
                    }catch (Throwable e){
                        e.printStackTrace();
                        continue;
                    }

                    if (setting.iSettingChange != null && setting != HeliosClient.CLICKGUI.FontRenderer) {
                        setting.iSettingChange.onSettingChange(setting);
                    }
                }
            }
        }
        LOGGER.info("Loading Client Settings Complete");
    }

    public void initClickGUIScreen(){
        if (ClickGUIScreen.INSTANCE == null) {
            ClickGUIScreen.INSTANCE = new ClickGUIScreen();
        } else {
            ClickGUIScreen.INSTANCE.reset();
        }
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
     * Gets the hud configuration for the client.
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
            pane.put("collapsed", HudCategoryPane.INSTANCE != null && HudCategoryPane.INSTANCE.collapsed);


            pane.put("elements", hudElements.isEmpty() ? new HashMap<>() : hudElements);

            otherConfigManager.getSubConfig("hud").getWriteData().put("hudElements", pane);
        } catch (Exception e) {
            LOGGER.error("Error occurred while getting Hud config.", e);
        }
    }
    public void writeFriendConfig() {
        try {
            List<String> friends = new ArrayList<>();

            for(Friend friend: FriendManager.getFriends()){
                friends.add(friend.playerName());
            }
            otherConfigManager.getSubConfig("friends").getWriteData().put("friends", friends);
        } catch (Exception e) {
            LOGGER.error("Error occurred while getting Hud config.", e);
        }
    }
    public void loadFriendConfig() {
        try {
            List<String> friends = (List<String>) otherConfigManager.getSubConfig("friends").getReadData().get("friends");

            for(String friend: friends){
                FriendManager.addFriend(new Friend(friend));
            }
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
        MapReader hudElementsPaneMap = new MapReader(getHudElementsPaneMap());
        if (hudElementsPaneMap.map() != null) {
            HudCategoryPane.INSTANCE.x = hudElementsPaneMap.getInt("x",0);
            HudCategoryPane.INSTANCE.y = hudElementsPaneMap.getInt("y",0);
            HudCategoryPane.INSTANCE.collapsed = hudElementsPaneMap.getBoolean("collapsed",false);

            MapReader hudElementsMap = hudElementsPaneMap.getMap("elements");
            if (hudElementsMap != null) {
                hudElementsMap.map().forEach((str, obj)-> this.loadHudElement(str,hudElementsMap.getMap(str)));
            }
        }

        loadHudElementSettings(hudElementsPaneMap);

        HudCategoryPane.INSTANCE.updateAllCount();

        LOGGER.info("Loading Hud Elements Complete");
    }

    private Map<String, Object> getHudElementsPaneMap() {
        return cast(otherConfigManager.getSubConfig("hud").getReadData().get("hudElements"));
    }

    private void loadHudElement(String id, MapReader map) {
        if (map != null && map.has("name")) {
            HudElementData<?> hudElementData = HudElementList.INSTANCE.elementDataMap.get(map.getString("name",null));
            if(hudElementData != null) {
                HudElement hudElement = hudElementData.create();
                if (hudElement != null) {
                    try {
                        hudElement.loadFromFile(map);
                        hudElement.id.setUniqueID(id);
                        HudManager.INSTANCE.addHudElement(hudElement);
                    } catch (Exception e) {
                        LOGGER.error("An error occurred while loading hud element {}", hudElement.name);
                    }
                }
            }else{
                LOGGER.error("HudElement data was not found for {}", id);
            }
        }
    }

    public void loadHudElementSettings(MapReader hudElementsPaneMap) {
        if (hudElementsPaneMap != null) {
            MapReader hudElementsMap = hudElementsPaneMap.getMap("elements");
            if (hudElementsMap != null) {
                hudElementsMap.map().forEach((str, obj)->{
                    this.loadHudElementSettings(str,hudElementsMap.getMap(str));
                });
            }
        }
    }

    private void loadHudElementSettings(String string, MapReader mapReader) {
        for (HudElement element : HudManager.INSTANCE.hudElements) {
            if (string.equals(element.id.uniqueID)) {
                for (SettingGroup settingGroup : element.settingGroups) {
                    for (Setting<?> setting : settingGroup.getSettings()) {
                        if (!setting.shouldSaveAndLoad()) continue;

                        setting.loadFromFile(mapReader);
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
