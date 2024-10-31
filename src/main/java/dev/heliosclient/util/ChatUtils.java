package dev.heliosclient.util;

import dev.heliosclient.util.color.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ChatUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Map<String, Supplier<Text>> prefixes = new HashMap<>();
    private static final Text HELIOS_PREFIX = Text.of("[" + ColorUtils.red + "Helios" + ColorUtils.white + "] ");

    static {
        addPrefix("Helios",()-> HELIOS_PREFIX);
    }

    public static void addPrefix(String prefix, Supplier<Text> supplier) {
        prefixes.put(prefix, supplier);
    }

    public static void removePrefix(String prefix) {
        prefixes.remove(prefix);
    }

    public static Supplier<Text> getPrefixSupplier(String prefix) {
        return prefixes.get(prefix);
    }

    public static void sendMsg(String message) {
        sendMsg(Text.literal(message));
    }

    public static void sendMsg(Text msg) {
        if (mc.world == null) return;

        mc.execute(()->{
           mc.inGameHud.getChatHud().addMessage(msg);
        });
    }

    public static void sendHeliosMsg(Text msg) {
        //Space is added after the Helios name

       sendMsg(HELIOS_PREFIX.copy().append(msg));
    }


    public static void sendHeliosMsg(String message) {
        sendHeliosMsg(Text.of(message));
    }

    public static void sendPrefixMessage(String prefix, String message) {
        Supplier<Text> prefixSupplier = getPrefixSupplier(prefix);
        if (prefixSupplier == null) {
            throw new IllegalStateException("No supplier set for prefix " + prefix);
        }

        ChatUtils.sendMsg(prefixSupplier.get().copy().append(message));
    }

    public static void sendPlayerMessage(String message) {
        if (mc.player == null) return;

        mc.getNetworkHandler().sendChatMessage(message);
    }
    public static void sendPlayerMessageOrCommand(String message) {
        if (mc.player == null) return;

        if(message.startsWith("/")) mc.getNetworkHandler().sendChatCommand(message);
        else mc.getNetworkHandler().sendChatMessage(message);
    }

    public static void sendChatCommand(String message) {
        if (mc.player == null) return;

        mc.getNetworkHandler().sendChatCommand(message);
    }

    public static void sendCommand(String message) {
        if (mc.player == null) return;

        mc.getNetworkHandler().sendCommand(message);
    }

    public static Text getHeliosPrefix() {
        return HELIOS_PREFIX;
    }
}
