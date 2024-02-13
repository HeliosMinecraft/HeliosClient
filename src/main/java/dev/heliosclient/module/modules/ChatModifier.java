package dev.heliosclient.module.modules;

import com.mojang.authlib.GameProfile;
import dev.heliosclient.module.modules.chat.ChatHighlight;
import dev.heliosclient.util.ChatUtils;
import net.minecraft.network.message.MessageType.Parameters;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ChatModifier {
    /**
     * Format String for chat messages.
     * {0} = Message
     * {1} = Name of the sender
     * {2} = Timestamp in HH:MM:SS format
     */
    public static String formatString = "<{1}> {0}";

    public static void onReceiveChatMessage(Text message, @Nullable SignedMessage signedMessage, @Nullable GameProfile sender, Parameters params, Instant timestamp) {
        ChatHighlight chatHighlighter = new ChatHighlight();
        DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("H:m:s").withZone(ZoneId.systemDefault());
        assert sender != null;
        Object[] messageArguments = {chatHighlighter.onMessage(message.toString()).replace("<" + sender.getName() + ">", "").strip(), sender.getName(), timestampFormatter.format(timestamp)};

        ChatUtils.sendMsg(new MessageFormat(formatString).format(messageArguments));
    }
}