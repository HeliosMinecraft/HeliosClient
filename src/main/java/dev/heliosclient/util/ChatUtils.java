package dev.heliosclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class ChatUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void sendMsg(String message) {
        sendMsg(Text.literal(message));
    }

    public static void sendMsg(Text msg) {
        if (mc.world == null) return;

        mc.inGameHud.getChatHud().addMessage(msg);
    }

    public static void sendHeliosMsg(Text msg) {
        if (mc.world == null) return;

        mc.inGameHud.getChatHud().addMessage(Text.of("[" + ColorUtils.red + "Helios" + ColorUtils.white + "] " + msg.getString()));
    }


    public static void sendHeliosMsg(String message) {
        sendHeliosMsg(Text.of(message));
    }

    public static void sendPlayerMessage(String message) {
        if (mc.player == null) return;

        mc.getNetworkHandler().sendChatMessage(message);
    }

    public static void sendChatCommand(String message) {
        if (mc.player == null) return;

        mc.getNetworkHandler().sendChatCommand(message);
    }

    public static void sendCommand(String message) {
        if (mc.player == null) return;

        mc.getNetworkHandler().sendCommand(message);
    }
}
