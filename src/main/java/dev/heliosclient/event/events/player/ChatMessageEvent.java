package dev.heliosclient.event.events.player;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;

@Cancelable
@LuaEvent("ChatMsgEvent")
public class ChatMessageEvent extends Event {
    private final MessageSignatureData signature;
    private final MessageIndicator indicator;
    private Text message;


    public ChatMessageEvent(Text message, MessageIndicator indicator, MessageSignatureData signature) {
        this.message = message;
        this.signature = signature;
        this.indicator = indicator;
    }

    public Text getMessage() {
        return message;
    }

    public void setMessage(Text message) {
        this.message = message;
    }

    public MessageIndicator getIndicator() {
        return indicator;
    }

    public MessageSignatureData getSignature() {
        return signature;
    }
}
