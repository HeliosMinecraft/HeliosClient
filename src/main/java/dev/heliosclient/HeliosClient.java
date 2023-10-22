package dev.heliosclient;

import dev.heliosclient.addon.AddonManager;
import dev.heliosclient.managers.*;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.system.Config;
import dev.heliosclient.ui.clickgui.CategoryPane;
import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.DamageUtils;
import dev.heliosclient.util.SoundUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HeliosClient implements ModInitializer {
    public static final HeliosClient INSTANCE = new HeliosClient();
    public static final MinecraftClient MC = MinecraftClient.getInstance();
    public static final Logger LOGGER = LoggerFactory.getLogger("Helios Client");
    public static final String clientTag = ColorUtils.red + "Helios Client";
    public static final String versionTag = ColorUtils.gray + "v0.dev";
    public static final String MODID = "heliosclient";

    public static Config CONFIG = new Config();
    public static int uiColorA = 0xFF55FFFF;
    public static int uiColor = 0x55FFFF;
    public static FontManager fontManager = new FontManager();
    public static NotificationManager notificationManager = new NotificationManager();
    public static AddonManager addonManager = new AddonManager();

    @Override
    public void onInitialize() {
        LOGGER.info("Helios Client loading...");
        addonManager.loadAddons();
        AddonManager.initializeAddons();

        Categories.registerCategories();
        loadConfig();

        EventManager.register(fontManager);
        EventManager.register(notificationManager);
        EventManager.register(dev.heliosclient.util.Renderer2D.INSTANCE);
        EventManager.register(KeybindManager.INSTANCE);
        EventManager.register(new DamageUtils());

        SoundUtils.registerSounds();
    }

    public void loadConfig() {
        LOGGER.info("Loading config...");
        if (!CONFIG.doesConfigExist()) {
            CONFIG.loadDefaultConfig();
            CONFIG.save();
        }
        CONFIG.load();

        for (Module_ m : ModuleManager.INSTANCE.modules) {
            for (SettingGroup settingBuilder : m.settingGroups) {
                for (Setting s : settingBuilder.getSettings()) {
                    s.value = ((Map<String, Object>) ((Map<String, Object>) CONFIG.config.get("modules")).get(m.name))
                            .get(s.name);
                }
            }
        }
        CommandManager.prefix = (String) CONFIG.config.get("prefix");
    }

    public void saveConfig() {
        LOGGER.info("Saving config...");
        Map<String, Object> mi = new HashMap<>();
        for (Module_ m : ModuleManager.INSTANCE.modules) {
            Map<String, Object> mo = new HashMap<>();
            for (SettingGroup settingBuilder : m.settingGroups) {
                for (Setting s : settingBuilder.getSettings()) {
                    mo.put(s.name, s.value);
                }
                mi.put(m.name, mo);
            }
        }
        CONFIG.config.put("modules", mi);
        Map<String, Object> pi = new HashMap<>();
        for (CategoryPane c : ClickGUIScreen.INSTANCE.categoryPanes) {
            Map<String, Object> po = new HashMap<>();
            po.put("x", c.x);
            po.put("y", c.y);
            po.put("collapsed", c.collapsed);
            pi.put(c.category.name, po);
        }
        CONFIG.config.put("panes", pi);
        CONFIG.config.put("prefix", CommandManager.get().getPrefix());
        CONFIG.save();
    }
}
