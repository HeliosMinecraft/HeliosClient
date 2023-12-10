package dev.heliosclient.system;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.CategoryManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.ui.clickgui.CategoryPane;
import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import dev.heliosclient.util.FileUtils;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mojang.text2speech.Narrator.LOGGER;

public class Config {
    public Map<String, Object> moduleConfigMap = new HashMap<>();
    public Map<String, Object> clientConfigMap = new HashMap<>();
    public TomlWriter tomlWriter = new TomlWriter.Builder()
            .indentTablesBy(2)
            .indentValuesBy(3)
            .padArrayDelimitersBy(1)
            .build();
    public Toml moduleToml;
    public Toml clientToml;
    MinecraftClient mc = MinecraftClient.getInstance();
    public File configDir = new File(mc.runDirectory.getPath() + "/heliosclient");
    public File configFile = new File(configDir, "config.toml");
    public File moduleConfigFile = new File(configDir, "modules.toml");


    public Config() {
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
    }


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
                            ModuleConfig.put(setting.name.replace(" ", ""), setting.saveToToml(new HashMap<>()));
                        }
                    }
                }
                paneConfigMap.put(module.name.replace(" ", ""), ModuleConfig);
            }
            categoryPaneMap.put(category.name, paneConfigMap);
            moduleConfigMap.put("panes", categoryPaneMap);
            xOffset[0].addAndGet(100);
        });
    }

    public void getModuleConfig() {
        Map<String, Object> categoryPaneMap = new HashMap<>();
        CategoryManager.getCategories().forEach((s, category) -> {
            Map<String, Object> paneConfigMap = new HashMap<>();
            CategoryPane categoryPane = CategoryManager.findCategoryPane(category);
                paneConfigMap.put("x", categoryPane.x);
                paneConfigMap.put("y", categoryPane.y);
                paneConfigMap.put("collapsed", categoryPane.collapsed);
                for (Module_ module : ModuleManager.INSTANCE.getModulesByCategory(category)) {
                    Map<String, Object> ModuleConfig = new HashMap<>();
                    for (SettingGroup settingGroup : module.settingGroups) {
                        for (Setting setting : settingGroup.getSettings()) {
                            if (setting.name != null) {
                                ModuleConfig.put(setting.name.replace(" ", ""), setting.saveToToml(new HashMap<>()));
                            }
                        }
                    }
                    paneConfigMap.put(module.name.replace(" ", ""), ModuleConfig);
                }
                categoryPaneMap.put(category.name, paneConfigMap);
                moduleConfigMap.put("panes", categoryPaneMap);
        });
    }

    public void getClientConfig() {
        clientConfigMap.put("prefix", ".");
        Map<String, Object> ModuleConfig = new HashMap<>();
        for (SettingGroup settingGroup : HeliosClient.CLICKGUI.settingGroups) {
            for (Setting setting : settingGroup.getSettings()) {
                if (setting.name != null) {
                    ModuleConfig.put(setting.name.replace(" ", ""), setting.saveToToml(new HashMap<>()));
                }
            }
        }
        clientConfigMap.put("settings", ModuleConfig);
    }

    public boolean shouldLoadDefaultConfig(){
       return !FileUtils.doesFileInPathExist(this.configFile.getPath()) || !FileUtils.doesFileInPathExist(this.moduleConfigFile.getPath());
    }
   public void loadConfig(){
       ModuleManager.INSTANCE = new ModuleManager();
       this.load();
       if(shouldLoadDefaultConfig()){
           LOGGER.info("Loading default config...");
           this.getDefaultModuleConfig();
           ClickGUIScreen.INSTANCE = new ClickGUIScreen();
           this.save();
       }else{
           this.getModuleConfig();
       }
       this.getClientConfig();
    }

    public void load() {
        try {
            moduleToml = new Toml().read(moduleConfigFile);
            clientToml = new Toml().read(configFile);
            moduleConfigMap = moduleToml.toMap();
            clientConfigMap = clientToml.toMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadModules() {
        CategoryManager.getCategories().forEach((s, category) -> {
            for (Module_ m : ModuleManager.INSTANCE.getModulesByCategory(category)) {
                for (SettingGroup settingGroup : m.settingGroups) {
                    for (Setting setting : settingGroup.getSettings()) {
                        Map<String, Object> map = moduleToml.getTable("panes").getTable(category.name).getTable(m.name.replace(" ", "")).toMap();
                        if (map != null) {
                            setting.loadFromToml(map, moduleToml.getTable("panes").getTable(category.name).getTable(m.name.replace(" ", "")));
                        }
                    }
                }
            }
        });
    }

    public void save() {
        try {
            if (!FileUtils.doesFileInPathExist(configFile.getPath())) configFile.createNewFile();
            if (!FileUtils.doesFileInPathExist(moduleConfigFile.getPath())) moduleConfigFile.createNewFile();

            tomlWriter.write(moduleConfigMap, moduleConfigFile);
            tomlWriter.write(clientConfigMap, configFile);
        } catch (Exception feelings) {
            feelings.printStackTrace();
        }
    }
}
