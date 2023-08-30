package dev.heliosclient.util;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ChatUtils 
{
    private static MinecraftClient mc = MinecraftClient.getInstance();

    public static void sendMsg(String message) 
    {
        sendMsg(null, null, Text.literal(message));
    }

    public static void sendMsg(@Nullable String prefixTitle, @Nullable Formatting prefixColor, Text msg) 
    {
        if (mc.world == null) return;

        //Text message = Text.literal("");
        //message.append(CommandManager.get().getPrefix());
        //if (prefixTitle != null) message.append(CommandManager.get().getPrefix());
        //message.append(msg);

        mc.inGameHud.getChatHud().addMessage(msg);
    }
    public static void sendHeliosMsg(String message){
        sendMsg(null,null,Text.of("["+ColorUtils.red + "Helios"+ColorUtils.white+"] "+ message));
    }
}
