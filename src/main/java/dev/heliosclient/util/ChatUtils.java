package dev.heliosclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public class ChatUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void sendMsg(String message) {
        sendMsg(null, null, Text.literal(message));
    }

    public static void sendMsg(@Nullable String prefixTitle, @Nullable Formatting prefixColor, Text msg) {
        if (mc.world == null) return;

        mc.inGameHud.getChatHud().addMessage(msg);
    }

    public static void sendHeliosMsg(String message) {
        sendMsg(null, null, Text.of("[" + ColorUtils.red + "Helios" + ColorUtils.white + "] " + message));
    }
}
