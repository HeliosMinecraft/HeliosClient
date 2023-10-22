package dev.heliosclient.system;

import com.google.gson.Gson;
import dev.heliosclient.managers.CategoryManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Config {
    protected static Gson gson = new Gson();
    public Map<String, Object> config = new HashMap<>();
    MinecraftClient mc = MinecraftClient.getInstance();
    public File configDir = new File(mc.runDirectory.getPath() + "/heliosclient");
    public File configFile = new File(configDir, "config.json");

    public Config() {
        configDir.mkdirs();
    }

    public boolean doesConfigExist() {
        return Files.exists(configFile.toPath());
    }

    @NotNull
    private static Map<String, Object> getPi() {
        final AtomicInteger[] xOffset = {new AtomicInteger(4)};
        final int[] yOffset = {4};
        Map<String, Object> pi = new HashMap<>();
        CategoryManager.getCategories().forEach((s, category) -> {
            Map<String, Object> po = new HashMap<>();
            if (xOffset[0].get() > 400) {
                xOffset[0].set(4);
                yOffset[0] = 128;
            }
            po.put("x", xOffset[0].get());
            po.put("y", yOffset[0]);
            po.put("collapsed", false);
            pi.put(category.name, po);
            xOffset[0].addAndGet(100);
        });
        return pi;
    }

    public void loadDefaultConfig() {
        ModuleManager.INSTANCE = new ModuleManager();
        Map<String, Object> moduleConfig = new HashMap<>();
        for (Module_ module : ModuleManager.INSTANCE.modules) {
            Map<String, Object> singleModuleConfig = new HashMap<>();
            for (SettingGroup settingBuilder : module.settingGroups) {
                for (Setting setting : settingBuilder.getSettings()) {
                    singleModuleConfig.put(setting.name, setting.value);
                }
            }
            moduleConfig.put(module.name, singleModuleConfig);
        }
        config.put("modules", moduleConfig);
        Map<String, Object> pi = getPi();
        config.put("panes", pi);
        config.put("prefix", ".");
        ClickGUIScreen.INSTANCE = new ClickGUIScreen();
    }

    public void load() {
        try {
            String configText = new String(Files.readAllBytes(configFile.toPath()));
            config = (Map<String, Object>) gson.fromJson(configText, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            if (!doesConfigExist()) configFile.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String configText = gson.toJson(config);
            FileWriter writer = new FileWriter(configFile);
            writer.write(configText);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
