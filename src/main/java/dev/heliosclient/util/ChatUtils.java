package dev.heliosclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.ast.Str;

public class ChatUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void sendMsg(String message) {
        sendMsg(Text.literal(message));
    }

    public static void sendMsg(Text msg) {
        if (mc.world == null) return;

        mc.inGameHud.getChatHud().addMessage(msg);
    }


    public static void sendHeliosMsg(String message) {
        sendMsg(Text.of("[" + ColorUtils.red + "Helios" + ColorUtils.white + "] " + message));
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
