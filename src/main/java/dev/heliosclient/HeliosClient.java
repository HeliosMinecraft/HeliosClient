package dev.heliosclient;

import dev.heliosclient.addon.AddonManager;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.client.FontChangeEvent;
import dev.heliosclient.event.events.player.PlayerLeaveEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.hud.HudElementList;
import dev.heliosclient.managers.*;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.modules.misc.NotificationModule;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.scripting.LuaScriptManager;
import dev.heliosclient.system.ConsoleAppender;
import dev.heliosclient.system.DiscordRPC;
import dev.heliosclient.system.HeliosExecutor;
import dev.heliosclient.system.TickRate;
import dev.heliosclient.system.config.Config;
import dev.heliosclient.ui.clickgui.ConsoleScreen;
import dev.heliosclient.ui.notification.notifications.InfoNotification;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.SoundUtils;
import dev.heliosclient.util.TimerUtils;
import dev.heliosclient.util.player.DamageUtils;
import dev.heliosclient.util.player.RotationSimulator;
import dev.heliosclient.util.render.Renderer2D;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.WorldSaveHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.function.Consumer;

import static dev.heliosclient.managers.FontManager.fonts;

public class HeliosClient implements ModInitializer, Listener {
    public static final HeliosClient INSTANCE = new HeliosClient();
    public static final MinecraftClient MC = MinecraftClient.getInstance();
    public static final Logger LOGGER = LoggerFactory.getLogger("HeliosClient");
    public static final String clientTag = ColorUtils.yellow + "Helios" + ColorUtils.white + "Client";
    public static final String versionTag = ColorUtils.gray + "v0.dev";
    public static final String MODID = "heliosclient";
    private static final TimerUtils configTimer = new TimerUtils();
    public static Config CONFIG = new Config();
    public static AddonManager ADDONMANAGER = new AddonManager();
    public static ClickGUI CLICKGUI;
    public volatile static ConsoleScreen CONSOLE = new ConsoleScreen();
    public static File SAVE_FOLDER = new File(MC.runDirectory.getPath() + "/heliosclient");

    public static void loadConfig() {
        load(config -> {
            config.loadEverything();
            LOGGER.info("Loading Config complete in: {}s", configTimer.getElapsedTime());

            String configSelectedAsSaved = (String) config.otherConfigManager.getCurrentConfig().getReadData().get(CLICKGUI.switchConfigs.getSaveName());
            if (configSelectedAsSaved != null) {
                //We are loading at the earliest before the user can make changes so no need to save.
                config.getModuleConfigManager().switchConfig(configSelectedAsSaved, false);
                loadModulesOnly();
            }
        });
    }

    private static void load(Consumer<Config> consumer) {
        //Record time it took

        configTimer.startTimer();

        consumer.accept(CONFIG);

        if (shouldSendNotification() && ModuleManager.get(NotificationModule.class).clientNotification.value) {
            NotificationManager.addNotification(new InfoNotification("Loading Done", "in: " + configTimer.getElapsedTime() + "s", 1000, SoundUtils.TING_SOUNDEVENT));
        }

        configTimer.resetTimer();

        // Font event is posted to allow the GUI to reset its calculation for the new font by the config.
        if (fonts != null)
            EventManager.postEvent(new FontChangeEvent(fonts));
    }

    public static void loadModulesOnly() {
        load(config -> {
            CONFIG.getModuleConfigManager().load();
            CONFIG.loadModules();

            LOGGER.info("Loading Module config complete in: {}s", configTimer.getElapsedTime());
        });
    }

    public static void saveConfig() {
        HeliosExecutor.execute(HeliosClient::saveConfigHook);
    }

    public static void saveConfigHook() {
        LOGGER.info("Saving all configs... \t Info: Current module config being saved \"{}\"", CONFIG.moduleConfigManager.getCurrentConfig().getName());
        configTimer.startTimer();
        CONFIG.saveEverything();
        LOGGER.info("Saving Config complete in: {}s", configTimer.getElapsedTime());
        if (shouldSendNotification() && ModuleManager.get(NotificationModule.class).clientNotification.value) {
            NotificationManager.addNotification(new InfoNotification("Saving Done", "in: " + configTimer.getElapsedTime() + "s", 1000, SoundUtils.TING_SOUNDEVENT));
        }
        configTimer.resetTimer();
    }


    public static boolean shouldSendNotification() {
        return (NotificationManager.INSTANCE != null && ModuleManager.get(NotificationModule.class) != null && MC.getWindow() != null);
    }

    public static boolean shouldUpdate() {
        return MC != null && MC.getWindow() != null && MC.player != null && MC.world != null;
    }

    @SubscribeEvent
    public void onDisconnect(PlayerLeaveEvent client) {
        HeliosClient.saveConfigHook();
    }

    @Override
    public void onInitialize() {
        ConsoleAppender consoleAppender = new ConsoleAppender(CONSOLE);

        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        config.addAppender(consoleAppender);
        config.getRootLogger().addAppender(consoleAppender, null, null);
        ctx.updateLoggers(config);

        LOGGER.info("Initialising Helios Client...");

        ColorManager.createInstance();
        CONFIG.init();
        CLICKGUI = new ClickGUI();

        registerListeners();

        LOGGER.info("Downloading and extracting Discord Native Library-2.5.6...");
        DiscordRPC.INSTANCE.init();
        LOGGER.info("Downloading Completed...");

        LuaScriptManager.getScripts();


        FontManager.INSTANCE.refresh();
        ADDONMANAGER.loadAddons();
        AddonManager.initializeAddons();

        Categories.registerCategories();
        SoundUtils.registerSounds();
        HudElementList.INSTANCE = new HudElementList();

        CapeManager.CAPE_NAMES = CapeManager.loadCapes();


        HeliosExecutor.execute(HeliosClient::loadConfig);

        // Save when the client stops

        ClientLifecycleEvents.CLIENT_STOPPING.register((client) -> {
            saveConfigHook();
            if (DiscordRPC.INSTANCE.isRunning) {
                DiscordRPC.INSTANCE.stopPresence();
            }
            CapeManager.capeTextureManager.stopCapeAnimation();
        });
        //Saving is also handled in MixinCrashReport and ClickGUI.java


        MC.execute(() -> {
            while (MC.getWindow() == null) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    // Cope
                }
            }
            HeliosClient.CLICKGUI.onLoad();
        });

        if (fonts != null)
            EventManager.postEvent(new FontChangeEvent(fonts));
    }

    public void registerListeners() {
        EventManager.register(this);
        EventManager.register(FontManager.INSTANCE);
        EventManager.register(NotificationManager.INSTANCE);
        EventManager.register(Renderer2D.INSTANCE);
        EventManager.register(KeybindManager.INSTANCE);
        EventManager.register(ColorManager.INSTANCE);
        EventManager.register(TickRate.INSTANCE);
        EventManager.register(DamageUtils.INSTANCE);
        EventManager.register(RotationSimulator.INSTANCE);
    }
}
