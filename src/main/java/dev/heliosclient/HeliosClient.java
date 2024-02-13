package dev.heliosclient;

import dev.heliosclient.addon.AddonManager;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.client.FontChangeEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.hud.HudElementList;
import dev.heliosclient.managers.*;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.system.Config;
import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import dev.heliosclient.ui.clickgui.ConsoleScreen;
import dev.heliosclient.ui.clickgui.gui.Quadtree;
import dev.heliosclient.ui.notification.notifications.InfoNotification;
import dev.heliosclient.util.*;
import dev.heliosclient.util.cape.CapeSynchronizer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeliosClient implements ModInitializer, Listener {
    public static final HeliosClient INSTANCE = new HeliosClient();
    public static final MinecraftClient MC = MinecraftClient.getInstance();
    public static final Logger LOGGER = LoggerFactory.getLogger("Helios Client");
    public static final String clientTag = ColorUtils.yellow + "Helios" + ColorUtils.white + "Client";
    public static final String versionTag = ColorUtils.gray + "v0.dev";
    public static final String MODID = "heliosclient";
    private static final TimerUtils configTimer = new TimerUtils();
    public static Quadtree quadTree;
    public static Config CONFIG = new Config();
    public static int uiColorA = 0xFF55FFFF;
    public static int uiColor = 0x55FFFF;
    public static AddonManager addonManager = new AddonManager();
    public static ClickGUI CLICKGUI = new ClickGUI();
    public static boolean isSaving = false;
    public static ConsoleScreen CONSOLE = new ConsoleScreen();

    public static void loadConfig() {
        configTimer.startTimer();
        CONFIG.loadConfig();
        CommandManager.prefix = (String) CONFIG.getClientConfigMap().get("prefix");
        CONFIG.loadClientConfigModules();

        // Font event is posted to allow the GUI to reset its calculation for the new font by the config.
        if (FontManager.fonts != null)
            EventManager.postEvent(new FontChangeEvent(FontManager.fonts));

        CONFIG.loadHudElements();
        CONFIG.loadModules();
        LOGGER.info("Loading Config complete in: " + configTimer.getElapsedTime() + "s");
        if(shouldSendNotification()){
            NotificationManager.addNotification(new InfoNotification("Loading Done", "in: " + configTimer.getElapsedTime() + "s", 1000, SoundUtils.TING_SOUNDEVENT));
        }
        configTimer.resetTimer();
    }

    public static void saveConfig() {
        if (isSaving) return;

        isSaving = true;
        LOGGER.info("Saving config... \t Module Config being saved: " + Config.MODULES);
        configTimer.startTimer();
        CONFIG.getModuleConfig();
        CONFIG.getClientConfig();
        CONFIG.save();
        LOGGER.info("Saving Config complete in: " + configTimer.getElapsedTime() + "s");
        if(shouldSendNotification()){
            NotificationManager.addNotification(new InfoNotification("Saving Done", "in: " + configTimer.getElapsedTime() + "s", 1000, SoundUtils.TING_SOUNDEVENT));
        }
        configTimer.resetTimer();
        isSaving = false;
    }

    public static boolean shouldSendNotification() {
        return (NotificationManager.INSTANCE != null && ModuleManager.notificationModule != null && ModuleManager.notificationModule.clientNotification.value && MC.getWindow() != null);
    }
    @SubscribeEvent
    public void tick(TickEvent.CLIENT client){
        if (MC.getWindow() != null) {
            quadTree = new Quadtree(0);
            EventManager.unregister(this);
        }
    }

    @Override
    public void onInitialize() {
        EventManager.register(this);
        registerEvents();
        DiscordRPC.INSTANCE.getLibrary();


        LOGGER.info("Initialising Helios Client...");

        FontManager.INSTANCE.refresh();
        addonManager.loadAddons();
        AddonManager.initializeAddons();

        Categories.registerCategories();
        SoundUtils.registerSounds();
        HudElementList.INSTANCE = new HudElementList();


        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (MC.player != null) {
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    Identifier capeTexture = CapeManager.cape;
                    CapeSynchronizer.sendCapeSyncPacket(player, capeTexture, CapeManager.getElytraTexture(MC.player));
                }
            }
        });



        loadConfig();
        ClickGUIScreen.INSTANCE.onLoad();
        HeliosClient.CLICKGUI.onLoad();

        // Save
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> HeliosClient.saveConfig());
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((a,b,c) -> HeliosClient.saveConfig());
        ServerPlayConnectionEvents.DISCONNECT.register((handler, packetSender) -> HeliosClient.saveConfig());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            saveConfig();
            if (DiscordRPC.INSTANCE.isRunning) {
                DiscordRPC.INSTANCE.stopPresence();
            }
        }));

       /* ConsoleAppender consoleAppender = new ConsoleAppender(CONSOLE);

        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        config.addAppender(consoleAppender);
        config.getRootLogger().addAppender(consoleAppender, null, null);
        ctx.updateLoggers(config);

        */
    }

    public static boolean shouldUpdate() {
        return MC.getWindow() == null && MC.player == null;
    }

    public void registerEvents() {
        EventManager.register(FontManager.INSTANCE);
        EventManager.register(NotificationManager.INSTANCE);
        EventManager.register(Renderer2D.INSTANCE);
        EventManager.register(KeybindManager.INSTANCE);
        EventManager.register(ColorManager.INSTANCE);
        EventManager.register(TickRate.INSTANCE);
        EventManager.register(CapeManager.INSTANCE);
        EventManager.register(DamageUtils.INSTANCE);
    }
}
