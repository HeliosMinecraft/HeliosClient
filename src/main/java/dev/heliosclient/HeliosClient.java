package dev.heliosclient;

import dev.heliosclient.addon.AddonManager;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.client.FontChangeEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.hud.HudElementList;
import dev.heliosclient.managers.*;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.modules.misc.NotificationModule;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.scripting.LuaExecutor;
import dev.heliosclient.scripting.LuaScriptManager;
import dev.heliosclient.system.Config;
import dev.heliosclient.system.DiscordRPC;
import dev.heliosclient.system.HeliosExecutor;
import dev.heliosclient.system.TickRate;
import dev.heliosclient.ui.clickgui.ConsoleScreen;
import dev.heliosclient.ui.clickgui.gui.Quadtree;
import dev.heliosclient.ui.notification.notifications.InfoNotification;
import dev.heliosclient.util.*;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.render.Renderer2D;
import me.x150.renderer.font.FontRenderer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.heliosclient.managers.FontManager.fontSize;
import static dev.heliosclient.managers.FontManager.fonts;

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
    public static ConsoleScreen CONSOLE = new ConsoleScreen();

    public static void loadConfig() {
        configTimer.startTimer();
        CommandManager.prefix = (String) CONFIG.getClientConfigMap().get("prefix");
        CONFIG.loadConfig();
        CONFIG.loadClientConfigModules();

        // Font event is posted to allow the GUI to reset its calculation for the new font by the config.
        if (FontManager.fonts != null)
            EventManager.postEvent(new FontChangeEvent(FontManager.fonts));
        
        CONFIG.loadHudElements();
        CONFIG.loadModules();
        LOGGER.info("Loading Config complete in: " + configTimer.getElapsedTime() + "s");
        if(NotificationModule.get().clientNotification.value && shouldSendNotification()){
            NotificationManager.addNotification(new InfoNotification("Loading Done", "in: " + configTimer.getElapsedTime() + "s", 1000, SoundUtils.TING_SOUNDEVENT));
        }
        if (HeliosClient.MC.getWindow() != null) {
            FontManager.INSTANCE.registerFonts();
            FontRenderers.fontRenderer = new FontRenderer(fonts, fontSize);
            EventManager.postEvent(new FontChangeEvent(fonts));
        }
        configTimer.resetTimer();
    }

    public static void saveConfig() {
        HeliosExecutor.execute(HeliosClient::saveConfigHook);
    }
    public static void saveConfigHook() {
            LOGGER.info("Saving config... \t Module Config being saved: " + Config.MODULES);
            configTimer.startTimer();
            CONFIG.getModuleConfig();
            CONFIG.getClientConfig();
            CONFIG.save();
            LOGGER.info("Saving Config complete in: " + configTimer.getElapsedTime() + "s");
            if(NotificationModule.get().clientNotification.value && shouldSendNotification()){
                NotificationManager.addNotification(new InfoNotification("Saving Done", "in: " + configTimer.getElapsedTime() + "s", 1000, SoundUtils.TING_SOUNDEVENT));
            }
            configTimer.resetTimer();
    }


    public static boolean shouldSendNotification() {
        return (NotificationManager.INSTANCE != null && NotificationModule.get() != null && MC.getWindow() != null);
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
        LOGGER.info("Initialising Helios Client...");

        EventManager.register(this);
        registerEvents();

        LOGGER.info("Downloading and extracting Discord Native Library-2.5.6...");
        DiscordRPC.INSTANCE.init();
        LOGGER.info("Downloading Completed...");

        LuaScriptManager.getScripts();


        FontManager.INSTANCE.refresh();
        addonManager.loadAddons();
        AddonManager.initializeAddons();

        Categories.registerCategories();
        SoundUtils.registerSounds();
        HudElementList.INSTANCE = new HudElementList();


        CapeManager.capes = CapeManager.loadCapes();

        HeliosExecutor.execute(HeliosClient::loadConfig);

        // Save
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> HeliosClient.saveConfig());
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((a,b,c) -> HeliosClient.saveConfig());
        ServerPlayConnectionEvents.DISCONNECT.register((handler, packetSender) -> HeliosClient.saveConfig());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            saveConfigHook();
            if (DiscordRPC.INSTANCE.isRunning) {
                DiscordRPC.INSTANCE.stopPresence();
            }
            HeliosExecutor.shutdown();
        }));

       /* ConsoleAppender consoleAppender = new ConsoleAppender(CONSOLE);

        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        config.addAppender(consoleAppender);
        config.getRootLogger().addAppender(consoleAppender, null, null);
        ctx.updateLoggers(config);

        */
        MC.execute(()->{
            while(MC.getWindow() == null){
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    // Cope
                }
            }
           HeliosClient.CLICKGUI.onLoad();
        });
    }

    public static boolean shouldUpdate() {
        return MC != null && MC.getWindow() != null && MC.player != null;
    }

    public void registerEvents() {
        EventManager.register(FontManager.INSTANCE);
        EventManager.register(NotificationManager.INSTANCE);
        EventManager.register(Renderer2D.INSTANCE);
        EventManager.register(KeybindManager.INSTANCE);
        EventManager.register(ColorManager.INSTANCE);
        EventManager.register(TickRate.INSTANCE);
        EventManager.register(DamageUtils.INSTANCE);
    }
}
