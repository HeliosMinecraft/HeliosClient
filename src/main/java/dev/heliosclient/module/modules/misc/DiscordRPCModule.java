package dev.heliosclient.module.modules.misc;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.system.DiscordRPC;
import dev.heliosclient.util.animation.AnimationUtils;
import net.minecraft.client.gui.screen.multiplayer.AddServerScreen;
import net.minecraft.client.gui.screen.multiplayer.DirectConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;

public class DiscordRPCModule extends Module_ {
    public DiscordRPCModule() {
        super("DiscordRPC", "Start/Stop Discord Rich Presence", Categories.MISC);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (DiscordRPC.INSTANCE.isRunning) {
            AnimationUtils.addErrorToast("DiscordRPC is already running!", true, 1000);
            HeliosClient.LOGGER.error("DiscordRPC is already running!");
            return;
        }
        DiscordRPC.INSTANCE.runPresence();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        DiscordRPC.INSTANCE.stopPresence();
    }

    @SubscribeEvent
    public void onTick(TickEvent.CLIENT event) {
        if (!DiscordRPC.INSTANCE.isRunning) return;

        if (HeliosClient.MC.currentScreen instanceof SelectWorldScreen || HeliosClient.MC.currentScreen instanceof CreateWorldScreen) {
            DiscordRPC.INSTANCE.currentGameState = DiscordRPC.GameState.SINGLEPLAYER_SCREEN;
        } else if (HeliosClient.MC.currentScreen instanceof MultiplayerScreen || HeliosClient.MC.currentScreen instanceof DirectConnectScreen || HeliosClient.MC.currentScreen instanceof AddServerScreen) {
            DiscordRPC.INSTANCE.currentGameState = DiscordRPC.GameState.MULTIPLAYER_SCREEN;
        } else if (HeliosClient.MC.player != null) {
            if (HeliosClient.MC.isInSingleplayer()) {
                DiscordRPC.INSTANCE.currentGameState = DiscordRPC.GameState.SINGLEPLAYER;
            }
            if (!HeliosClient.MC.isInSingleplayer() || HeliosClient.MC.player.getServer() != null) {
                DiscordRPC.INSTANCE.currentGameState = DiscordRPC.GameState.MULTIPLAYER;
            }
        } else if (HeliosClient.MC.currentScreen instanceof RealmsScreen) {
            DiscordRPC.INSTANCE.currentGameState = DiscordRPC.GameState.REALMS;
        } else {
            DiscordRPC.INSTANCE.currentGameState = DiscordRPC.GameState.MAINMENU;
        }

        DiscordRPC.INSTANCE.updateActivity();
    }
}
