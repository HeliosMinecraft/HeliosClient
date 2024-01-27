package dev.heliosclient;

import dev.heliosclient.addon.AddonManager;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.client.FontChangeEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.hud.HudElementList;
import dev.heliosclient.managers.*;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.modules.NotificationModule;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.system.Config;
import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import dev.heliosclient.ui.clickgui.gui.Quadtree;
import dev.heliosclient.ui.notification.notifications.InfoNotification;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.DamageUtils;
import dev.heliosclient.util.SoundUtils;
import dev.heliosclient.util.TimerUtils;
import dev.heliosclient.util.cape.CapeSynchronizer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
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
    public static FontManager fontManager = new FontManager();
    public static NotificationManager notificationManager = new NotificationManager();
    public static AddonManager addonManager = new AddonManager();
    public static ClickGUI CLICKGUI = new ClickGUI();

    public static void loadConfig() {
        configTimer.startTimer();
        CONFIG.loadConfig();
        CommandManager.prefix = (String) CONFIG.getClientConfigMap().get("prefix");
        CONFIG.loadClientConfigModules();
        CONFIG.loadHudElements();
        CONFIG.loadModules();
        LOGGER.info("Loading Config complete in: " + configTimer.getElapsedTime() + "s");
        if(shouldSendNotification()){
            notificationManager.addNotification(new InfoNotification("Loading Done", "in: " + configTimer.getElapsedTime() + "s", 1000, SoundUtils.TING_SOUNDEVENT));
        }
        configTimer.resetTimer();

        // Font event is posted to allow the GUI to reset its calculation for the new font by the config.
        if(FontManager.fonts != null)
        EventManager.postEvent(new FontChangeEvent(FontManager.fonts));
    }

    public static void saveConfig() {
        LOGGER.info("Saving config... \t Module Config being saved: " + Config.MODULES);
        configTimer.startTimer();
        CONFIG.getModuleConfig();
        CONFIG.getClientConfig();
        CONFIG.save();
        LOGGER.info("Saving Config complete in: " + configTimer.getElapsedTime() + "s");
        if(shouldSendNotification()){
            notificationManager.addNotification(new InfoNotification("Saving Done", "in: " + configTimer.getElapsedTime() + "s", 1000, SoundUtils.TING_SOUNDEVENT));
        }
        configTimer.resetTimer();
    }

    @Override
    public void onInitialize() {
        EventManager.register(this);

        LOGGER.info("Helios Client loading...");
        fontManager.refresh();
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

        EventManager.register(fontManager);
        EventManager.register(notificationManager);
        EventManager.register(dev.heliosclient.util.Renderer2D.INSTANCE);
        EventManager.register(KeybindManager.INSTANCE);
        EventManager.register(ColorManager.INSTANCE);
        EventManager.register(new DamageUtils());

        CapeManager.capes = CapeManager.loadCapes();
        CapeSynchronizer.registerCapeSyncPacket();

        loadConfig();
        ClickGUIScreen.INSTANCE.onLoad();
        HeliosClient.CLICKGUI.onLoad();
        // Save on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(HeliosClient::saveConfig));
    }
    @SubscribeEvent
    public void tick(TickEvent.CLIENT client){
        if (MC.getWindow() != null) {
            quadTree = new Quadtree(0);
            EventManager.unregister(this);
        }
    }
    private static boolean shouldSendNotification(){
        return (notificationManager != null && ModuleManager.notificationModule != null && ModuleManager.notificationModule.clientNotification.value && MC.getWindow() != null);
    }
}
